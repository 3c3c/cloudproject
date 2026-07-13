package com.cloud.file.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.common.result.Result;
import com.cloud.common.result.ResultCode;
import com.cloud.file.dto.request.BatchDeleteRequest;
import com.cloud.file.dto.response.BatchDeleteResult;
import com.cloud.file.dto.response.FileResponse;
import com.cloud.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
     * 文件下载
     */
    @GetMapping("/download/{id}")
    @PreAuthorize("hasAuthority('file:download')")
    public Result<byte[]> download(@PathVariable Long id) {
        byte[] fileBytes = fileService.downloadFile(id);
        return Result.success(fileBytes);
    }

    /**
     * 文件预览
     */
    @GetMapping("/preview/{id}")
    @PreAuthorize("hasAuthority('file:preview')")
    public Result<FileResponse> preview(@PathVariable Long id) {
        FileResponse fileInfo = fileService.getFileById(id);
        return Result.success(fileInfo);
    }

    /**
     * 获取临时访问 URL
     */
    @GetMapping("/presigned-url/{id}")
    @PreAuthorize("hasAuthority('file:preview')")
    public Result<String> getPresignedUrl(
            @PathVariable Long id,
            @RequestParam(value = "expireSeconds", defaultValue = "3600") Integer expireSeconds
    ) {
        String presignedUrl = fileService.getPresignedUrl(id, expireSeconds);
        return Result.success(presignedUrl);
    }

    /**
     * 单文件删除
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('file:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        boolean deleted = fileService.deleteFile(id);
        return deleted ? Result.success() : Result.error(ResultCode.FILE_DELETE_FAILED, "删除失败");
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
     * 根据 ID 获取文件信息
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('file:query')")
    public Result<FileResponse> getFileById(@PathVariable Long id) {
        FileResponse fileInfo = fileService.getFileById(id);
        return Result.success(fileInfo);
    }
}