package com.cloud.file.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloud.common.entity.BaseEntity;
import lombok.Data;

/**
 * 文件信息实体
 */
@Data
@TableName("file_info")
public class FileInfo extends BaseEntity {

    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文件唯一标识（存储路径）
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
     * 存储类型（minio/oss）
     */
    private String storageType;

    /**
     * 访问 URL
     */
    private String fileUrl;

    /**
     * 文件 MD5（用于去重）
     */
    private String fileMd5;

    /**
     * 业务类型（avatar/product/order）
     */
    private String businessType;

    /**
     * 业务 ID
     */
    private Long businessId;

    @TableLogic(value = "0",delval = "now()")
    private Long deleted;    // 逻辑删除：0 未删除 删除之后变为时间戳
}