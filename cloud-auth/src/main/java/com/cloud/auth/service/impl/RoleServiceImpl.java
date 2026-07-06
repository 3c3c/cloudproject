package com.cloud.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.auth.converter.RoleConverter;
import com.cloud.auth.dto.permission.PermissionTreeWithAssignedResponse;
import com.cloud.auth.dto.role.RoleRequest;
import com.cloud.auth.dto.role.RoleResponse;
import com.cloud.auth.entity.SysPermission;
import com.cloud.auth.entity.SysRole;
import com.cloud.auth.mapper.SysRoleMapper;
import com.cloud.auth.mapper.SysRolePermissionMapper;
import com.cloud.auth.mapper.SysUserMapper;
import com.cloud.auth.service.PermissionService;
import com.cloud.auth.service.RoleService;
import com.cloud.common.entity.BasePage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final PermissionService permissionService;
    private final SysRolePermissionMapper rolePermissionMapper;

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
        // 1. 先删除角色与权限的绑定关系
        rolePermissionMapper.deleteAllByRoleId(id);

        // 2. MyBatis Plus的逻辑删除会自动将deleted字段设置为1
        roleMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // 1. 先删除所有角色与权限的绑定关系
        for (Long roleId : ids) {
            rolePermissionMapper.deleteAllByRoleId(roleId);
        }

        // 2. 使用MyBatis Plus的批量删除方法（支持逻辑删除）
        roleMapper.deleteBatchIds(ids);
    }

    @Override
    public RoleResponse getById(Long id) {
        SysRole role = roleMapper.selectById(id);
        return roleConverter.toResponse(role);
    }

    @Override
    public Page<RoleResponse> page(BasePage basePage, String keyword) {
        Page<SysRole> page = new Page<>(basePage.getCurrent(), basePage.getSize());
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();

        // 如果提供了关键字，进行模糊搜索（匹配角色编码或角色说明）
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysRole::getRoleCode, keyword)
                    .or()
                    .like(SysRole::getRemark, keyword));
        }

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
    public void batchUpdateStatus(List<Long> ids, Integer enabled) {
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
    public List<RoleResponse> getRolesNotAssignedToUser(Long userId, String keyword) {
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

        // 如果提供了关键字，进行模糊搜索（匹配角色编码或角色说明）
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysRole::getRoleCode, keyword)
                    .or()
                    .like(SysRole::getRemark, keyword));
        }

        // 3. 按角色编码排序
        wrapper.orderByAsc(SysRole::getRoleCode);

        List<SysRole> roles = roleMapper.selectList(wrapper);
        return roleConverter.toResponseList(roles);
    }

    @Override
    public List<RoleResponse> getRolesByUserId(Long userId, String keyword) {
        // 1. 查询用户拥有的所有角色
        List<SysRole> userRoles = userMapper.selectRolesByUserId(userId);

        // 2. 如果提供了关键字，进行过滤
        if (userRoles != null && !userRoles.isEmpty() && StringUtils.hasText(keyword)) {
            return userRoles.stream()
                    .filter(role -> {
                        // 角色编码或角色说明模糊匹配（OR 关系）
                        boolean codeMatch = role.getRoleCode() != null &&
                                role.getRoleCode().toLowerCase().contains(keyword.toLowerCase());
                        boolean remarkMatch = role.getRemark() != null &&
                                role.getRemark().toLowerCase().contains(keyword.toLowerCase());
                        return codeMatch || remarkMatch;
                    })
                    .map(roleConverter::toResponse)
                    .collect(Collectors.toList());
        }

        return roleConverter.toResponseList(userRoles);
    }

    @Override
    @Transactional
    public void removeUserRoles(Long userId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        userMapper.deleteUserRolesByRoleIds(userId, roleIds);
    }

    @Override
    public List<PermissionTreeWithAssignedResponse> getPermissionTreeByRole(Long roleId) {
        // 1. 获取所有权限
        List<SysPermission> allPermissions = permissionService.getAllPermissions();

        // 2. 获取角色拥有的权限ID列表
        List<Long> assignedPermissionIds = rolePermissionMapper.getPermissionIdsByRoleId(roleId);
        Set<Long> assignedSet = assignedPermissionIds.stream()
                .collect(Collectors.toSet());

        // 3. 构建权限ID到权限的映射
        Map<Long, SysPermission> permissionMap = allPermissions.stream()
                .collect(Collectors.toMap(SysPermission::getId, p -> p));

        // 4. 构建带标记的权限树
        List<PermissionTreeWithAssignedResponse> tree = buildPermissionTreeWithAssigned(
                allPermissions, 0L, assignedSet, permissionMap);

        return tree;
    }

    /**
     * 递归构建带权限分配标记的权限树
     */
    private List<PermissionTreeWithAssignedResponse> buildPermissionTreeWithAssigned(
            List<SysPermission> allPermissions,
            Long parentId,
            Set<Long> assignedSet,
            Map<Long, SysPermission> permissionMap) {

        return allPermissions.stream()
                .filter(p -> p.getParentId().equals(parentId))
                .sorted((p1, p2) -> {
                    // 先按sort排序，再按ID排序
                    int sortCompare = Integer.compare(
                            p1.getSort() != null ? p1.getSort() : 0,
                            p2.getSort() != null ? p2.getSort() : 0);
                    return sortCompare != 0 ? sortCompare : Long.compare(p1.getId(), p2.getId());
                })
                .map(permission -> {
                    PermissionTreeWithAssignedResponse node = new PermissionTreeWithAssignedResponse();
                    node.setId(permission.getId());
                    node.setPermCode(permission.getPermCode());
                    node.setPermName(permission.getPermName());

                    // 设置权限分配标记
                    node.setAssigned(assignedSet.contains(permission.getId()));

                    // 递归构建子节点
                    List<PermissionTreeWithAssignedResponse> children = buildPermissionTreeWithAssigned(
                            allPermissions, permission.getId(), assignedSet, permissionMap);
                    node.setChildren(children);

                    return node;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        // 1. 验证角色是否存在
        if (!roleMapper.selectById(roleId).getDeleted().equals(0)) {
            throw new RuntimeException("角色不存在或已删除");
        }

        // 2. 删除角色的所有现有权限
        rolePermissionMapper.deleteAllByRoleId(roleId);

        // 3. 如果权限列表不为空，批量插入新的权限分配
        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<com.cloud.auth.entity.SysRolePermission> rolePermissions = permissionIds.stream()
                    .map(permId -> {
                        com.cloud.auth.entity.SysRolePermission rp = new com.cloud.auth.entity.SysRolePermission();
                        rp.setRoleId(roleId);
                        rp.setPermId(permId);
                        return rp;
                    })
                    .collect(Collectors.toList());

            rolePermissionMapper.batchInsert(rolePermissions);
        }
    }
}