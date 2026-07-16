package com.cloud.message.sender;

import com.cloud.message.api.enums.MessageType;
import com.cloud.message.entity.MessageRecord;
import com.cloud.message.websocket.MessageWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket 推送器
 *
 * @author Cloud Team
 * @since 2025-01-15
 */
@Slf4j
@Component
public class WebSocketSender implements MessageSender {

    @Autowired
    private MessageWebSocketHandler webSocketHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean send(MessageRecord record) {
        try {
            // 解析接收者（用户ID）
            Long userId = Long.parseLong(record.getReceiver());

            // 构建消息
            Map<String, Object> message = new HashMap<>();
            message.put("messageId", record.getId());
            message.put("title", record.getTitle());
            message.put("content", record.getContent());
            message.put("messageType", record.getMessageType());
            message.put("timestamp", record.getCreateTime());

            String messageJson = objectMapper.writeValueAsString(message);

            // 推送消息
            boolean success = webSocketHandler.pushToUser(userId, messageJson);

            if (success) {
                log.info("WebSocket 推送成功: userId={}, messageId={}", userId, record.getId());
            } else {
                log.warn("WebSocket 推送失败（用户离线）: userId={}, messageId={}", userId, record.getId());
            }

            return success;

        } catch (Exception e) {
            log.error("WebSocket 推送异常: receiver={}", record.getReceiver(), e);
            return false;
        }
    }

    @Override
    public MessageType getType() {
        return MessageType.WEBSOCKET;
    }
}
