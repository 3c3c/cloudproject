package com.cloud.message.converter;

import com.cloud.message.api.dto.MessageSendRequest;
import com.cloud.message.api.dto.MessageSendResponse;
import com.cloud.message.entity.MessageRecord;
import com.cloud.message.entity.MessageTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

/**
 * 消息对象转换器
 * 使用 MapStruct 实现对象转换
 *
 * @author Cloud Team
 * @since 2025-01-15
 */
@Mapper(componentModel = "spring")
public interface MessageConverter {

    MessageConverter INSTANCE = Mappers.getMapper(MessageConverter.class);

    /**
     * 消息记录转换为响应
     */
    @Mappings({
        @Mapping(target = "messageId", source = "id"),
        @Mapping(target = "success", expression = "java(entity.getStatus() == 2)"),
        @Mapping(target = "errorMessage", source = "errorMsg")
    })
    MessageSendResponse toResponse(MessageRecord entity);
}
