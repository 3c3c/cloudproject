package com.cloud.message.api.constant;

/**
 * Kafka 常量定义
 * 参考现有项目的 RedisConstants 模式
 *
 * @author Cloud Team
 * @since 2025-01-15
 */
public final class KafkaConstants {

    private KafkaConstants() {
    }

    /** Topic 命名前缀 */
    public static final String TOPIC_PREFIX = "cloud-message";

    /** Topic：普通消息发送 */
    public static final String TOPIC_MESSAGE_SEND = TOPIC_PREFIX + "-send";

    /** Topic：失败消息重试 */
    public static final String TOPIC_MESSAGE_RETRY = TOPIC_PREFIX + "-retry";

    /** Topic：死信消息存储 */
    public static final String TOPIC_MESSAGE_DEAD = TOPIC_PREFIX + "-dead";

    /** 消费者组前缀 */
    public static final String CONSUMER_GROUP_PREFIX = "cloud-message-consumer";

    /** 消费者组：消息服务 */
    public static final String GROUP_MESSAGE_SERVICE = CONSUMER_GROUP_PREFIX + "-service";
}
