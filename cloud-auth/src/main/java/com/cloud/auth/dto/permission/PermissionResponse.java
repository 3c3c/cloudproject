package com.cloud.auth.dto.permission;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 权限响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponse {

    private Long id;
    private String permCode;
    private String permName;
    private String serviceCode;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String createdBy;
    private String updatedBy;
}