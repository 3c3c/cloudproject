package com.cloud.auth.converter;

import com.cloud.auth.dto.role.RoleRequest;
import com.cloud.auth.dto.role.RoleResponse;
import com.cloud.auth.entity.SysRole;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * 角色实体与DTO转换Converter
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface RoleConverter {

    /**
     * Request DTO转Entity
     */
    SysRole toEntity(RoleRequest request);

    /**
     * Entity转Response DTO
     */
    RoleResponse toResponse(SysRole role);

    /**
     * Entity列表转Response DTO列表
     */
    List<RoleResponse> toResponseList(List<SysRole> roles);
}