package com.cloud.admin.converter;

import com.cloud.admin.dto.DictTypeRequest;
import com.cloud.admin.dto.DictTypeResponse;
import com.cloud.admin.entity.SysDictType;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/**
 * 字典类型 MapStruct 转换器
 */
@Mapper(componentModel = "spring")
public interface DictTypeConverter {

    /**
     * 实体转响应DTO
     */
    DictTypeResponse toResponse(SysDictType entity);

    /**
     * 请求DTO转实体
     */
    SysDictType toEntity(DictTypeRequest request);

    /**
     * 更新实体（忽略null值）
     */
    void updateEntity(DictTypeRequest request, @MappingTarget SysDictType entity);
}
