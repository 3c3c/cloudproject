package com.cloud.file.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.common.exception.BusinessException;
import com.cloud.common.result.ResultCode;
import com.cloud.common.utils.IdUtils;
import com.cloud.file.config.FileStorageProperties;
import com.cloud.file.converter.FileConverter;
import com.cloud.file.dto.response.FileResponse;
import com.cloud.file.entity.FileInfo;
import com.cloud.file.mapper.FileInfoMapper;
import com.cloud.file.service.FileService;
import com.cloud.file.storage.FileStorageService;
import com.cloud.file.utils.FileKeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

        // 4. 生成 fileKey
        String fileKey = FileKeyGenerator.generateKey(
                businessType != null ? businessType : "general",
                file.getOriginalFilename()
        );

        // 5. 上传到存储服务
        try (InputStream inputStream = file.getInputStream()) {
            fileStorageService.upload(
                    inputStream,
                    fileKey,
                    file.getContentType(),
                    file.getSize()
            );
        } catch (Exception e) {
            throw new BusinessException(ResultCode.FILE_UPLOAD_FAILED, "上传失败");
        }

        // 6. 保存文件信息到数据库
        FileInfo fileInfo = new FileInfo();
        long id = IdUtils.nextId();
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
        log.info("File uploaded successfully: id={}, key={}", fileInfo.getId(), fileKey);
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
    public byte[] downloadFile(Long id) {
        FileResponse fileResponse = getFileById(id);
        try (InputStream inputStream = fileStorageService.download(fileResponse.getFileKey())) {
            return inputStream.readAllBytes();
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

        // 1. 删除存储中的文件
        boolean deleted = fileStorageService.delete(fileInfo.getFileKey());
        if (!deleted) {
            log.warn("Failed to delete file from storage: {}", fileInfo.getFileKey());
        }

        // 2. 逻辑删除数据库记录
        fileInfo.setDeleted(System.currentTimeMillis());
        fileInfoMapper.updateById(fileInfo);

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
        String extension = FileKeyGenerator.getExtension(file.getOriginalFilename());
        if (storageProperties.getAllowedExtensions() != null &&
                !storageProperties.getAllowedExtensions().contains(extension)) {
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