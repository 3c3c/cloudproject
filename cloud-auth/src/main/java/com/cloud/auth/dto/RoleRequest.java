package com.cloud.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 角色创建/更新请求DTO
 */
@Data
public class RoleRequest {

    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50, message = "角色编码长度不能超过50个字符")
    private String roleCode;

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    private String roleName;

    @Size(max = 200, message = "备注长度不能超过200个字符")
    private String remark;

    private Integer enabled;
}