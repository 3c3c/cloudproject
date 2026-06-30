package com.cloud.auth.converter;

import com.cloud.auth.dto.user.UserInfoRequest;
import com.cloud.auth.dto.user.UserRequest;
import com.cloud.auth.dto.user.UserResponse;
import com.cloud.auth.entity.SysUser;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * 用户实体与DTO转换Converter
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserConverter {

    /**
     * Request DTO转Entity（创建用户时使用，包含密码）
     * 审计字段(id, createTime, updateTime, createdBy, updatedBy)由数据库和MyBatis Plus自动处理
     */
    SysUser toEntity(UserRequest request);

    /**
     * UserInfoRequest转Entity（更新用户时使用，不包含密码）
     * 审计字段(id, createTime, updateTime, createdBy, updatedBy)由数据库和MyBatis Plus自动处理
     */
    SysUser toEntityFromUserInfo(UserInfoRequest request);

    /**
     * Entity转Response DTO
     */
    UserResponse toResponse(SysUser user);

    /**
     * Entity列表转Response DTO列表
     */
    List<UserResponse> toResponseList(List<SysUser> users);
}