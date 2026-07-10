package com.cloud.file.dto.request;

import lombok.Data;

/**
 * 文件上传请求
 */
@Data
public class FileUploadRequest {

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 业务 ID
     */
    private Long businessId;
}