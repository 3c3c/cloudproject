package com.cloud.message.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloud.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息模板实体
 *
 * @author Cloud Team
 * @since 2025-01-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("message_template")
public class MessageTemplate extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 模板编码（唯一标识）
     */
    private String templateCode;

    /**
     * 消息类型：1-短信 2-邮件 3-WebSocket 4-站内信
     */
    private Integer messageType;

    /**
     * 渠道：aliyun/tencent/email/websocket/inbox
     */
    private String channel;

    /**
     * 标题（邮件/站内信使用）
     */
    private String title;

    /**
     * 模板内容（支持变量：${code}、${expireTime}等）
     */
    private String content;

    /**
     * 模板变量定义（JSON 格式）
     */
    private String variables;

    /**
     * 状态：1-启用 0-禁用
     */
    private Integer status;

    @TableLogic(value = "0", delval = "now()")
    private Long deleted;
}
