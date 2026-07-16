package com.cloud.message.service;

/**
 * 消息服务接口
 *
 * @author Cloud Team
 * @since 2025-01-15
 */
public interface MessageService {

    /**
     * 处理消息（从 Kafka 接收）
     *
     * @param messageJson 消息 JSON 字符串
     */
    void processMessage(String messageJson);

    /**
     * 发送消息（同步）
     *
     * @param templateCode 模板编码
     * @param receiver     接收者
     * @param variables    模板变量
     * @return 消息ID
     */
    Long sendMessage(String templateCode, String receiver, java.util.Map<String, Object> variables);
}
