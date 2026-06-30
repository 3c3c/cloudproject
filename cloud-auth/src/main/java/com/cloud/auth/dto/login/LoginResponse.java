package com.cloud.auth.dto.login;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String tokenHead;
    private Long userId;
    private String username;
    private String nickname;
    private String mobile;
    private String email;
    private String avatar;
    private List<String> authorities;
    private Boolean mustChangePassword;
}
