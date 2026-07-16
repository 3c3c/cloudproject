package com.cloud.message.sender;

import com.cloud.message.entity.MessageRecord;
import com.cloud.message.api.enums.MessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.internet.MimeMessage;

/**
 * 邮件发送器
 *
 * @author Cloud Team
 * @since 2025-01-15
 */
@Slf4j
@Component
public class EmailSender implements MessageSender {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Override
    public boolean send(MessageRecord record) {
        try {
            log.info("开始发送邮件: to={}, subject={}", record.getReceiver(), record.getTitle());

            // 创建 MIME 消息
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // 设置发件人
            helper.setFrom(fromEmail);

            // 设置收件人（支持多个）
            String[] receivers = record.getReceiver().split(",");
            helper.setTo(receivers);

            // 设置主题和内容
            helper.setSubject(record.getTitle());
            helper.setText(record.getContent(), true); // HTML 格式

            // 发送邮件
            mailSender.send(mimeMessage);

            log.info("邮件发送成功: to={}, messageId={}", record.getReceiver(), record.getId());
            return true;

        } catch (Exception e) {
            log.error("邮件发送失败: to={}", record.getReceiver(), e);
            return false;
        }
    }

    @Override
    public MessageType getType() {
        return MessageType.EMAIL;
    }
}
