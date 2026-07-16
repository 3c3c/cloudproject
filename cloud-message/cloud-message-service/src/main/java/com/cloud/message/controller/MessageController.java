package com.cloud.message.controller;

import com.cloud.common.result.Result;
import com.cloud.message.api.dto.MessageSendRequest;
import com.cloud.message.api.dto.MessageSendResponse;
import com.cloud.message.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 消息控制器
 * 提供统一的消息发送 REST 接口。
 *
 * <p>发送渠道由模板的 messageType 决定（service 层按 MessageType 路由到对应的 MessageSender），
 * 因此对外只暴露一个 {@code POST /message/send}，不再按渠道拆分接口。</p>
 *
 * @author Cloud Team
 * @since 2025-01-15
 */
@Slf4j
@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    /**
     * 统一发送消息。
     */
    @PostMapping("/send")
    public Result<MessageSendResponse> send(@Validated @RequestBody MessageSendRequest request) {
        try {
            Long messageId = messageService.sendMessage(
                request.getTemplateCode(),
                request.getReceiver(),
                request.getVariables()
            );

            MessageSendResponse response = MessageSendResponse.builder()
                .messageId(messageId)
                .success(true)
                .message("消息发送成功")
                .build();

            return Result.success(response);

        } catch (Exception e) {
            log.error("消息发送失败: templateCode={}, receiver={}", request.getTemplateCode(), request.getReceiver(), e);
            MessageSendResponse response = MessageSendResponse.builder()
                .success(false)
                .errorMessage(e.getMessage())
                .message("消息发送失败")
                .build();
            // 失败时把详细响应通过 data 返回，便于调用方定位
            return new Result<>(com.cloud.common.result.ResultCode.INTERNAL_ERROR.getCode(),
                response.getMessage(), response);
        }
    }
}
