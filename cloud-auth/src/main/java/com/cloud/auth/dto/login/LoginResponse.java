package com.cloud.auth.dto.login;

import com.cloud.auth.dto.permission.PermissionTreeResponse;
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
    private Boolean mustChangePassword;

    // 按钮权限列表
    private List<String> authorities;
    // 组装好的目录和菜单树
    private List<PermissionTreeResponse> menuTree;

}
