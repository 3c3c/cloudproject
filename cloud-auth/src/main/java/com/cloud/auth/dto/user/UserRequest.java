package com.cloud.auth.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户创建/更新请求DTO
 */
@Data
public class UserRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名长度不能超过50个字符")
    private String username;

    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;

    @Size(max = 20, message = "手机号长度不能超过20个字符")
    private String mobile;

    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    @Size(max = 200, message = "头像URL长度不能超过200个字符")
    private String avatar;

    private Integer enabled;

    private Boolean mustChangePassword;
}