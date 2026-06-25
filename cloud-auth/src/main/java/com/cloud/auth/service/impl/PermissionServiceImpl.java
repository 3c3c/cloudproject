package com.cloud.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.auth.converter.PermissionConverter;
import com.cloud.auth.dto.permission.PermissionRequest;
import com.cloud.auth.dto.permission.PermissionResponse;
import com.cloud.auth.entity.SysPermission;
import com.cloud.auth.mapper.SysPermissionMapper;
import com.cloud.auth.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 权限管理服务实现类
 */
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final SysPermissionMapper permissionMapper;
    private final PermissionConverter permissionConverter;

    @Override
    @Transactional
    public PermissionResponse create(PermissionRequest request) {
        SysPermission permission = permissionConverter.toEntity(request);
        permissionMapper.insert(permission);
        return permissionConverter.toResponse(permission);
    }

    @Override
    @Transactional
    public PermissionResponse update(Long id, PermissionRequest request) {
        SysPermission existing = permissionMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("权限不存在");
        }

        SysPermission permission = permissionConverter.toEntity(request);
        permission.setId(id);
        permissionMapper.updateById(permission);

        return permissionConverter.toResponse(permissionMapper.selectById(id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        permissionMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void batchDelete(java.util.List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        // 使用MyBatis Plus的批量删除方法
        permissionMapper.deleteBatchIds(ids);
    }

    @Override
    public PermissionResponse getById(Long id) {
        SysPermission permission = permissionMapper.selectById(id);
        return permissionConverter.toResponse(permission);
    }

    @Override
    public Page<PermissionResponse> page(Integer current, Integer size, String permName) {
        Page<SysPermission> page = new Page<>(current, size);
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();

        if (permName != null && !permName.trim().isEmpty()) {
            wrapper.like(SysPermission::getPermName, permName);
        }

        permissionMapper.selectPage(page, wrapper);

        // 转换为Response DTO分页对象
        Page<PermissionResponse> responsePage = new Page<>(current, size, page.getTotal());
        responsePage.setRecords(permissionConverter.toResponseList(page.getRecords()));
        return responsePage;
    }

    @Override
    public java.util.List<PermissionResponse> getByServiceCode(String serviceCode) {
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPermission::getServiceCode, serviceCode);
        return permissionConverter.toResponseList(permissionMapper.selectList(wrapper));
    }
}