package com.cloud.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.auth.entity.SysRole;

/**
 * 角色管理服务接口
 */
public interface RoleService {

    /**
     * 创建角色（创建时默认启用）
     */
    SysRole create(SysRole role);

    /**
     * 编辑角色
     */
    SysRole update(Long id, SysRole role);

    /**
     * 删除角色（逻辑删除）
     */
    void delete(Long id);

    /**
     * 根据ID查询角色
     */
    SysRole getById(Long id);

    /**
     * 分页查询角色（支持按角色名称筛选）
     */
    Page<SysRole> page(Integer current, Integer size, String roleName);
}