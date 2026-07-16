package com.cloud.message.api.enums;

import lombok.Getter;

/**
 * 消息状态枚举
 *
 * @author Cloud Team
 * @since 2025-01-15
 */
@Getter
public enum MessageStatus {

    /**
     * 待发送
     */
    PENDING(0, "待发送"),

    /**
     * 发送中
     */
    SENDING(1, "发送中"),

    /**
     * 发送成功
     */
    SUCCESS(2, "发送成功"),

    /**
     * 发送失败
     */
    FAILED(3, "发送失败");

    private final Integer code;
    private final String desc;

    MessageStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据 code 获取枚举
     */
    public static MessageStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (MessageStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
