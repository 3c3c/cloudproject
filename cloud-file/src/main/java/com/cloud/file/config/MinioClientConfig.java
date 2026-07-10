package com.cloud.file.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 客户端配置
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "file.storage.type", havingValue = "minio", matchIfMissing = true)
public class MinioClientConfig {

    private final FileStorageProperties fileStorageProperties;

    @Bean
    public MinioClient minioClient() {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(fileStorageProperties.getMinio().getEndpoint())
                .credentials(fileStorageProperties.getMinio().getAccessKey(), fileStorageProperties.getMinio().getSecretKey())
                .build();

        try {
            // 检查并创建 Bucket
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(fileStorageProperties.getMinio().getBucketName())
                            .build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(fileStorageProperties.getMinio().getBucketName())
                                .build()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("MinIO创建桶失败", e);
        }
        return minioClient;
    }
}