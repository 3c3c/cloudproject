package com.cloud.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.auth.entity.SysRole;
import com.cloud.auth.mapper.SysRoleMapper;
import com.cloud.auth.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 角色管理服务实现类
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final SysRoleMapper roleMapper;

    @Override
    @Transactional
    public SysRole create(SysRole role) {
        // 创建时默认启用
        if (role.getEnabled() == null) {
            role.setEnabled(1);
        }
        // 设置逻辑删除字段为未删除
        role.setDeleted(0);
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        roleMapper.insert(role);
        return role;
    }

    @Override
    @Transactional
    public SysRole update(Long id, SysRole role) {
        SysRole existing = roleMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("角色不存在");
        }

        role.setId(id);
        role.setUpdateTime(LocalDateTime.now());
        roleMapper.updateById(role);
        return roleMapper.selectById(id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // MyBatis Plus的逻辑删除会自动将deleted字段设置为1
        roleMapper.deleteById(id);
    }

    @Override
    public SysRole getById(Long id) {
        return roleMapper.selectById(id);
    }

    @Override
    public Page<SysRole> page(Integer current, Integer size, String roleName) {
        Page<SysRole> page = new Page<>(current, size);
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();

        if (roleName != null && !roleName.trim().isEmpty()) {
            wrapper.like(SysRole::getRoleName, roleName);
        }

        roleMapper.selectPage(page, wrapper);
        return page;
    }
}