package com.cloud.auth.dto.permission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 权限创建/更新请求DTO
 */
@Data
public class PermissionRequest {

    /**
     * 权限码，格式如：product:add
     */
    @NotBlank(message = "权限码不能为空")
    @Size(max = 100, message = "权限码长度不能超过100个字符")
    private String permCode;

    /**
     * 权限名称
     */
    @NotBlank(message = "权限名称不能为空")
    @Size(max = 50, message = "权限名称长度不能超过50个字符")
    private String permName;

    /**
     * 权限类型：1=目录，2=菜单，3=按钮/权限点
     */
    @NotNull(message = "权限类型不能为空")
    private Integer type;

    /**
     * 父级ID，0或null表示根节点
     */
    private Long parentId;

    /**
     * 菜单图标
     */
    @Size(max = 100, message = "图标长度不能超过100个字符")
    private String icon;

    /**
     * 路由地址
     */
    @Size(max = 200, message = "路由地址长度不能超过200个字符")
    private String path;

    /**
     * 前端组件路径
     */
    @Size(max = 200, message = "组件路径长度不能超过200个字符")
    private String component;

    /**
     * 是否可见：0=隐藏，1=显示
     */
    private Integer visible;

    /**
     * 所属产品/服务
     */
    @Size(max = 50, message = "服务代码长度不能超过50个字符")
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
    @Size(max = 500, message = "说明长度不能超过500个字符")
    private String remark;
}
