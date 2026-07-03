package com.cloud.auth.service;

import com.cloud.auth.dto.permission.PermissionRequest;
import com.cloud.auth.dto.permission.PermissionResponse;
import com.cloud.auth.dto.permission.PermissionTreeResponse;

import java.util.List;

/**
 * 权限管理服务接口
 */
public interface PermissionService {

    /**
     * 创建权限
     *
     * @param request 创建请求
     * @return 创建的权限
     */
    PermissionResponse create(PermissionRequest request);

    /**
     * 更新权限
     *
     * @param id      权限ID
     * @param request 更新请求
     * @return 更新后的权限
     */
    PermissionResponse update(Long id, PermissionRequest request);

    /**
     * 删除权限
     *
     * @param id 权限ID
     */
    void delete(Long id);

    /**
     * 批量删除权限
     *
     * @param ids 权限ID列表
     */
    void batchDelete(List<Long> ids);

    /**
     * 根据ID查询权限
     *
     * @param id 权限ID
     * @return 权限信息
     */
    PermissionResponse getById(Long id);

    /**
     * 查询所有权限列表（扁平化）
     *
     * @return 权限列表
     */
    List<PermissionResponse> listAll();

    /**
     * 查询权限树形列表
     *
     * @param permName 权限名称（可选，模糊查询）
     * @param type     权限类型（可选）
     * @return 树形权限列表
     */
    List<PermissionTreeResponse> getTree(String permName, Integer type);

    /**
     * 更新权限状态（级联更新所有子节点）
     *
     * @param id      权限ID
     * @param enabled 状态：1=启用，0=禁用
     */
    void updateEnabled(Long id, Integer enabled);

    /**
     * 更新权限可见性（级联更新所有子节点）
     *
     * @param id      权限ID
     * @param visible 可见性：1=显示，0=隐藏
     */
    void updateVisible(Long id, Integer visible);
}