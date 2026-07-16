package com.cloud.admin.service;

import com.cloud.message.api.feign.MessageNotificationFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 系统通知服务
 * 通过 Feign 调用消息服务发送邮件
 *
 * @author Cloud Team
 * @since 2025-01-15
 */
@Slf4j
@Service
public class NotificationService {

    @Autowired
    private MessageNotificationFeign messageNotificationFeign;

    /**
     * 发送欢迎邮件
     *
     * @param email    邮箱地址
     * @param username 用户名
     */
    public void sendWelcomeEmail(String email, String username) {
        try {
            com.cloud.message.api.dto.MessageSendRequest request = new com.cloud.message.api.dto.MessageSendRequest();
            request.setTemplateCode("EMAIL_REGISTER_WELCOME");
            request.setReceiver(email);

            java.util.Map<String, Object> variables = new java.util.HashMap<>();
            variables.put("username", username);
            variables.put("registerTime", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            request.setVariables(variables);
            request.setSender("cloud-admin");

            messageNotificationFeign.send(request);
            log.info("欢迎邮件已发送: email={}, username={}", email, username);

        } catch (Exception e) {
            log.error("欢迎邮件发送失败: email={}", email, e);
            throw new RuntimeException("欢迎邮件发送失败", e);
        }
    }
}
