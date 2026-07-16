package com.cloud.message.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloud.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息记录实体
 *
 * @author Cloud Team
 * @since 2025-01-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("message_record")
public class MessageRecord extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 模板ID
     */
    private Long templateId;

    /**
     * 消息类型：1-短信 2-邮件 3-WebSocket 4-站内信
     */
    private Integer messageType;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 接收者（手机号/邮箱/用户ID，多个用逗号分隔）
     */
    private String receiver;

    /**
     * 发送者
     */
    private String sender;

    /**
     * 状态：0-待发送 1-发送中 2-成功 3-失败
     */
    private Integer status;

    /**
     * 发送时间（毫秒时间戳）
     */
    private Long sendTime;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 重试次数
     */
    private Integer retryCount;

    @TableLogic(value = "0", delval = "now()")
    private Long deleted;
}
