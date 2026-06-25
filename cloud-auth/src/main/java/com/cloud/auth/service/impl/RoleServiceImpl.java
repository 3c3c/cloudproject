package com.cloud.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.auth.converter.RoleConverter;
import com.cloud.auth.dto.RoleRequest;
import com.cloud.auth.dto.RoleResponse;
import com.cloud.auth.entity.SysRole;
import com.cloud.auth.mapper.SysRoleMapper;
import com.cloud.auth.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 角色管理服务实现类
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final SysRoleMapper roleMapper;
    private final RoleConverter roleConverter;

    @Override
    @Transactional
    public RoleResponse create(RoleRequest request) {
        SysRole role = roleConverter.toEntity(request);
        // 创建时默认启用
        if (role.getEnabled() == null) {
            role.setEnabled(1);
        }
        // 设置逻辑删除字段为未删除
        role.setDeleted(0);
        roleMapper.insert(role);
        return roleConverter.toResponse(role);
    }

    @Override
    @Transactional
    public RoleResponse update(Long id, RoleRequest request) {
        SysRole existing = roleMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("角色不存在");
        }

        SysRole role = roleConverter.toEntity(request);
        role.setId(id);
        roleMapper.updateById(role);
        return roleConverter.toResponse(roleMapper.selectById(id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // MyBatis Plus的逻辑删除会自动将deleted字段设置为1
        roleMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void batchDelete(java.util.List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        // 使用MyBatis Plus的批量删除方法（支持逻辑删除）
        roleMapper.deleteBatchIds(ids);
    }

    @Override
    public RoleResponse getById(Long id) {
        SysRole role = roleMapper.selectById(id);
        return roleConverter.toResponse(role);
    }

    @Override
    public Page<RoleResponse> page(Integer current, Integer size, String roleName) {
        Page<SysRole> page = new Page<>(current, size);
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();

        if (roleName != null && !roleName.trim().isEmpty()) {
            wrapper.like(SysRole::getRoleName, roleName);
        }

        roleMapper.selectPage(page, wrapper);

        // 转换为Response DTO分页对象
        Page<RoleResponse> responsePage = new Page<>(current, size, page.getTotal());
        responsePage.setRecords(roleConverter.toResponseList(page.getRecords()));
        return responsePage;
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer enabled) {
        SysRole role = new SysRole();
        role.setId(id);
        role.setEnabled(enabled);
        roleMapper.updateById(role);
    }
}