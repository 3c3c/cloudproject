package com.cloud.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.auth.dto.role.RoleRequest;
import com.cloud.auth.dto.role.RoleResponse;
import com.cloud.common.entity.BasePage;

/**
 * 角色管理服务接口
 */
public interface RoleService {

    /**
     * 创建角色（创建时默认启用）
     */
    RoleResponse create(RoleRequest request);

    /**
     * 编辑角色
     */
    RoleResponse update(Long id, RoleRequest request);

    /**
     * 删除角色（逻辑删除）
     */
    void delete(Long id);

    /**
     * 批量删除角色（逻辑删除）
     */
    void batchDelete(java.util.List<Long> ids);

    /**
     * 根据ID查询角色
     */
    RoleResponse getById(Long id);

    /**
     * 分页查询角色
     */
    Page<RoleResponse> page(BasePage basePage);

    /**
     * 更新角色状态
     */
    void updateStatus(Long id, Integer enabled);

    /**
     * 批量更新角色状态
     */
    void batchUpdateStatus(java.util.List<Long> ids, Integer enabled);

    /**
     * 查询所有启用的角色
     */
    java.util.List<RoleResponse> getAllEnabledRoles();
}