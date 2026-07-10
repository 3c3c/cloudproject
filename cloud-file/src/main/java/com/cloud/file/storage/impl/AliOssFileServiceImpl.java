package com.cloud.file.storage.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.OSSObject;
import com.cloud.file.config.FileStorageProperties;
import com.cloud.file.enums.StorageType;
import com.cloud.file.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;

/**
 * 阿里云 OSS 存储服务实现
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "file.storage.type", havingValue = "oss")
@RequiredArgsConstructor
public class AliOssFileServiceImpl implements FileStorageService, DisposableBean {

    private final FileStorageProperties fileStorageProperties;
    private final OSS ossClient;

    @Override
    public String upload(InputStream inputStream, String fileName,
                         String contentType, long fileSize) {
        try {
            ossClient.putObject(fileStorageProperties.getOss().getBucketName(), fileName, inputStream);
            log.info("File uploaded to OSS: {}", fileName);
            return fileName;
        } catch (Exception e) {
            log.error("Failed to upload file to OSS: {}", fileName, e);
            throw new RuntimeException("Upload failed", e);
        }
    }

    @Override
    public InputStream download(String fileKey) {
        try {
            OSSObject ossObject = ossClient.getObject(fileStorageProperties.getOss().getBucketName(), fileKey);
            return ossObject.getObjectContent();
        } catch (Exception e) {
            log.error("Failed to download file from OSS: {}", fileKey, e);
            throw new RuntimeException("Download failed", e);
        }
    }

    @Override
    public boolean delete(String fileKey) {
        try {
            ossClient.deleteObject(fileStorageProperties.getOss().getBucketName(), fileKey);
            log.info("File deleted from OSS: {}", fileKey);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete file from OSS: {}", fileKey, e);
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
            Date expiration = new Date(System.currentTimeMillis() + expireSeconds * 1000);
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
                    fileStorageProperties.getOss().getBucketName(),
                    fileKey
            );
            request.setExpiration(expiration);
            URL url = ossClient.generatePresignedUrl(request);
            return url.toString();
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for: {}", fileKey, e);
            throw new RuntimeException("Failed to generate presigned URL", e);
        }
    }

    @Override
    public boolean exists(String fileKey) {
        try {
            return ossClient.doesObjectExist(fileStorageProperties.getOss().getBucketName(), fileKey);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.OSS;
    }

    @Override
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }
}