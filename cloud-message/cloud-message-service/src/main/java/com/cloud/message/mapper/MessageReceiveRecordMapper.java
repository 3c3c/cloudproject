package com.cloud.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.message.entity.MessageReceiveRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息接收记录 Mapper（站内信）
 *
 * @author Cloud Team
 * @since 2025-01-15
 */
@Mapper
public interface MessageReceiveRecordMapper extends BaseMapper<MessageReceiveRecord> {
}
