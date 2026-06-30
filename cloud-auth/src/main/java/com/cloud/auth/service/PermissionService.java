package com.cloud.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.auth.dto.permission.PermissionRequest;
import com.cloud.auth.dto.permission.PermissionResponse;
import com.cloud.common.entity.BasePage;

/**
 * 权限管理服务接口
 */
public interface PermissionService {

    /**
     * 创建权限
     */
    PermissionResponse create(PermissionRequest request);

    /**
     * 编辑权限
     */
    PermissionResponse update(Long id, PermissionRequest request);

    /**
     * 删除权限
     */
    void delete(Long id);

    /**
     * 批量删除权限
     */
    void batchDelete(java.util.List<Long> ids);

    /**
     * 根据ID查询权限
     */
    PermissionResponse getById(Long id);

    /**
     * 分页查询权限（支持按权限名称筛选）
     */
    Page<PermissionResponse> page(BasePage basePage, String permName);

    /**
     * 按服务代码查询权限
     */
    java.util.List<PermissionResponse> getByServiceCode(String serviceCode);
}