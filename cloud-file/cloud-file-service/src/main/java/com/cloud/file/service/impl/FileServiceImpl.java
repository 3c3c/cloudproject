package com.cloud.file.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.common.exception.BusinessException;
import com.cloud.common.result.ResultCode;
import com.cloud.file.config.FileStorageProperties;
import com.cloud.file.converter.FileConverter;
import com.cloud.file.api.dto.FileResponse;
import com.cloud.file.entity.FileInfo;
import com.cloud.file.mapper.FileInfoMapper;
import com.cloud.file.service.FileService;
import com.cloud.file.storage.FileStorageService;
import com.cloud.file.utils.FileKeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileStorageService fileStorageService;
    private final FileInfoMapper fileInfoMapper;
    private final FileConverter fileConverter;
    private final FileStorageProperties storageProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileResponse uploadFile(MultipartFile file, String businessType, Long businessId) {
        // 1. 文件校验
        validateFile(file);

        // 2. 计算 MD5（用于去重）
        String md5;
        try (InputStream inputStream = file.getInputStream()) {
            md5 = DigestUtil.md5Hex(inputStream);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.FILE_UPLOAD_FAILED, "Failed to calculate file MD5");
        }

        // 3. 检查文件是否已存在（去重）
        FileInfo existingFile = fileInfoMapper.selectOne(
                new LambdaQueryWrapper<FileInfo>()
                        .eq(FileInfo::getFileMd5, md5)
                        .eq(FileInfo::getDeleted, false)
        );
        if (existingFile != null) {
            log.info("File already exists, returning existing record: {}", existingFile.getId());
            return fileConverter.toResponse(existingFile);
        }

        // 4. 生成 fileKey（事务内确定，供 afterCommit 上传使用）
        final String fileKey = FileKeyGenerator.generateKey(
                businessType != null ? businessType : "general",
                file.getOriginalFilename()
        );

        // 5. 事务内：先落库（存储尚未上传）。
        // id 由 MyBatis-Plus 的 IdWorker 生成（与实体 @TableId(ASSIGN_ID) 底层同一套雪花算法），
        // 多实例下基于 machineId/PID 自动分配 workerId，避免硬编码导致的 ID 冲突。
        FileInfo fileInfo = new FileInfo();
        long id = com.baomidou.mybatisplus.core.toolkit.IdWorker.getId();
        fileInfo.setId(id);
        fileInfo.setFileKey(fileKey);
        fileInfo.setOriginalFileName(file.getOriginalFilename());
        fileInfo.setFileExtension(FileKeyGenerator.getExtension(file.getOriginalFilename()));
        fileInfo.setFileSize(file.getSize());
        fileInfo.setContentType(file.getContentType());
        fileInfo.setStorageType(fileStorageService.getStorageType().getCode());
        fileInfo.setFileMd5(md5);
        fileInfo.setBusinessType(businessType);
        fileInfo.setBusinessId(businessId);
        fileInfo.setFileUrl(buildFileUrl(id));
        fileInfoMapper.insert(fileInfo);

        // 捕获上传所需的不可变快照（MultipartFile 在事务提交后仍需可读，故在事务内取出流复用需谨慎：
        // 这里把上传动作整体放到 afterCommit，直接用 file 对象——它在请求作用域内有效）
        final MultipartFile fileToUpload = file;
        final Long fileId = id;

        // 6. 事务提交后再上传存储——避免网络 IO 长占 DB 连接，并保证“DB 记录已持久化”才真正写存储。
        //    上传失败则补偿：删除刚才插入的 DB 记录（物理删，因它对应的文件并未成功写入存储），保持一致。
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try (InputStream inputStream = fileToUpload.getInputStream()) {
                    fileStorageService.upload(
                            inputStream,
                            fileKey,
                            fileToUpload.getContentType(),
                            fileToUpload.getSize()
                    );
                    log.info("File uploaded to storage successfully: id={}, key={}", fileId, fileKey);
                } catch (Exception e) {
                    log.error("Storage upload failed after DB commit, compensating by deleting record: id={}", fileId, e);
                    // 补偿：物理删除 DB 记录（此记录对应的文件并未成功写入存储）
                    fileInfoMapper.deleteById(fileId);
                }
            }
        });

        log.info("File record saved, pending storage upload: id={}, key={}", fileInfo.getId(), fileKey);
        return fileConverter.toResponse(fileInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<FileResponse> uploadFiles(List<MultipartFile> files, String businessType, Long businessId) {
        return files.stream()
                .map(file -> uploadFile(file, businessType, businessId))
                .collect(Collectors.toList());
    }

    @Override
    public FileResponse getFileById(Long id) {
        FileInfo fileInfo = fileInfoMapper.selectById(id);
        if (fileInfo == null) {
            throw new BusinessException(ResultCode.FILE_NOT_FOUND);
        }
        return fileConverter.toResponse(fileInfo);
    }

    @Override
    public FileResponse getFileByKey(String fileKey) {
        FileInfo fileInfo = fileInfoMapper.selectOne(
                new LambdaQueryWrapper<FileInfo>()
                        .eq(FileInfo::getFileKey, fileKey)
                        .eq(FileInfo::getDeleted, false)
        );
        if (fileInfo == null) {
            throw new BusinessException(ResultCode.FILE_NOT_FOUND);
        }
        return fileConverter.toResponse(fileInfo);
    }

    @Override
    public InputStream downloadFile(Long id) {
        FileResponse fileResponse = getFileById(id);
        try {
            // 直接返回底层存储的输入流，不读入内存。流的关闭由调用方（InputStreamResource）负责。
            return fileStorageService.download(fileResponse.getFileKey());
        } catch (Exception e) {
            throw new BusinessException(ResultCode.FILE_DOWNLOAD_FAILED);
        }
    }

    @Override
    public String getPresignedUrl(Long id, Integer expireSeconds) {
        FileResponse fileResponse = getFileById(id);
        return fileStorageService.getPresignedUrl(fileResponse.getFileKey(), expireSeconds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFile(Long id) {
        FileInfo fileInfo = fileInfoMapper.selectById(id);
        if (fileInfo == null) {
            return false;
        }

        // 1. 事务内：先逻辑删除 DB 记录。
        //    删除存储是物理删除、不可逆，故必须保证 DB 已标记删除后才删存储，
        //    避免“存储已删但 DB 回滚”导致文件永久丢失。存储删除失败不影响 DB 软删结果，
        //    残留的孤儿存储对象可由后台定期清理任务回收。
        fileInfo.setDeleted(System.currentTimeMillis());
        fileInfoMapper.updateById(fileInfo);

        final String fileKey = fileInfo.getFileKey();
        final Long fileId = id;

        // 2. 事务提交后再删除存储对象（网络 IO 不占用 DB 事务）。
        //    失败仅记日志——DB 已软删，文件对用户不可见；孤儿对象留待后台清理，绝不回滚 DB。
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    boolean deleted = fileStorageService.delete(fileKey);
                    if (!deleted) {
                        log.warn("Storage object not deleted (will remain as orphan): fileKey={}", fileKey);
                    } else {
                        log.info("Storage object deleted: id={}, key={}", fileId, fileKey);
                    }
                } catch (Exception e) {
                    log.error("Storage deletion failed, DB already soft-deleted (orphan object left): id={}", fileId, e);
                }
            }
        });

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteFiles(List<Long> fileIds) {
        int successCount = 0;
        for (Long fileId : fileIds) {
            if (deleteFile(fileId)) {
                successCount++;
            }
        }
        return successCount;
    }

    @Override
    public Page<FileResponse> pageFiles(Integer current, Integer size,
                                        String businessType) {
        Page<FileInfo> page = new Page<>(current, size);

        LambdaQueryWrapper<FileInfo> queryWrapper = new LambdaQueryWrapper<FileInfo>()
                .eq(FileInfo::getDeleted, false);

        if (businessType != null && !businessType.isEmpty()) {
            queryWrapper.eq(FileInfo::getBusinessType, businessType);
        }

        queryWrapper.orderByDesc(FileInfo::getCreateTime);

        Page<FileInfo> fileInfoPage = fileInfoMapper.selectPage(page, queryWrapper);

        // 转换为 FileResponse
        Page<FileResponse> responsePage = new Page<>();
        responsePage.setCurrent(fileInfoPage.getCurrent());
        responsePage.setSize(fileInfoPage.getSize());
        responsePage.setTotal(fileInfoPage.getTotal());
        responsePage.setRecords(
                fileInfoPage.getRecords().stream()
                        .map(fileConverter::toResponse)
                        .collect(Collectors.toList())
        );

        return responsePage;
    }

    /**
     * 文件校验
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "File is empty");
        }

        // 文件大小校验
        if (file.getSize() > storageProperties.getMaxSize()) {
            throw new BusinessException(ResultCode.FILE_TOO_LARGE);
        }

        // 文件扩展名校验
        // 注意：必须用 getAllowedExtensionList() 按列表精确匹配，
        // 不能对 getAllowedExtensions() 整串做 .contains() ——那会变成子串匹配，
        // 导致扩展名 "g"/"p"/"ar" 等（恰好是 jpg/png/rar 的子串）被误判为合法。
        String extension = FileKeyGenerator.getExtension(file.getOriginalFilename());
        List<String> allowed = storageProperties.getAllowedExtensionList().stream()
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(java.util.stream.Collectors.toList());
        if (!allowed.isEmpty() && !allowed.contains(extension)) {
            throw new BusinessException(ResultCode.FILE_TYPE_NOT_ALLOWED);
        }
    }

    /**
     * 构建文件 URL
     */
    private String buildFileUrl(Long id) {
        return storageProperties.getBaseUrl() + "/file/download?id=" + id;
    }
}