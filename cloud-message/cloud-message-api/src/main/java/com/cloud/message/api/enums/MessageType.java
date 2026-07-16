package com.cloud.message.api.enums;

import lombok.Getter;

/**
 * 消息类型枚举
 *
 * @author Cloud Team
 * @since 2025-01-15
 */
@Getter
public enum MessageType {

    /**
     * 邮件
     */
    EMAIL(1, "邮件"),

    /**
     * WebSocket
     */
    WEBSOCKET(2, "WebSocket"),

    /**
     * 站内信
     */
    INBOX(3, "站内信");

    private final Integer code;
    private final String desc;

    MessageType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据 code 获取枚举
     */
    public static MessageType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (MessageType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
