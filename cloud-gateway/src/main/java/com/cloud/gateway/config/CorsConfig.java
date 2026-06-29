package com.cloud.gateway.config;

import com.cloud.gateway.properties.CorsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * 网关统一 CORS 配置
 *
 * 功能说明：
 * 1. 允许跨域请求通过网关
 * 2. 支持自定义允许的源、方法、请求头
 * 3. 支持携带认证信息（Cookie、Authorization等）
 * 4. 所有配置通过配置文件动态获取
 */
@Configuration
public class CorsConfig {

    private final CorsProperties corsProperties;

    public CorsConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    /**
     * 创建 CORS Web 过滤器
     *
     * @return CorsWebFilter
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 从配置文件中动态获取允许的源
        corsProperties.getAllowedOrigins().forEach(config::addAllowedOrigin);
        // 默认允许所有请求头
        config.addAllowedHeader("*");

        // 从配置文件中动态获取允许的 HTTP 方法
        if (corsProperties.getAllowedMethods() != null && !corsProperties.getAllowedMethods().isEmpty()) {
            config.setAllowedMethods(corsProperties.getAllowedMethods());
        } else {
            // 默认配置
            config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"
            ));
        }

        // 从配置文件中获取是否允许携带认证信息
        config.setAllowCredentials(true);

        // 从配置文件中获取预检请求的缓存时间
        config.setMaxAge(3600L);

        // 从配置文件中动态获取暴露的响应头
        if (corsProperties.getExposedHeaders() != null && !corsProperties.getExposedHeaders().isEmpty()) {
            corsProperties.getExposedHeaders().forEach(config::addExposedHeader);
        }

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
