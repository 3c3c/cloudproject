package com.cloud.message.api.feign;

import com.cloud.common.result.Result;
import com.cloud.message.api.dto.MessageSendRequest;
import com.cloud.message.api.dto.MessageSendResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 消息通知 Feign 客户端
 * 提供统一的消息发送接口，供其他服务通过 Feign 调用。
 *
 * <p>发送渠道由请求模板的消息类型决定，故对外只暴露一个 {@code POST /message/send}，
 * 不再按 email/inbox/websocket 拆分。</p>
 *
 * @author Cloud Team
 * @since 2025-01-15
 */
@FeignClient(contextId = "messageNotificationFeign", name = "cloud-message-service")
public interface MessageNotificationFeign {

    /**
     * 发送消息（渠道由模板类型决定）
     *
     * @param request 消息发送请求
     * @return 消息发送响应
     */
    @PostMapping("/message/send")
    Result<MessageSendResponse> send(@RequestBody MessageSendRequest request);
}
