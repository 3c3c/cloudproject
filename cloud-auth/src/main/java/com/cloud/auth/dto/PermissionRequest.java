package com.cloud.auth.dto;

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

    @NotBlank(message = "权限名称不能为空")
    @Size(max = 50, message = "权限名称长度不能超过50个字符")
    private String permName;

    @Size(max = 200, message = "服务代码长度不能超过200个字符")
    private String serviceCode;
}