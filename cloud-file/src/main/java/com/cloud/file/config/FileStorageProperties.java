package com.cloud.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文件存储配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "file.storage")
public class FileStorageProperties {

    /**
     * 存储类型：minio 或 oss
     */
    private String type;

    /**
     * 文件最大大小（字节）
     */
    private Long maxSize; // 默认 10MB

    /**
     * 允许的文件扩展名（逗号分隔的字符串）
     */
    private String allowedExtensions;

    /**
     * 将逗号分隔的扩展名字符串转为 List
     * 如果后续需要遍历判断，可以用这个方法
     */
    public List<String> getAllowedExtensionList() {
        if (allowedExtensions == null || allowedExtensions.isEmpty()) {
            return List.of();
        }
        return List.of(allowedExtensions.split(","));
    }

    /**
     * 文件访问基础 URL
     */
    private String baseUrl;

    /**
     * MinIO 配置
     */
    private MinioConfig minio = new MinioConfig();

    /**
     * OSS 配置
     */
    private OssConfig oss = new OssConfig();

    @Data
    public static class MinioConfig {
        private String endpoint = "http://127.0.0.1:9000";
        private String accessKey = "minioadmin";
        private String secretKey = "minioadmin";
        private String bucketName = "cloud-files";
        private Integer connectTimeout = 10000;
        private Integer writeTimeout = 60000;
        private Integer readTimeout = 10000;
    }

    @Data
    public static class OssConfig {
        private String endpoint = "oss-cn-hangzhou.aliyuncs.com";
        private String accessKey;
        private String secretKey;
        private String bucketName = "cloud-files";
        private String region = "cn-hangzhou";
    }
}