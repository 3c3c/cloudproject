package com.cloud.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.message.entity.MessageRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息记录 Mapper
 *
 * @author Cloud Team
 * @since 2025-01-15
 */
@Mapper
public interface MessageRecordMapper extends BaseMapper<MessageRecord> {
}
