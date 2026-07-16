package com.cloud.common.constant;

import java.util.concurrent.TimeUnit;

/**
 * Redis Key 设计（对齐 aaa.md 第五节）。
 *
 * <pre>
 * login:token:{username}  -> token   单点登录 / 强制下线
 * blacklist:{token}       -> 1       退出登录后失效
 * sms:code:{mobile}       -> code    短信验证码
 * sms:rate:{mobile}       -> 1       短信发送频控
 * </pre>
 */
public final class RedisConstants {

    private RedisConstants() {
    }

    public static final String LOGIN_TOKEN_PREFIX = "login:token:";
    public static final String USER_PERMISSION_PREFIX = "user:permission:";
    public static final String BLACKLIST_PREFIX = "blacklist:";
    public static final String SMS_CODE_PREFIX = "sms:code:";
    public static final String SMS_RATE_PREFIX = "sms:rate:";

    /** 验证码有效期：5 分钟 */
    public static final long SMS_CODE_TTL = 5;
    /** 验证码发送频控：60 秒 */
    public static final long SMS_RATE_TTL = 60;

    public static final TimeUnit SMS_CODE_UNIT = TimeUnit.MINUTES;
    public static final TimeUnit SMS_RATE_UNIT = TimeUnit.SECONDS;

    public static String loginTokenKey(String username) {
        return LOGIN_TOKEN_PREFIX + username;
    }

    public static String userPermissionKey(String username) {
        return USER_PERMISSION_PREFIX + username;
    }

    public static String blacklistKey(String token) {
        return BLACKLIST_PREFIX + token;
    }

    public static String smsCodeKey(String mobile) {
        return SMS_CODE_PREFIX + mobile;
    }

    public static String smsRateKey(String mobile) {
        return SMS_RATE_PREFIX + mobile;
    }
}
