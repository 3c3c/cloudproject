package com.cloud.auth.entity;

import lombok.Data;

/**
 * 角色-权限关联表实体
 */
@Data
public class SysRolePermission {

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 权限ID
     */
    private Long permId;
}