package com.cloud.auth.feign;

import com.cloud.common.result.Result;
import com.cloud.file.api.dto.FileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 文件服务 Feign 客户端（通过 Nacos 服务名解析实例）。
 * 注意：文件上传功能由于涉及 MultipartFile，Feign 支持有限，建议直接在网关层路由或使用专用接口。
 */
@FeignClient(name = "cloud-file-service", configuration = FeignConfig.class)
public interface FileFeignClient {

    /**
     * 根据 ID 获取文件信息
     */
    @GetMapping("/file/{id}")
    Result<FileResponse> getFileById(@PathVariable("id") Long id);

    /**
     * 根据 fileKey 获取文件信息
     */
    @GetMapping("/file/by-key")
    Result<FileResponse> getFileByKey(@RequestParam("fileKey") String fileKey);

    /**
     * 删除文件
     */
    @DeleteMapping("/file/{id}")
    Result<Void> deleteFile(@PathVariable("id") Long id);

    /**
     * 获取临时访问 URL
     */
    @GetMapping("/file/presigned-url/{id}")
    Result<String> getPresignedUrl(
            @PathVariable("id") Long id,
            @RequestParam(value = "expireSeconds", defaultValue = "3600") Integer expireSeconds
    );
}
