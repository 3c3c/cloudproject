package com.cloud.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.auth.converter.PermissionConverter;
import com.cloud.auth.dto.permission.PermissionRequest;
import com.cloud.auth.dto.permission.PermissionResponse;
import com.cloud.auth.dto.permission.PermissionTreeResponse;
import com.cloud.auth.entity.SysPermission;
import com.cloud.auth.mapper.SysPermissionMapper;
import com.cloud.auth.mapper.SysRolePermissionMapper;
import com.cloud.auth.service.PermissionService;
import com.cloud.common.entity.BasePage;
import com.cloud.common.exception.BusinessException;
import com.cloud.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限管理服务实现
 */
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final SysPermissionMapper permissionMapper;
    private final PermissionConverter permissionConverter;
    private final SysRolePermissionMapper rolePermissionMapper;

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

        // 2. 获取所有子孙节点ID（包括子节点、孙节点等）
        List<SysPermission> descendants = getDescendantEntities(id);
        List<Long> allIdsToDelete = new ArrayList<>();
        allIdsToDelete.add(id); // 添加当前节点
        descendants.forEach(descendant -> allIdsToDelete.add(descendant.getId()));

        // 3. 解除所有权限与角色的绑定关系
        if (!allIdsToDelete.isEmpty()) {
            rolePermissionMapper.deleteAllByPermIds(allIdsToDelete);
        }

        // 4. 批量删除所有节点
        if (!allIdsToDelete.isEmpty()) {
            permissionMapper.deleteBatchIds(allIdsToDelete);
        }
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

        // 3. 解除所有权限与角色的绑定关系
        if (!allIdsToDelete.isEmpty()) {
            rolePermissionMapper.deleteAllByPermIds(allIdsToDelete);
        }

        // 4. 批量删除权限节点
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
    public Page<PermissionTreeResponse> getTree(BasePage basePage, String permName) {
        List<SysPermission> allPermissions = permissionMapper.selectList(new LambdaQueryWrapper<>());
        return buildTree(basePage, allPermissions, permName);
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

    @Override
    public List<SysPermission> getAllPermissions() {
        return permissionMapper.selectList(new LambdaQueryWrapper<>());
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 校验权限类型和父级ID的关系
     */
    private void validateTypeAndParentId(Integer type, Long parentId, Long currentId) {
        // 按钮（type=3）的父级必须是菜单（type=2）
        if (type != null && type == 3) {
            if (parentId == 0) {
                throw new BusinessException(ResultCode.PERMISSION_PARENT_TYPE_INVALID, "按钮的父级必须是菜单，不能直接挂在根节点下");
            }

            SysPermission parent = permissionMapper.selectById(parentId);
            if (parent != null && parent.getType() != 2) {
                throw new BusinessException(ResultCode.PERMISSION_PARENT_TYPE_INVALID, "按钮的父级必须是菜单（type=2）");
            }
        }

        // 菜单（type=2）的父级必须是目录（type=1）或根节点
        if (type != null && type == 2) {
            if (parentId != 0) {
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

        // 目录（type=1）的 path 必须填写，component 不需要
        if (type != null && type == 1) {
            if (!StringUtils.hasText(request.getPath())) {
                throw new BusinessException(ResultCode.PERMISSION_PARAM_INVALID, "目录类型的路由地址(path)不能为空");
            }
            // 目录通常不需要 component，如果传了就清空
            if (request.getComponent() != null) {
                request.setComponent(null);
            }
        }

        // 菜单（type=2）的 path 和 component 都必须填写
        if (type != null && type == 2) {
            if (!StringUtils.hasText(request.getPath())) {
                throw new BusinessException(ResultCode.PERMISSION_PARAM_INVALID, "菜单类型的路由地址(path)不能为空");
            }
            if (!StringUtils.hasText(request.getComponent())) {
                throw new BusinessException(ResultCode.PERMISSION_PARAM_INVALID, "菜单类型的组件路径(component)不能为空");
            }
        }

        // 按钮（type=3）的 path 和 component 应该为 null
        if (type != null && type == 3) {
            request.setPath(null);
            request.setComponent(null);
        }
    }

    /**
     * 构建树形结构（支持分页和过滤）
     * @param basePage 分页参数
     * @param allPermissions 所有权限数据
     * @param permName 权限名称过滤条件
     * @return 分页的树形结构
     */
    private Page<PermissionTreeResponse> buildTree(BasePage basePage, List<SysPermission> allPermissions, String permName) {
        // 1. 获取所有符合过滤条件的节点
        List<SysPermission> filteredPermissions = filterPermissions(allPermissions, permName);

        // 2. 如果有过滤条件，获取所有符合条件的节点及其祖先节点（整个链路）
        List<SysPermission> nodesToInclude;
        if (StringUtils.hasText(permName)) {
            nodesToInclude = getNodesWithAncestors(filteredPermissions, allPermissions);
        } else {
            // 无过滤条件，使用所有权限
            nodesToInclude = allPermissions;
        }

        // 3. 构建ID到节点的映射，方便查找
        java.util.Map<Long, SysPermission> nodeMap = nodesToInclude.stream()
            .collect(java.util.stream.Collectors.toMap(SysPermission::getId, java.util.function.Function.identity()));

        // 4. 获取所有第一级节点（parentId = 0）
        List<SysPermission> firstLevelNodes = nodesToInclude.stream()
            .filter(p -> p.getParentId().equals(0L))
            .sorted(java.util.Comparator.comparing(SysPermission::getSort).thenComparing(SysPermission::getId))
            .collect(java.util.stream.Collectors.toList());

        // 5. 计算总记录数（第一级节点总数）
        long total = firstLevelNodes.size();

        // 6. 应用分页（只对第一级进行分页）
        List<SysPermission> pagedFirstLevelNodes = applyPagination(firstLevelNodes, basePage);

        // 7. 构建树形结构
        List<PermissionTreeResponse> records = pagedFirstLevelNodes.stream()
            .map(node -> buildTreeNode(node, nodeMap))
            .collect(java.util.stream.Collectors.toList());

        // 8. 创建分页对象
        Page<PermissionTreeResponse> page = new Page<>();
        int current = basePage != null && basePage.getCurrent() != null ? basePage.getCurrent() : 1;
        int size = basePage != null && basePage.getSize() != null ? basePage.getSize() : 10;

        page.setCurrent(current);
        page.setSize(size);
        page.setTotal(total);
        page.setRecords(records);

        return page;
    }

    /**
     * 根据条件过滤权限
     */
    private List<SysPermission> filterPermissions(List<SysPermission> allPermissions, String permName) {
        return allPermissions.stream()
            .filter(permission -> {
                // 权限名称过滤
                if (StringUtils.hasText(permName)) {
                    if (!StringUtils.hasText(permission.getPermName()) ||
                        !permission.getPermName().contains(permName)) {
                        return false;
                    }
                }
                return true;
            })
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 获取节点及其所有祖先节点和子孙节点（完整链路）
     */
    private List<SysPermission> getNodesWithAncestors(List<SysPermission> filteredPermissions, List<SysPermission> allPermissions) {
        java.util.Set<Long> idsToInclude = new java.util.HashSet<>();

        for (SysPermission permission : filteredPermissions) {
            // 1. 添加当前节点
            idsToInclude.add(permission.getId());

            // 2. 递归添加所有祖先节点
            Long currentParentId = permission.getParentId();
            while (currentParentId != 0) {
                idsToInclude.add(currentParentId);
                // 查找父节点
                Long finalCurrentParentId = currentParentId;
                SysPermission parent = allPermissions.stream()
                    .filter(p -> p.getId().equals(finalCurrentParentId))
                    .findFirst()
                    .orElse(null);
                if (parent == null) {
                    break;
                }
                currentParentId = parent.getParentId();
            }

            // 3. 递归添加所有子孙节点
            List<SysPermission> descendants = getDescendantEntities(permission.getId());
            descendants.forEach(descendant -> idsToInclude.add(descendant.getId()));
        }

        // 返回所有需要包含的节点
        return allPermissions.stream()
            .filter(p -> idsToInclude.contains(p.getId()))
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 应用分页
     */
    private List<SysPermission> applyPagination(List<SysPermission> nodes, BasePage basePage) {
        if (basePage == null) {
            return nodes;
        }

        int current = basePage.getCurrent() != null ? basePage.getCurrent() : 1;
        int size = basePage.getSize() != null ? basePage.getSize() : 10;

        int fromIndex = (current - 1) * size;
        if (fromIndex >= nodes.size()) {
            return new ArrayList<>();
        }

        int toIndex = Math.min(fromIndex + size, nodes.size());
        return nodes.subList(fromIndex, toIndex);
    }

    /**
     * 构建单个树节点（递归构建子节点）
     */
    private PermissionTreeResponse buildTreeNode(SysPermission permission, java.util.Map<Long, SysPermission> nodeMap) {
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

        // 递归查找并构建子节点
        List<PermissionTreeResponse> children = nodeMap.values().stream()
            .filter(child -> child.getParentId().equals(permission.getId()))
            .sorted(java.util.Comparator.comparing(SysPermission::getSort).thenComparing(SysPermission::getId))
            .map(child -> buildTreeNode(child, nodeMap))
            .collect(java.util.stream.Collectors.toList());

        treeNode.setChildren(children);
        return treeNode;
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
