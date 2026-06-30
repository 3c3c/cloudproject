package com.cloud.auth.dto.register;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    private String nickname;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String mobile;

    private String email;
}
