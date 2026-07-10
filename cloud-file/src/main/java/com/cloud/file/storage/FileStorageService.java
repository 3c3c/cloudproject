package com.cloud.file.storage;

import com.cloud.file.enums.StorageType;

import java.io.InputStream;
import java.util.List;

/**
 * 文件存储服务接口
 * 策略模式：支持多种存储实现（MinIO、OSS等）
 */
public interface FileStorageService {

    /**
     * 上传文件
     *
     * @param inputStream 文件输入流
     * @param fileName    文件名（包含路径）
     * @param contentType MIME 类型
     * @param fileSize    文件大小
     * @return fileKey 文件唯一标识
     */
    String upload(InputStream inputStream, String fileName, String contentType, long fileSize);

    /**
     * 下载文件
     *
     * @param fileKey 文件唯一标识
     * @return 文件输入流
     */
    InputStream download(String fileKey);

    /**
     * 删除文件
     *
     * @param fileKey 文件唯一标识
     * @return 是否成功
     */
    boolean delete(String fileKey);

    /**
     * 批量删除文件
     *
     * @param fileKeys 文件唯一标识列表
     * @return 成功删除的数量
     */
    int batchDelete(List<String> fileKeys);

    /**
     * 获取临时访问 URL
     *
     * @param fileKey       文件唯一标识
     * @param expireSeconds 过期时间（秒）
     * @return 临时访问 URL
     */
    String getPresignedUrl(String fileKey, Integer expireSeconds);

    /**
     * 检查文件是否存在
     *
     * @param fileKey 文件唯一标识
     * @return 是否存在
     */
    boolean exists(String fileKey);

    /**
     * 获取存储类型
     *
     * @return 存储类型枚举
     */
    StorageType getStorageType();
}