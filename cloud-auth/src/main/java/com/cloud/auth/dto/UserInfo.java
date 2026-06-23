package com.cloud.auth.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserInfo {

    private Long userId;
    private String username;
    private String mobile;
    private List<String> roles;
    private List<String> permissions;
}
