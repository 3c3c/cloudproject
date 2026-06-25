package com.cloud.auth.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserInfo {

    private Long userId;
    private String username;
    private String nickname;
    private String mobile;
    private String email;
    private String avatar;
    private List<String> roles;
    private List<String> permissions;
}
