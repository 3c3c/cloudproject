package com.cloud.auth.dto.permission;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 权限树形响应DTO（带权限分配标记）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimplePermissionTreeResponse {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 权限码
     */
    private String permCode;

    /**
     * 权限名称
     */
    private String permName;

    /**
     * 子节点列表
     */
    private List<SimplePermissionTreeResponse> children;

    /**
     * 当前角色是否拥有该权限：true=拥有，false=不拥有
     */
    private Boolean assigned;
}