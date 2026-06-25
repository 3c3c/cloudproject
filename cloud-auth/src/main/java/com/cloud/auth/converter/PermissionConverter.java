package com.cloud.auth.converter;

import com.cloud.auth.dto.permission.PermissionRequest;
import com.cloud.auth.dto.permission.PermissionResponse;
import com.cloud.auth.entity.SysPermission;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * 权限实体与DTO转换Converter
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PermissionConverter {

    /**
     * Request DTO转Entity
     */
    SysPermission toEntity(PermissionRequest request);

    /**
     * Entity转Response DTO
     */
    PermissionResponse toResponse(SysPermission permission);

    /**
     * Entity列表转Response DTO列表
     */
    List<PermissionResponse> toResponseList(List<SysPermission> permissions);
}