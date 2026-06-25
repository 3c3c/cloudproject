package com.cloud.admin.converter;

import com.cloud.admin.dto.DictRequest;
import com.cloud.admin.dto.DictResponse;
import com.cloud.admin.entity.SysDict;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * 字典实体与DTO转换Converter
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface DictConverter {

    /**
     * Request DTO转Entity
     * 审计字段(id, createTime, updateTime, createdBy, updatedBy)由数据库和MyBatis Plus自动处理
     */
    SysDict toEntity(DictRequest request);

    /**
     * Entity转Response DTO
     */
    DictResponse toResponse(SysDict dict);

    /**
     * Entity列表转Response DTO列表
     */
    List<DictResponse> toResponseList(List<SysDict> dicts);
}