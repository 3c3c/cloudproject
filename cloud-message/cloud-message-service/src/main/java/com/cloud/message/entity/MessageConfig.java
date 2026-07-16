package com.cloud.message.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloud.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息配置实体
 *
 * @author Cloud Team
 * @since 2025-01-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("message_config")
public class MessageConfig extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 配置键（唯一标识）
     */
    private String configKey;

    /**
     * 配置值（JSON 格式）
     */
    private String configValue;

    /**
     * 配置描述
     */
    private String description;

    @TableLogic(value = "0", delval = "now()")
    private Long deleted;
}
