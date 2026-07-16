package com.cloud.message.sender;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloud.message.api.enums.MessageType;
import com.cloud.message.entity.MessageRecord;
import com.cloud.message.entity.MessageReceiveRecord;
import com.cloud.message.mapper.MessageReceiveRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 站内信发送器
 *
 * @author Cloud Team
 * @since 2025-01-15
 */
@Slf4j
@Component
public class InboxSender implements MessageSender {

    @Autowired
    private MessageReceiveRecordMapper receiveRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean send(MessageRecord record) {
        try {
            // 解析接收者（支持多个用户ID，用逗号分隔）
            String[] receivers = record.getReceiver().split(",");

            for (String receiver : receivers) {
                try {
                    Long userId = Long.parseLong(receiver.trim());

                    // 检查是否已经存在接收记录
                    LambdaQueryWrapper<MessageReceiveRecord> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(MessageReceiveRecord::getMessageId, record.getId());
                    wrapper.eq(MessageReceiveRecord::getUserId, userId);
                    MessageReceiveRecord existRecord = receiveRecordMapper.selectOne(wrapper);

                    if (existRecord == null) {
                        // 创建接收记录
                        MessageReceiveRecord receiveRecord = new MessageReceiveRecord();
                        receiveRecord.setMessageId(record.getId());
                        receiveRecord.setUserId(userId);
                        receiveRecord.setReadStatus(0); // 未读
                        // createTime 和 updateTime 由 MyBatis-Plus 自动填充

                        receiveRecordMapper.insert(receiveRecord);
                        log.info("站内信已保存: userId={}, messageId={}", userId, record.getId());
                    } else {
                        log.info("站内信已存在: userId={}, messageId={}", userId, record.getId());
                    }

                } catch (NumberFormatException e) {
                    log.warn("无效的用户ID: {}", receiver);
                }
            }

            log.info("站内信发送完成: messageId={}, receivers={}", record.getId(), record.getReceiver());
            return true;

        } catch (Exception e) {
            log.error("站内信发送失败: messageId={}", record.getId(), e);
            return false;
        }
    }

    @Override
    public MessageType getType() {
        return MessageType.INBOX;
    }
}
