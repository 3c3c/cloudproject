package com.cloud.message.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 消息发送请求 DTO
 *
 * @author Cloud Team
 * @since 2025-01-15
 */
@Data
public class MessageSendRequest {

    /**
     * 模板编码
     */
    @NotBlank(message = "模板编码不能为空")
    private String templateCode;

    /**
     * 接收者（手机号/邮箱/用户ID）
     */
    @NotBlank(message = "接收者不能为空")
    private String receiver;

    /**
     * 模板变量
     */
    @NotNull(message = "模板变量不能为空")
    private Map<String, Object> variables;

    /**
     * 发送者（可选，默认为 system）
     */
    private String sender;
}
