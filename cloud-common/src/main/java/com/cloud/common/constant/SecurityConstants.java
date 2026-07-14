package com.cloud.common.constant;

/**
 * 网关与业务服务之间约定的请求头（用户信息透传），以及权限相关常量。
 */
public final class SecurityConstants {

    private SecurityConstants() {
    }

    /** 请求头：用户 ID（网关解析后透传给下游） */
    public static final String HEADER_USER_ID = "X-User-Id";
    /** 请求头：用户名 */
    public static final String HEADER_USERNAME = "X-Username";
    /** 请求头：权限集合（逗号分隔） */
    public static final String HEADER_AUTHORITIES = "X-User-Authorities";

    public static final String AUTH_HEADER = "Authorization";
}
