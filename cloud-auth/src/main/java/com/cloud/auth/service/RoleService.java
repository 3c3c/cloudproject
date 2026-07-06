package com.cloud.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.auth.dto.permission.PermissionTreeWithAssignedResponse;
import com.cloud.auth.dto.role.RoleRequest;
import com.cloud.auth.dto.role.RoleResponse;
import com.cloud.common.entity.BasePage;

import java.util.List;

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
    void batchDelete(List<Long> ids);

    /**
     * 根据ID查询角色
     */
    RoleResponse getById(Long id);

    /**
     * 分页查询角色，支持根据关键字（角色编码或角色说明）模糊搜索
     * @param basePage 分页参数
     * @param keyword 关键字（可选，可匹配角色编码或角色说明）
     * @return 角色分页列表
     */
    Page<RoleResponse> page(BasePage basePage, String keyword);

    /**
     * 更新角色状态
     */
    void updateStatus(Long id, Integer enabled);

    /**
     * 批量更新角色状态
     */
    void batchUpdateStatus(List<Long> ids, Integer enabled);

    /**
     * 查询所有启用的角色
     */
    List<RoleResponse> getAllEnabledRoles();

    /**
     * 根据用户查询当前用户还没拥有的所有角色，支持根据关键字（角色编码或角色说明）模糊搜索
     * @param userId 用户ID
     * @param keyword 关键字（可选，可匹配角色编码或角色说明）
     * @return 用户未拥有的角色列表
     */
    List<RoleResponse> getRolesNotAssignedToUser(Long userId, String keyword);

    /**
     * 根据用户ID查询当前用户拥有的所有角色，支持根据关键字（角色编码或角色说明）模糊搜索
     * @param userId 用户ID
     * @param keyword 关键字（可选，可匹配角色编码或角色说明）
     * @return 用户拥有的角色列表
     */
    List<RoleResponse> getRolesByUserId(Long userId, String keyword);

    /**
     * 根据用户ID和角色ID列表批量删除用户角色
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     */
    void removeUserRoles(Long userId, List<Long> roleIds);

    /**
     * 根据角色ID查询所有权限树，并标注角色是否拥有该权限
     * @param roleId 角色ID
     * @return 权限树（带权限分配标记）
     */
    List<PermissionTreeWithAssignedResponse> getPermissionTreeByRole(Long roleId);

    /**
     * 为角色分配多个权限
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     */
    void assignPermissions(Long roleId, List<Long> permissionIds);
}