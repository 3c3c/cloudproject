package com.cloud.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloud.message.api.dto.MessageSendRequest;
import com.cloud.message.api.enums.MessageStatus;
import com.cloud.message.api.enums.MessageType;
import com.cloud.message.entity.MessageRecord;
import com.cloud.message.entity.MessageTemplate;
import com.cloud.message.mapper.MessageRecordMapper;
import com.cloud.message.mapper.MessageTemplateMapper;
import com.cloud.message.sender.MessageSender;
import com.cloud.message.service.MessageService;
import com.cloud.message.service.MessageTemplateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 消息服务核心实现
 * 参考：现有项目的服务层实现模式
 *
 * @author Cloud Team
 * @since 2025-01-15
 */
@Slf4j
@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageTemplateMapper templateMapper;

    @Autowired
    private MessageRecordMapper recordMapper;

    @Autowired
    private MessageTemplateService templateService;

    /**
     * 所有消息发送器实现
     * Spring 只支持以 String（bean 名）作为 Map 的 key 注入，
     * 用枚举作 key 会得到空 Map。这里注入 List 后在 @PostConstruct 中按 getType() 组装。
     */
    @Autowired
    private List<MessageSender> senders;

    /**
     * MessageType → MessageSender 的映射
     */
    private Map<MessageType, MessageSender> senderMap;

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void initSenderMap() {
        senderMap = senders.stream()
            .collect(Collectors.toMap(MessageSender::getType, s -> s));
        log.info("消息发送器初始化完成: {}", senderMap.keySet());
    }

    @Override
    public void processMessage(String messageJson) {
        log.info("开始处理消息: {}", messageJson);

        // 1. 解析消息
        MessageSendRequest request;
        try {
            request = objectMapper.readValue(messageJson, MessageSendRequest.class);
        } catch (Exception e) {
            // 解析失败的坏消息，无法进入死信外的任何恢复流程，直接抛出触发 DefaultErrorHandler 的重试/死信
            log.error("消息解析失败: {}", messageJson, e);
            throw new RuntimeException("消息解析失败", e);
        }
        log.info("消息解析成功: templateCode={}, receiver={}", request.getTemplateCode(), request.getReceiver());

        // 2. 事务内：查模板 + 渲染 + 落库（PENDING）。事务不包含任何外部网络调用。
        MessageRecord record = persistRecord(request);
        log.info("消息记录已保存: messageId={}", record.getId());

        // 3. 事务提交后再执行真正的发送，避免网络 IO 长占数据库连接，
        //    也避免“已发出但事务回滚”的数据不一致。
        //    若当前不存在事务（例如被 sendMessage 直接调用），则立即发送。
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    doSend(record);
                }
            });
        } else {
            doSend(record);
        }
    }

    @Override
    public Long sendMessage(String templateCode, String receiver, Map<String, Object> variables) {
        // 构建请求
        MessageSendRequest request = new MessageSendRequest();
        request.setTemplateCode(templateCode);
        request.setReceiver(receiver);
        request.setVariables(variables);
        request.setSender("system");

        // 复用统一流程：事务内落库 → 提交后发送
        MessageRecord record = persistRecord(request);
        // sendMessage 由非事务上下文调用时，persistRecord 的事务已在方法返回前提交，
        // 此时直接发送即可（无需注册 afterCommit）。
        doSend(record);
        return record.getId();
    }

    /**
     * 事务内完成：查询模板 → 渲染 → 落库消息记录（PENDING）。
     * <p>刻意不在此事务内调用任何外部发送（SMTP/WS），保证数据库连接不被网络 IO 占用，
     * 并避免“消息已发但记录回滚”的不一致。</p>
     */
    @Transactional(rollbackFor = Exception.class)
    public MessageRecord persistRecord(MessageSendRequest request) {
        // 获取模板
        MessageTemplate template = getTemplate(request.getTemplateCode());
        if (template == null) {
            throw new RuntimeException("模板不存在或已禁用: " + request.getTemplateCode());
        }

        // 渲染模板内容（直接复用已查到的 template 对象，避免重复查库）
        String renderedContent = templateService.render(template, request.getVariables());

        // 保存消息记录
        MessageRecord record = buildMessageRecord(template, renderedContent, request);
        recordMapper.insert(record);
        return record;
    }

    /**
     * 执行真正的发送（事务提交后调用）：选择发送器 → 置 SENDING → 发送 → 置最终状态。
     * <p>状态更新使用独立的 updateById，不依赖外层事务。</p>
     */
    private void doSend(MessageRecord record) {
        // 选择发送器
        MessageType messageType = MessageType.fromCode(record.getMessageType());
        if (messageType == null) {
            log.error("不支持的消息类型: {}, messageId={}", record.getMessageType(), record.getId());
            updateMessageStatus(record.getId(), MessageStatus.FAILED, "不支持的消息类型: " + record.getMessageType());
            return;
        }
        MessageSender sender = senderMap.get(messageType);
        if (sender == null) {
            log.error("未注册的发送类型: {}, messageId={}", messageType, record.getId());
            updateMessageStatus(record.getId(), MessageStatus.FAILED, "未注册的发送类型: " + messageType);
            return;
        }

        // 更新状态为发送中
        updateMessageStatus(record.getId(), MessageStatus.SENDING, null);

        // 发送消息
        boolean success;
        try {
            success = sender.send(record);
        } catch (Exception e) {
            log.error("消息发送异常: messageId={}", record.getId(), e);
            success = false;
        }
        log.info("消息发送完成: messageId={}, success={}", record.getId(), success);

        // 更新最终状态
        updateMessageStatus(record.getId(), success ? MessageStatus.SUCCESS : MessageStatus.FAILED,
            success ? null : "发送失败");
    }

    /**
     * 获取启用的模板
     */
    private MessageTemplate getTemplate(String templateCode) {
        LambdaQueryWrapper<MessageTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MessageTemplate::getTemplateCode, templateCode);
        wrapper.eq(MessageTemplate::getStatus, 1); // 只查询启用的模板
        return templateMapper.selectOne(wrapper);
    }

    /**
     * 构建消息记录
     */
    private MessageRecord buildMessageRecord(MessageTemplate template, String content, MessageSendRequest request) {
        MessageRecord record = new MessageRecord();
        record.setTemplateId(template.getId());
        record.setMessageType(template.getMessageType());
        record.setTitle(template.getTitle());
        record.setContent(content);
        record.setReceiver(request.getReceiver());
        record.setSender(request.getSender() != null ? request.getSender() : "system");
        record.setStatus(MessageStatus.PENDING.getCode());
        record.setRetryCount(0);
        // createTime 和 updateTime 由 MyBatis-Plus 自动填充
        return record;
    }

    /**
     * 更新消息状态
     */
    private void updateMessageStatus(Long messageId, MessageStatus status, String errorMsg) {
        MessageRecord record = new MessageRecord();
        record.setId(messageId);
        record.setStatus(status.getCode());
        // updateTime 由 MyBatis-Plus 自动填充
        if (status == MessageStatus.SUCCESS) {
            record.setSendTime(System.currentTimeMillis());
        }
        if (errorMsg != null) {
            record.setErrorMsg(errorMsg);
        }
        recordMapper.updateById(record);
    }
}
