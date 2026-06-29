package com.cloud.gateway.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * CORS 配置属性
 *
 * 从配置文件中读取 CORS 相关配置
 */
@Component
@ConfigurationProperties(prefix = "gateway.cors")
public class CorsProperties {

    /**
     * 允许的源列表
     */
    private List<String> allowedOrigins = new ArrayList<>();

    /**
     * 允许的 HTTP 方法列表
     */
    private List<String> allowedMethods = new ArrayList<>();

    /**
     * 允许的请求头列表
     */
    private List<String> allowedHeaders = new ArrayList<>();

    /**
     * 是否允许携带认证信息
     */
    private Boolean allowCredentials = true;

    /**
     * 预检请求的缓存时间（秒）
     */
    private Long maxAge = 3600L;

    /**
     * 暴露的响应头列表
     */
    private List<String> exposedHeaders = new ArrayList<>();

    // Getters and Setters
    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public List<String> getAllowedMethods() {
        return allowedMethods;
    }

    public void setAllowedMethods(List<String> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    public List<String> getAllowedHeaders() {
        return allowedHeaders;
    }

    public void setAllowedHeaders(List<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }

    public Boolean getAllowCredentials() {
        return allowCredentials;
    }

    public void setAllowCredentials(Boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    public Long getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Long maxAge) {
        this.maxAge = maxAge;
    }

    public List<String> getExposedHeaders() {
        return exposedHeaders;
    }

    public void setExposedHeaders(List<String> exposedHeaders) {
        this.exposedHeaders = exposedHeaders;
    }
}
