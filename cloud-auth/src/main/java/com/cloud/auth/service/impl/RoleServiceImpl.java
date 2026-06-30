package com.cloud.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.auth.converter.RoleConverter;
import com.cloud.auth.dto.role.RoleRequest;
import com.cloud.auth.dto.role.RoleResponse;
import com.cloud.auth.entity.SysRole;
import com.cloud.auth.mapper.SysRoleMapper;
import com.cloud.auth.mapper.SysUserMapper;
import com.cloud.auth.service.RoleService;
import com.cloud.common.entity.BasePage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色管理服务实现类
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final SysRoleMapper roleMapper;
    private final RoleConverter roleConverter;
    private final SysUserMapper userMapper;

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
    public Page<RoleResponse> page(BasePage basePage) {
        Page<SysRole> page = new Page<>(basePage.getCurrent(), basePage.getSize());
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();

        roleMapper.selectPage(page, wrapper);

        // 转换为Response DTO分页对象，复制所有分页信息
        Page<RoleResponse> responsePage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        responsePage.setRecords(roleConverter.toResponseList(page.getRecords()));
        responsePage.setPages(page.getPages());
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

    @Override
    @Transactional
    public void batchUpdateStatus(java.util.List<Long> ids, Integer enabled) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        // 批量更新角色状态
        for (Long id : ids) {
            SysRole role = new SysRole();
            role.setId(id);
            role.setEnabled(enabled);
            roleMapper.updateById(role);
        }
    }

    @Override
    public List<RoleResponse> getAllEnabledRoles() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getEnabled, 1);  // 只查询启用的角色
        List<SysRole> roles = roleMapper.selectList(wrapper);
        return roleConverter.toResponseList(roles);
    }

    @Override
    public List<RoleResponse> getRolesNotAssignedToUser(Long userId, String roleName) {
        // 1. 查询该用户已拥有的角色 ID 列表
        List<com.cloud.auth.entity.SysRole> userRoles = userMapper.selectRolesByUserId(userId);
        List<Long> userRoleIds = userRoles.stream()
                .map(com.cloud.auth.entity.SysRole::getId)
                .collect(Collectors.toList());

        // 2. 查询所有启用且未被分配的角色
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getEnabled, 1);  // 只查询启用的角色

        // 如果用户已有角色，排除这些角色
        if (userRoleIds != null && !userRoleIds.isEmpty()) {
            wrapper.notIn(SysRole::getId, userRoleIds);
        }

        // 如果提供了角色名称，进行模糊搜索
        if (StringUtils.hasText(roleName)) {
            wrapper.like(SysRole::getRoleName, roleName);
        }

        // 3. 按角色名称排序
        wrapper.orderByAsc(SysRole::getRoleName);

        List<SysRole> roles = roleMapper.selectList(wrapper);
        return roleConverter.toResponseList(roles);
    }
}