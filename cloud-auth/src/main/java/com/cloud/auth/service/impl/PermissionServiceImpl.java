package com.cloud.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloud.auth.converter.PermissionConverter;
import com.cloud.auth.dto.permission.PermissionRequest;
import com.cloud.auth.dto.permission.PermissionResponse;
import com.cloud.auth.dto.permission.PermissionTreeResponse;
import com.cloud.auth.entity.SysPermission;
import com.cloud.auth.mapper.SysPermissionMapper;
import com.cloud.auth.service.PermissionService;
import com.cloud.common.exception.BusinessException;
import com.cloud.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限管理服务实现
 */
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final SysPermissionMapper permissionMapper;
    private final PermissionConverter permissionConverter;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PermissionResponse create(PermissionRequest request) {
        // 1. 检查权限码是否已存在
        LambdaQueryWrapper<SysPermission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysPermission::getPermCode, request.getPermCode());
        if (permissionMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException(ResultCode.PERMISSION_CODE_EXISTS, "权限码已存在: " + request.getPermCode());
        }

        // 2. 校验权限类型
        validateTypeAndParentId(request.getType(), request.getParentId(), null);

        // 3. 校验父级是否存在
        Long parentId = request.getParentId() != null ? request.getParentId() : 0L;
        if (parentId != 0) {
            SysPermission parent = permissionMapper.selectById(parentId);
            if (parent == null) {
                throw new BusinessException(ResultCode.PERMISSION_PARENT_NOT_FOUND, "父级权限不存在，ID: " + parentId);
            }
        }

        // 4. 根据类型设置 path 和 component
        validatePathAndComponent(request);

        SysPermission entity = permissionConverter.toEntity(request);
        entity.setParentId(parentId);

        // 设置默认值
        if (entity.getVisible() == null) {
            entity.setVisible(1);
        }
        if (entity.getEnabled() == null) {
            entity.setEnabled(1);
        }
        if (entity.getSort() == null) {
            entity.setSort(0);
        }

        permissionMapper.insert(entity);
        return permissionConverter.toResponse(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PermissionResponse update(Long id, PermissionRequest request) {
        SysPermission existEntity = permissionMapper.selectById(id);
        if (existEntity == null) {
            throw new BusinessException(ResultCode.PERMISSION_NOT_FOUND, "权限不存在，ID: " + id);
        }

        // 1. 检查权限码是否与其他记录冲突
        LambdaQueryWrapper<SysPermission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysPermission::getPermCode, request.getPermCode());
        queryWrapper.ne(SysPermission::getId, id);
        if (permissionMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException(ResultCode.PERMISSION_CODE_EXISTS, "权限码已存在: " + request.getPermCode());
        }

        // 2. 校验权限类型
        validateTypeAndParentId(request.getType(), request.getParentId(), id);

        // 3. 校验父级是否存在
        Long newParentId = request.getParentId() != null ? request.getParentId() : 0L;
        if (newParentId != 0) {
            SysPermission parent = permissionMapper.selectById(newParentId);
            if (parent == null) {
                throw new BusinessException(ResultCode.DICT_TYPE_PARENT_NOT_FOUND, "父级权限不存在，ID: " + newParentId);
            }

            // 检查是否设置自己为父节点
            if (newParentId.equals(id)) {
                throw new BusinessException(ResultCode.PERMISSION_SELF_PARENT);
            }

            // 检查是否设置自己的子节点为父节点（避免循环引用）
            List<SysPermission> descendants = getDescendantEntities(id);
            boolean isCircular = descendants.stream().anyMatch(entity -> entity.getId().equals(newParentId));
            if (isCircular) {
                throw new BusinessException(ResultCode.PERMISSION_CIRCULAR_REFERENCE);
            }
        }

        // 4. 根据类型设置 path 和 component
        validatePathAndComponent(request);

        permissionConverter.updateEntity(request, existEntity);
        existEntity.setParentId(newParentId);
        permissionMapper.updateById(existEntity);

        return permissionConverter.toResponse(existEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 1. 检查权限是否存在
        SysPermission entity = permissionMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.PERMISSION_NOT_FOUND, "权限不存在，ID: " + id);
        }

        // 2. 检查是否有子节点
        List<SysPermission> children = getChildEntities(id);
        if (!children.isEmpty()) {
            throw new BusinessException(ResultCode.PERMISSION_HAS_CHILDREN, "该权限下有子节点，请先删除子节点");
        }

        // 3. 检查是否已分配给角色（TODO: 需要角色权限关联表后实现）
        permissionMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // 1. 收集所有需要删除的权限ID（包括所有子孙节点）
        List<Long> allIdsToDelete = new ArrayList<>(ids);

        // 2. 对每个ID，递归获取其所有子孙节点
        for (Long id : ids) {
            List<SysPermission> descendants = getDescendantEntities(id);
            descendants.forEach(descendant -> {
                if (!allIdsToDelete.contains(descendant.getId())) {
                    allIdsToDelete.add(descendant.getId());
                }
            });
        }

        // 3. 批量删除
        if (!allIdsToDelete.isEmpty()) {
            permissionMapper.deleteBatchIds(allIdsToDelete);
        }
    }

    @Override
    public PermissionResponse getById(Long id) {
        SysPermission entity = permissionMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.PERMISSION_NOT_FOUND, "权限不存在，ID: " + id);
        }
        return permissionConverter.toResponse(entity);
    }

    @Override
    public List<PermissionResponse> listAll() {
        LambdaQueryWrapper<SysPermission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(SysPermission::getSort).orderByAsc(SysPermission::getId);
        List<SysPermission> entities = permissionMapper.selectList(queryWrapper);
        return permissionConverter.toResponseList(entities);
    }

    @Override
    public List<PermissionTreeResponse> getTree(String permName, Integer type) {
        LambdaQueryWrapper<SysPermission> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(permName)) {
            queryWrapper.like(SysPermission::getPermName, permName);
        }
        if (type != null) {
            queryWrapper.eq(SysPermission::getType, type);
        }

        queryWrapper.orderByAsc(SysPermission::getSort).orderByAsc(SysPermission::getId);
        List<SysPermission> allPermissions = permissionMapper.selectList(queryWrapper);

        return buildTree(allPermissions, 0L);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEnabled(Long id, Integer enabled) {
        SysPermission entity = permissionMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.PERMISSION_NOT_FOUND, "权限不存在，ID: " + id);
        }

        // 验证状态值
        if (enabled == null || (enabled != 0 && enabled != 1)) {
            throw new BusinessException(ResultCode.PERMISSION_INVALID_STATUS);
        }

        // 获取所有子孙节点
        List<SysPermission> descendants = getDescendantEntities(id);

        // 构建需要更新的ID列表
        List<Long> idsToUpdate = new ArrayList<>();
        idsToUpdate.add(id);
        descendants.forEach(descendant -> idsToUpdate.add(descendant.getId()));

        // 批量更新
        if (!idsToUpdate.isEmpty()) {
            LambdaQueryWrapper<SysPermission> updateWrapper = new LambdaQueryWrapper<>();
            updateWrapper.in(SysPermission::getId, idsToUpdate);

            SysPermission updateEntity = new SysPermission();
            updateEntity.setEnabled(enabled);
            permissionMapper.update(updateEntity, updateWrapper);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateVisible(Long id, Integer visible) {
        SysPermission entity = permissionMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.PERMISSION_NOT_FOUND, "权限不存在，ID: " + id);
        }

        // 验证可见性值
        if (visible == null || (visible != 0 && visible != 1)) {
            throw new BusinessException(ResultCode.PERMISSION_INVALID_STATUS);
        }

        // 获取所有子孙节点
        List<SysPermission> descendants = getDescendantEntities(id);

        // 构建需要更新的ID列表
        List<Long> idsToUpdate = new ArrayList<>();
        idsToUpdate.add(id);
        descendants.forEach(descendant -> idsToUpdate.add(descendant.getId()));

        // 批量更新
        if (!idsToUpdate.isEmpty()) {
            LambdaQueryWrapper<SysPermission> updateWrapper = new LambdaQueryWrapper<>();
            updateWrapper.in(SysPermission::getId, idsToUpdate);

            SysPermission updateEntity = new SysPermission();
            updateEntity.setVisible(visible);
            permissionMapper.update(updateEntity, updateWrapper);
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 校验权限类型和父级ID的关系
     */
    private void validateTypeAndParentId(Integer type, Long parentId, Long currentId) {
        // 按钮（type=3）的父级必须是菜单（type=2）
        if (type != null && type == 3) {
            if (parentId == null || parentId == 0) {
                throw new BusinessException(ResultCode.PERMISSION_PARENT_TYPE_INVALID, "按钮的父级必须是菜单，不能直接挂在根节点下");
            }

            SysPermission parent = permissionMapper.selectById(parentId);
            if (parent != null && parent.getType() != 2) {
                throw new BusinessException(ResultCode.PERMISSION_PARENT_TYPE_INVALID, "按钮的父级必须是菜单（type=2）");
            }
        }

        // 菜单（type=2）的父级必须是目录（type=1）或根节点
        if (type != null && type == 2) {
            if (parentId != null && parentId != 0) {
                SysPermission parent = permissionMapper.selectById(parentId);
                if (parent != null && parent.getType() != 1) {
                    throw new BusinessException(ResultCode.PERMISSION_PARENT_TYPE_INVALID, "菜单的父级必须是目录（type=1）或根节点");
                }
            }
        }
    }

    /**
     * 校验并设置 path 和 component
     */
    private void validatePathAndComponent(PermissionRequest request) {
        Integer type = request.getType();

        // 按钮（type=3）的 path 和 component 应该为 null
        if (type != null && type == 3) {
            request.setPath(null);
            request.setComponent(null);
        }

        // 目录（type=1）通常不需要 component
        if (type != null && type == 1 && request.getComponent() != null) {
            request.setComponent(null);
        }
    }

    /**
     * 构建树形结构
     */
    private List<PermissionTreeResponse> buildTree(List<SysPermission> allPermissions, Long parentId) {
        List<PermissionTreeResponse> tree = new ArrayList<>();

        for (SysPermission permission : allPermissions) {
            if ((parentId == 0L && (permission.getParentId() == null || permission.getParentId() == 0L)) ||
                (permission.getParentId() != null && permission.getParentId().equals(parentId))) {

                // 递归查找子节点
                List<PermissionTreeResponse> children = buildTree(allPermissions, permission.getId());

                PermissionTreeResponse treeNode = new PermissionTreeResponse();
                treeNode.setId(permission.getId());
                treeNode.setPermCode(permission.getPermCode());
                treeNode.setPermName(permission.getPermName());
                treeNode.setType(permission.getType());
                treeNode.setParentId(permission.getParentId());
                treeNode.setIcon(permission.getIcon());
                treeNode.setPath(permission.getPath());
                treeNode.setComponent(permission.getComponent());
                treeNode.setVisible(permission.getVisible());
                treeNode.setServiceCode(permission.getServiceCode());
                treeNode.setEnabled(permission.getEnabled());
                treeNode.setSort(permission.getSort());
                treeNode.setRemark(permission.getRemark());
                treeNode.setChildren(children);

                tree.add(treeNode);
            }
        }

        return tree;
    }

    /**
     * 获取所有子孙节点实体（递归查询）
     */
    private List<SysPermission> getDescendantEntities(Long id) {
        List<SysPermission> allDescendants = new ArrayList<>();
        collectDescendants(id, allDescendants);
        return allDescendants;
    }

    /**
     * 递归收集子孙节点
     */
    private void collectDescendants(Long parentId, List<SysPermission> result) {
        List<SysPermission> children = getChildEntities(parentId);
        for (SysPermission child : children) {
            result.add(child);
            collectDescendants(child.getId(), result);
        }
    }

    /**
     * 获取子节点实体
     */
    private List<SysPermission> getChildEntities(Long parentId) {
        LambdaQueryWrapper<SysPermission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysPermission::getParentId, parentId);
        queryWrapper.orderByAsc(SysPermission::getSort).orderByAsc(SysPermission::getId);
        return permissionMapper.selectList(queryWrapper);
    }
}
