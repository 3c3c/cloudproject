package com.cloud.auth.dto.permission;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 权限响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponse {

    private Long id;
    private String permCode;
    private String remark;
    private String serviceCode;
    private Integer type;       // 权限类型：1=目录/菜单，2=按钮/权限点
    private Long parentId;      // 父级权限ID
    private Integer sort;       // 排序字段
    private List<PermissionResponse> children; // 子权限列表（用于树形结构）
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String createdBy;
    private String updatedBy;
}