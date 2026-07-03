package com.cloud.admin.converter;

import com.cloud.admin.dto.DictDataRequest;
import com.cloud.admin.dto.DictDataResponse;
import com.cloud.admin.entity.SysDictData;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/**
 * 字典数据 MapStruct 转换器
 */
@Mapper(componentModel = "spring")
public interface DictDataConverter {

    /**
     * 实体转响应DTO
     */
    DictDataResponse toResponse(SysDictData entity);

    /**
     * 请求DTO转实体
     */
    SysDictData toEntity(DictDataRequest request);

    /**
     * 更新实体（忽略null值）
     */
    void updateEntity(DictDataRequest request, @MappingTarget SysDictData entity);
}
