package com.cloud.message.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息发送响应 DTO
 *
 * @author Cloud Team
 * @since 2025-01-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSendResponse {

    /**
     * 消息ID
     */
    private Long messageId;

    /**
     * 是否发送成功
     */
    private Boolean success;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 响应消息
     */
    private String message;
}
