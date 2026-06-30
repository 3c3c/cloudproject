package com.cloud.auth.dto.permission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 权限创建/更新请求DTO
 */
@Data
public class PermissionRequest {

    @NotBlank(message = "权限码不能为空")
    @Size(max = 100, message = "权限码长度不能超过100个字符")
    private String permCode;

    @Size(max = 255, message = "权限说明长度不能超过255个字符")
    private String remark;

    @Size(max = 200, message = "服务代码长度不能超过200个字符")
    private String serviceCode;

    private Integer type;       // 权限类型：1=目录/菜单，2=按钮/权限点
    private Long parentId;      // 父级权限ID，0表示根节点
    private Integer sort;       // 排序字段，数值越小越靠前
}