package com.cloud.auth.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 用户绑定角色请求DTO
 */
@Data
public class UserRoleBindRequest {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 角色ID列表（覆盖式分配：将用户的角色设置为此列表，为空表示解除该用户的所有角色绑定）
     */
    @NotNull(message = "角色ID列表不能为空")
    private List<Long> roleIds;
}
