package com.cloud.message.api.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka 生产者工具类
 * 提供统一的消息发送接口，供其他服务使用
 * 参考：cloud-common 的 RestTemplate 模式
 *
 * @author Cloud Team
 * @since 2025-01-15
 */
@Slf4j
@Component
public class KafkaProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 发送消息（异步）
     *
     * @param topic Topic 名称
     * @param key   消息键（用于分区选择）
     * @param value 消息内容（JSON 字符串）
     */
    public void sendAsync(String topic, String key, String value) {
        try {
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, value);
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Kafka消息发送失败: topic={}, key={}, value={}", topic, key, value, ex);
                } else {
                    if (result != null && result.getRecordMetadata() != null) {
                        log.info("Kafka消息发送成功: topic={}, partition={}, offset={}, key={}",
                            topic,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset(),
                            key
                        );
                    }
                }
            });
        } catch (Exception e) {
            log.error("Kafka消息发送异常: topic={}, key={}", topic, key, e);
        }
    }

    /**
     * 发送消息（同步，带重试）
     *
     * @param topic Topic 名称
     * @param key   消息键（用于分区选择）
     * @param value 消息内容（JSON 字符串）
     * @return 是否发送成功
     */
    public boolean sendSync(String topic, String key, String value) {
        try {
            SendResult<String, String> result = kafkaTemplate.send(topic, key, value).get();
            if (result != null && result.getRecordMetadata() != null) {
                log.info("Kafka消息发送成功: topic={}, partition={}, offset={}, key={}",
                    topic,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset(),
                    key
                );
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Kafka消息发送失败: topic={}, key={}", topic, key, e);
            return false;
        }
    }

    /**
     * 发送消息到默认 Topic（异步）
     *
     * @param key   消息键
     * @param value 消息内容
     */
    public void send(String key, String value) {
        sendAsync(com.cloud.message.api.constant.KafkaConstants.TOPIC_MESSAGE_SEND, key, value);
    }
}
