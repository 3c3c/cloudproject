package com.cloud.common.result;

import lombok.Getter;

/**
 * 统一响应码
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证或认证已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    USERNAME_OR_PASSWORD_ERROR(1001, "用户名或密码错误"),
    ACCOUNT_DISABLED(1002, "账号已被禁用"),
    TOKEN_INVALID(1003, "Token 无效"),
    TOKEN_EXPIRED(1004, "Token 已过期"),
    TOKEN_KICKED(1005, "Token 已失效或在别处登录"),
    SMS_CODE_ERROR(1006, "验证码错误或已过期"),
    SMS_CODE_RATE_LIMIT(1007, "验证码发送过于频繁"),
    FORBIDDEN_OPERATION(1008, "无权执行该操作");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
