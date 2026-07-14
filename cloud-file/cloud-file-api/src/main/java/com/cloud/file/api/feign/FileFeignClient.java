package com.cloud.file.api.feign;

import com.cloud.common.result.Result;
import com.cloud.file.api.dto.FileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 文件服务 Feign 客户端接口
 * 用于其他服务通过 Feign 调用文件服务
 */
@FeignClient(contextId = "fileFeignClient", name = "cloud-file-service",configuration = FeignConfig.class)
public interface FileFeignClient {

    /**
     * 根据 fileKey 获取文件信息
     */
    @GetMapping("/file/info")
    Result<FileResponse> getFileByKey(@RequestParam("key") String fileKey);

    /**
     * 删除文件
     */
    @DeleteMapping("/file/delete")
    Result<Void> deleteFile(@RequestParam("key") String fileKey);

    /**
     * 获取临时访问 URL
     */
    @GetMapping("/file/presigned-url")
    Result<String> getPresignedUrl(
            @RequestParam("key") String fileKey,
            @RequestParam(value = "expireSeconds", defaultValue = "3600") Integer expireSeconds
    );
}
