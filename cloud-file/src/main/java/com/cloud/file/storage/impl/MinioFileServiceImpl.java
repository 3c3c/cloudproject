package com.cloud.file.storage.impl;

import com.cloud.file.config.FileStorageProperties;
import com.cloud.file.storage.FileStorageService;
import com.cloud.file.enums.StorageType;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * MinIO 存储服务实现
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "file.storage.type", havingValue = "minio", matchIfMissing = true)
@RequiredArgsConstructor
public class MinioFileServiceImpl implements FileStorageService {

    private final FileStorageProperties fileStorageProperties;
    private final MinioClient minioClient;

    @Override
    public String upload(InputStream inputStream, String fileName,
                         String contentType, long fileSize) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(fileStorageProperties.getMinio().getBucketName())
                            .object(fileName)
                            .stream(inputStream, fileSize, -1)
                            .contentType(contentType)
                            .build()
            );
            log.info("File uploaded to MinIO: {}", fileName);
            return fileName;
        } catch (Exception e) {
            log.error("Failed to upload file to MinIO: {}", fileName, e);
            throw new RuntimeException("Upload failed", e);
        }
    }

    @Override
    public InputStream download(String fileKey) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(fileStorageProperties.getMinio().getBucketName())
                            .object(fileKey)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to download file from MinIO: {}", fileKey, e);
            throw new RuntimeException("Download failed", e);
        }
    }

    @Override
    public boolean delete(String fileKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(fileStorageProperties.getMinio().getBucketName())
                            .object(fileKey)
                            .build()
            );
            log.info("File deleted from MinIO: {}", fileKey);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete file from MinIO: {}", fileKey, e);
            return false;
        }
    }

    @Override
    public int batchDelete(List<String> fileKeys) {
        int successCount = 0;
        for (String fileKey : fileKeys) {
            if (delete(fileKey)) {
                successCount++;
            }
        }
        return successCount;
    }

    @Override
    public String getPresignedUrl(String fileKey, Integer expireSeconds) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(fileStorageProperties.getMinio().getBucketName())
                            .object(fileKey)
                            .expiry(expireSeconds, TimeUnit.SECONDS)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for: {}", fileKey, e);
            throw new RuntimeException("Failed to generate presigned URL", e);
        }
    }

    @Override
    public boolean exists(String fileKey) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(fileStorageProperties.getMinio().getBucketName())
                            .object(fileKey)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.MINIO;
    }
}