package com.cloud.auth.dto.permission;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 权限响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponse {

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
     * 权限类型：1=目录，2=菜单，3=按钮/权限点
     */
    private Integer type;

    /**
     * 父级ID
     */
    private Long parentId;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 前端组件路径
     */
    private String component;

    /**
     * 是否可见：0=隐藏，1=显示
     */
    private Integer visible;

    /**
     * 所属产品/服务
     */
    private String serviceCode;

    /**
     * 状态：1=启用，0=禁用
     */
    private Integer enabled;

    /**
     * 排序字段
     */
    private Integer sort;

    /**
     * 说明
     */
    private String remark;
}
