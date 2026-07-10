package com.cloud.file.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件响应
 */
@Data
public class FileResponse {

    /**
     * 文件 ID
     */
    private Long id;

    /**
     * 文件唯一标识
     */
    private String fileKey;

    /**
     * 原始文件名
     */
    private String originalFileName;

    /**
     * 文件扩展名
     */
    private String fileExtension;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * MIME 类型
     */
    private String contentType;

    /**
     * 存储类型
     */
    private String storageType;

    /**
     * 访问 URL
     */
    private String fileUrl;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 业务 ID
     */
    private Long businessId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}