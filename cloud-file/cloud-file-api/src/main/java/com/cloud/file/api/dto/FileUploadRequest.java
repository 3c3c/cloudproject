package com.cloud.file.api.dto;

import lombok.Data;

/**
 * 文件上传请求 DTO
 */
@Data
public class FileUploadRequest {

    /**
     * 业务类型（如：avatar、document等）
     */
    private String businessType;

    /**
     * 业务 ID（关联的具体业务记录ID）
     */
    private Long businessId;
}
