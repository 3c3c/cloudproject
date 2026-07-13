package com.cloud.file.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云OSS客户端配置
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "file.storage.type", havingValue = "oss")
public class AliOssClientConfig {

    private final FileStorageProperties fileStorageProperties;

    @Bean
    public OSS ossClient() {
        OSS ossClient = new OSSClientBuilder().build(
                fileStorageProperties.getOss().getEndpoint(),
                fileStorageProperties.getOss().getAccessKey(),
                fileStorageProperties.getOss().getSecretKey()
        );
        try {
            // 检查并创建 Bucket
            boolean exists = ossClient.doesBucketExist(fileStorageProperties.getOss().getBucketName());
            if (!exists) {
                ossClient.createBucket(fileStorageProperties.getOss().getBucketName());
            }
        } catch (Exception e) {
            throw new RuntimeException("OSS创建桶失败", e);
        }
        return ossClient;
    }

}