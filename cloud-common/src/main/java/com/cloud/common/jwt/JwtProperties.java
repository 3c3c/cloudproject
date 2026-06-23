package com.cloud.common.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置。secret/expiration 等可由 Nacos 共享配置 cloud-common.yaml 覆盖。
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** 签名密钥（HS256 要求 >= 32 字节） */
    private String secret = "cloud-project-default-secret-key-please-change-in-nacos-32bytes";

    /** 过期时间，单位毫秒，默认 2 小时 */
    private Long expiration = 7_200_000L;

    /** 请求头名称 */
    private String header = "Authorization";

    /** token 前缀 */
    private String tokenPrefix = "Bearer ";
}
