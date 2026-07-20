package com.cloud.common.jwt;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置。secret/expiration 等可由 Nacos 共享配置 cloud-common.yaml 覆盖。
 *
 * <p>安全要求：{@code jwt.secret} 必须显式配置，且不得使用公开的占位值。
 * 启动时会做 fail-fast 校验——若 secret 为空、仍是默认占位符、或不足 32 字节，
 * 应用将拒绝启动，避免在生产环境用一把公开密钥签发可伪造的 token。</p>
 */
@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** 历史默认占位符，视为"未配置"，出现即拒绝启动 */
    private static final String PLACEHOLDER_SECRET =
        "cloud-project-default-secret-key-please-change-in-nacos-32bytes";

    /** HS256 要求密钥 >= 32 字节 */
    private static final int MIN_SECRET_BYTES = 32;

    /**
     * 签名密钥（HS256 要求 >= 32 字节）。
     * <p>默认留空，强制通过 {@code jwt.secret}（如 Nacos cloud-common.yaml 或环境变量 JWT_SECRET）显式注入。</p>
     */
    private String secret = "";

    /** 过期时间，单位毫秒，默认 2 小时 */
    private Long expiration = 7_200_000L;

    /** 请求头名称 */
    private String header = "Authorization";

    /** token 前缀 */
    private String tokenPrefix = "Bearer ";

    /**
     * 启动时校验密钥配置，不合规则 fail-fast。
     *
     * <p>注意：本方法仅在依赖了 cloud-common-security 的服务（持有 JWT 能力）中触发，
     * 例如 cloud-auth / cloud-message；不依赖本模块的纯净服务不会受影响。</p>
     */
    @PostConstruct
    public void validate() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                "[JWT] jwt.secret 未配置。请在 Nacos cloud-common.yaml 或环境变量 JWT_SECRET 中设置一个不少于 32 字节的强随机密钥。");
        }
        if (PLACEHOLDER_SECRET.equals(secret)) {
            throw new IllegalStateException(
                "[JWT] jwt.secret 仍是公开的默认占位值，存在 token 被伪造的风险。请务必替换为自定义强密钥。");
        }
        if (secret.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                "[JWT] jwt.secret 长度不足 32 字节，不满足 HS256 要求。请使用更长的密钥。");
        }
        log.info("[JWT] 密钥配置校验通过。");
    }
}
