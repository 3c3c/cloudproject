package com.cloud.file.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.common.exception.BusinessException;
import com.cloud.common.result.Result;
import com.cloud.common.result.ResultCode;
import com.cloud.file.api.dto.BatchDeleteRequest;
import com.cloud.file.api.dto.BatchDeleteResult;
import com.cloud.file.api.dto.FileResponse;
import com.cloud.file.service.FileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
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
//    @PreAuthorize("hasAuthority('file:upload')")
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
//    @PreAuthorize("hasAuthority('file:upload')")
    public Result<List<FileResponse>> batchUpload(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "businessType", required = false) String businessType,
            @RequestParam(value = "businessId", required = false) Long businessId
    ) {
        List<FileResponse> responses = fileService.uploadFiles(files, businessType, businessId);
        return Result.success(responses);
    }

    /**
     * 文件下载（流式返回，文件内容不读入内存）
     */
    @GetMapping("/download")
//    @PreAuthorize("hasAuthority('file:download')")
    public ResponseEntity<Resource> download(@RequestParam("id") Long id) {
        FileResponse fileInfo = fileService.getFileById(id);
        InputStream inputStream = fileService.downloadFile(fileInfo.getId());
        // InputStreamResource 流式写出，Spring MVC 边读边写到响应，响应完成后自动关闭流
        InputStreamResource resource = new InputStreamResource(inputStream);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileInfo.getOriginalFileName() + "\"")
                .contentType(MediaType.parseMediaType(fileInfo.getContentType()))
                .contentLength(fileInfo.getFileSize() != null ? fileInfo.getFileSize() : -1)
                .body(resource);
    }

    /**
     * 按 id 预览文件（流式返回，支持直接通过 URL 访问图片、PDF 等）
     */
    @GetMapping("/view")
//    @PreAuthorize("hasAuthority('file:preview')")
    public ResponseEntity<Resource> viewById(@RequestParam("id") Long id) {
        FileResponse fileInfo = fileService.getFileById(id);
        InputStream inputStream = fileService.downloadFile(fileInfo.getId());
        InputStreamResource resource = new InputStreamResource(inputStream);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .contentType(MediaType.parseMediaType(fileInfo.getContentType()))
                .contentLength(fileInfo.getFileSize() != null ? fileInfo.getFileSize() : -1)
                .cacheControl(org.springframework.http.CacheControl.maxAge(30, java.util.concurrent.TimeUnit.DAYS))
                .body(resource);
    }

    /**
     * 获取临时访问 URL
     */
    @GetMapping("/presigned-url")
//    @PreAuthorize("hasAuthority('file:preview')")
    public Result<String> getPresignedUrl(
            @RequestParam("id") Long id,
            @RequestParam(value = "expireSeconds", defaultValue = "3600") Integer expireSeconds
    ) {
        String presignedUrl = fileService.getPresignedUrl(id, expireSeconds);
        return Result.success(presignedUrl);
    }

    /**
     * 单文件删除
     */
    @DeleteMapping("/delete")
//    @PreAuthorize("hasAuthority('file:delete')")
    public Result<Void> delete(@RequestParam("id") Long id) {
        try {
            boolean deleted = fileService.deleteFile(id);
            return deleted ? Result.success() : Result.error(ResultCode.FILE_DELETE_FAILED, "删除失败");
        } catch (Exception e) {
            log.error("文件删除失败: id={}, error={}", id, e.getMessage(), e);
            throw new BusinessException(ResultCode.FILE_DELETE_FAILED, "文件删除失败");
        }
    }

    /**
     * 批量文件删除
     */
    @DeleteMapping("/batch")
//    @PreAuthorize("hasAuthority('file:delete')")
    public Result<BatchDeleteResult> batchDelete(@Valid @RequestBody BatchDeleteRequest request) {
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
//    @PreAuthorize("hasAuthority('file:query')")
    public Result<Page<FileResponse>> page(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "businessType", required = false) String businessType
    ) {
        Page<FileResponse> page = fileService.pageFiles(current, size, businessType);
        return Result.success(page);
    }

    /**
     * 根据 id 获取文件信息
     */
    @GetMapping("/info")
//    @PreAuthorize("hasAuthority('file:query')")
    public Result<FileResponse> getFileInfo(@RequestParam("id") Long id) {
        FileResponse fileInfo = fileService.getFileById(id);
        return Result.success(fileInfo);
    }
}