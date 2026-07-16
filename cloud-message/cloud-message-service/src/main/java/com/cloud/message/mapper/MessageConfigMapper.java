package com.cloud.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.message.entity.MessageConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息配置 Mapper
 *
 * @author Cloud Team
 * @since 2025-01-15
 */
@Mapper
public interface MessageConfigMapper extends BaseMapper<MessageConfig> {
}
