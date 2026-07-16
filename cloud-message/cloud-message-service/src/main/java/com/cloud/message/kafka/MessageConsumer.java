package com.cloud.message.kafka;

import com.cloud.message.api.constant.KafkaConstants;
import com.cloud.message.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Kafka 消息消费者
 * 监听 cloud-message-send topic，处理消息发送
 *
 * <p>失败处理：由 {@link com.cloud.message.config.KafkaConsumerConfig} 中配置的
 * {@code DefaultErrorHandler} 统一负责——消费失败时间隔重试 3 次，仍失败则投递到死信队列 (DLT)，
 * 因此这里正常处理完成后 ack，异常直接抛出交给错误处理器，不再手动决定是否重试。</p>
 *
 * @author Cloud Team
 * @since 2025-01-15
 */
@Slf4j
@Component
public class MessageConsumer {

    @Autowired
    private MessageService messageService;

    /**
     * 监听普通消息发送
     * <p>抛出异常会触发 DefaultErrorHandler 的重试与死信策略，故此处不吞异常。</p>
     */
    @KafkaListener(
        topics = KafkaConstants.TOPIC_MESSAGE_SEND,
        groupId = KafkaConstants.GROUP_MESSAGE_SERVICE,
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeMessageSend(ConsumerRecord<String, String> record, Acknowledgment ack) {
        log.info("收到消息: topic={}, partition={}, offset={}",
            record.topic(), record.partition(), record.offset());

        String messageJson = record.value();
        log.debug("消息内容: {}", messageJson);

        // 处理消息（异常直接抛出，交给 DefaultErrorHandler 重试/死信）
        messageService.processMessage(messageJson);

        // 处理成功才提交 offset
        ack.acknowledge();
        log.info("消息处理完成并提交 offset: offset={}", record.offset());
    }
}
