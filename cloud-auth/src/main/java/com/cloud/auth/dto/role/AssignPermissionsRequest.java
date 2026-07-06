package com.cloud.auth.dto.role;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 角色权限分配请求DTO
 */
@Data
public class AssignPermissionsRequest {

    /**
     * 要分配的权限ID列表
     * 可以为空数组，表示清除该角色的所有权限
     */
    @NotNull(message = "权限ID列表不能为null")
    private List<Long> permissionIds;
}