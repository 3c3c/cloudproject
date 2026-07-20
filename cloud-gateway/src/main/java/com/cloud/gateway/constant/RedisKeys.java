package com.cloud.gateway.constant;

/**
 * 网关侧 Redis Key 定义。
 *
 * <p>必须与 {@code cloud-common-core} 的 {@code RedisConstants} 保持完全一致的 key 拼接规则，
 * 否则网关与 auth 服务读到的不是同一个 key，SSO / 黑名单会失效。
 * 由于网关是 WebFlux 不能依赖 servlet 版的 cloud-common，这里单独维护一份镜像常量。</p>
 */
public final class RedisKeys {

    private RedisKeys() {
    }

    /** 单点登录 / 强制下线：login:token:{username} -> 当前有效 token */
    public static final String LOGIN_TOKEN_PREFIX = "login:token:";

    /** 退出登录后失效：blacklist:{token} -> 1 */
    public static final String BLACKLIST_PREFIX = "blacklist:";

    /** 拼接单点登录 key */
    public static String loginTokenKey(String username) {
        return LOGIN_TOKEN_PREFIX + username;
    }

    /** 拼接黑名单 key */
    public static String blacklistKey(String token) {
        return BLACKLIST_PREFIX + token;
    }
}
