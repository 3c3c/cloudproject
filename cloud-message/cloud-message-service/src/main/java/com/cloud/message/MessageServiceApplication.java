package com.cloud.message;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * 消息服务启动类
 *
 * @author Cloud Team
 * @since 2025-01-15
 */
@SpringBootApplication(scanBasePackages = {"com.cloud.message", "com.cloud.common"})
@EnableDiscoveryClient
@EnableKafka
public class MessageServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MessageServiceApplication.class, args);
    }
}
