package com.cloud.file.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.common.exception.BusinessException;
import com.cloud.common.result.Result;
import com.cloud.common.result.ResultCode;
import com.cloud.file.dto.request.BatchDeleteRequest;
import com.cloud.file.dto.response.BatchDeleteResult;
import com.cloud.file.dto.response.FileResponse;
import com.cloud.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件管理 REST API
 */
@Slf4j
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * 单文件上传
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('file:upload')")
    public Result<FileResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "businessType", required = false) String businessType,
            @RequestParam(value = "businessId", required = false) Long businessId
    ) {
        FileResponse response = fileService.uploadFile(file, businessType, businessId);
        return Result.success(response);
    }

    /**
     * 批量文件上传
     */
    @PostMapping("/batch-upload")
    @PreAuthorize("hasAuthority('file:upload')")
    public Result<List<FileResponse>> batchUpload(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "businessType", required = false) String businessType,
            @RequestParam(value = "businessId", required = false) Long businessId
    ) {
        List<FileResponse> responses = fileService.uploadFiles(files, businessType, businessId);
        return Result.success(responses);
    }

    /**
     * 文件下载（返回文件流，用于下载）
     */
    @GetMapping("/download")
    @PreAuthorize("hasAuthority('file:download')")
    public ResponseEntity<Resource> download(@RequestParam("key") String fileKey) {
        try {
            FileResponse fileInfo = fileService.getFileByKey(fileKey);
            byte[] fileBytes = fileService.downloadFile(fileInfo.getId());
            ByteArrayResource resource = new ByteArrayResource(fileBytes);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileInfo.getOriginalFileName() + "\"")
                    .contentType(MediaType.parseMediaType(fileInfo.getContentType()))
                    .contentLength(fileBytes.length)
                    .body(resource);
        } catch (Exception e) {
            log.error("文件下载失败: key={}, error={}", fileKey, e.getMessage(), e);
            throw new BusinessException(ResultCode.FILE_DOWNLOAD_FAILED, "文件下载失败");
        }
    }

    /**
     * 按 fileKey 预览文件（支持直接通过URL访问图片、PDF等）
     */
    @GetMapping("/view")
    @PreAuthorize("hasAuthority('file:preview')")
    public ResponseEntity<Resource> viewByKey(@RequestParam("key") String fileKey) {
        try {
            FileResponse fileInfo = fileService.getFileByKey(fileKey);
            byte[] fileBytes = fileService.downloadFile(fileInfo.getId());

            ByteArrayResource resource = new ByteArrayResource(fileBytes);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .contentType(MediaType.parseMediaType(fileInfo.getContentType()))
                    .contentLength(fileBytes.length)
                    .cacheControl(org.springframework.http.CacheControl.maxAge(30, java.util.concurrent.TimeUnit.DAYS))
                    .body(resource);
        } catch (Exception e) {
            log.error("文件预览失败: key={}, error={}", fileKey, e.getMessage(), e);
            throw new BusinessException(ResultCode.FILE_NOT_FOUND, "文件预览失败");
        }
    }

    /**
     * 获取临时访问 URL
     */
    @GetMapping("/presigned-url")
    @PreAuthorize("hasAuthority('file:preview')")
    public Result<String> getPresignedUrl(
            @RequestParam("key") String fileKey,
            @RequestParam(value = "expireSeconds", defaultValue = "3600") Integer expireSeconds
    ) {
        FileResponse fileInfo = fileService.getFileByKey(fileKey);
        String presignedUrl = fileService.getPresignedUrl(fileInfo.getId(), expireSeconds);
        return Result.success(presignedUrl);
    }

    /**
     * 单文件删除
     */
    @DeleteMapping("/delete")
    @PreAuthorize("hasAuthority('file:delete')")
    public Result<Void> delete(@RequestParam("key") String fileKey) {
        try {
            FileResponse fileInfo = fileService.getFileByKey(fileKey);
            boolean deleted = fileService.deleteFile(fileInfo.getId());
            return deleted ? Result.success() : Result.error(ResultCode.FILE_DELETE_FAILED, "删除失败");
        } catch (Exception e) {
            log.error("文件删除失败: key={}, error={}", fileKey, e.getMessage(), e);
            throw new BusinessException(ResultCode.FILE_DELETE_FAILED, "文件删除失败");
        }
    }

    /**
     * 批量文件删除
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('file:delete')")
    public Result<BatchDeleteResult> batchDelete(@RequestBody BatchDeleteRequest request) {
        int successCount = fileService.batchDeleteFiles(request.getFileIds());
        BatchDeleteResult result = new BatchDeleteResult();
        result.setTotalCount(request.getFileIds().size());
        result.setSuccessCount(successCount);
        result.setFailedCount(request.getFileIds().size() - successCount);
        return Result.success(result);
    }

    /**
     * 分页查询文件
     */
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('file:query')")
    public Result<Page<FileResponse>> page(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "businessType", required = false) String businessType
    ) {
        Page<FileResponse> page = fileService.pageFiles(current, size, businessType);
        return Result.success(page);
    }

    /**
     * 根据 fileKey 获取文件信息
     */
    @GetMapping("/info")
    @PreAuthorize("hasAuthority('file:query')")
    public Result<FileResponse> getFileInfo(@RequestParam("key") String fileKey) {
        FileResponse fileInfo = fileService.getFileByKey(fileKey);
        return Result.success(fileInfo);
    }
}