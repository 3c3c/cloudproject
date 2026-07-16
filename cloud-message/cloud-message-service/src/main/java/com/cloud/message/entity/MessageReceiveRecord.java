package com.cloud.message.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloud.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息接收记录实体（站内信）
 *
 * @author Cloud Team
 * @since 2025-01-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("message_receive_record")
public class MessageReceiveRecord extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 消息ID（关联 message_record）
     */
    private Long messageId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 阅读状态：0-未读 1-已读
     */
    private Integer readStatus;

    /**
     * 阅读时间（毫秒时间戳）
     */
    private Long readTime;

    @TableLogic(value = "0", delval = "now()")
    private Long deleted;
}
