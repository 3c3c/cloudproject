package com.cloud.message.sender;

import com.cloud.message.entity.MessageRecord;

/**
 * 消息发送器接口
 * 统一的消息发送抽象，支持多种实现
 *
 * @author Cloud Team
 * @since 2025-01-15
 */
public interface MessageSender {

    /**
     * 发送消息
     *
     * @param record 消息记录
     * @return 是否发送成功
     */
    boolean send(MessageRecord record);

    /**
     * 获取发送器类型
     *
     * @return 消息类型
     */
    com.cloud.message.api.enums.MessageType getType();
}
