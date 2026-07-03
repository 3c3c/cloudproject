package com.cloud.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloud.admin.converter.DictTypeConverter;
import com.cloud.admin.dto.DictTypeRequest;
import com.cloud.admin.dto.DictTypeResponse;
import com.cloud.admin.dto.DictTypeTreeResponse;
import com.cloud.admin.entity.SysDictData;
import com.cloud.admin.entity.SysDictType;
import com.cloud.admin.mapper.SysDictDataMapper;
import com.cloud.admin.mapper.SysDictTypeMapper;
import com.cloud.admin.service.DictTypeService;
import com.cloud.common.exception.BusinessException;
import com.cloud.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 字典类型服务实现（支持树形结构）
 */
@Service
@RequiredArgsConstructor
public class DictTypeServiceImpl implements DictTypeService {

    private final SysDictTypeMapper dictTypeMapper;
    private final DictTypeConverter dictTypeConverter;
    private final SysDictDataMapper dictDataMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DictTypeResponse create(DictTypeRequest request) {
        // 检查字典类型编码是否已存在
        LambdaQueryWrapper<SysDictType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysDictType::getDictCode, request.getDictCode());
        if (dictTypeMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException(ResultCode.DICT_TYPE_CODE_EXISTS, "字典类型编码已存在: " + request.getDictCode());
        }

        SysDictType entity = dictTypeConverter.toEntity(request);

        // 处理树形结构字段
        Long parentId = request.getParentId() != null ? request.getParentId() : 0L;
        entity.setParentId(parentId);

        if (parentId != 0) {
            // 子节点，需要查询父节点信息并更新
            SysDictType parent = dictTypeMapper.selectById(parentId);
            if (parent == null) {
                throw new BusinessException(ResultCode.DICT_TYPE_PARENT_NOT_FOUND, "父节点不存在，ID: " + parentId);
            }

            // 检查是否设置自己为父节点
            if (parentId.equals(entity.getId())) {
                throw new BusinessException(ResultCode.DICT_TYPE_SELF_PARENT);
            }
        }

        // 设置默认状态为启用
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }

        dictTypeMapper.insert(entity);
        DictTypeResponse response = dictTypeConverter.toResponse(entity);
        response.setIsLeaf(true); // 新创建的节点默认为叶子节点
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DictTypeResponse update(Long id, DictTypeRequest request) {
        SysDictType existEntity = dictTypeMapper.selectById(id);
        if (existEntity == null) {
            throw new BusinessException(ResultCode.DICT_TYPE_NOT_FOUND, "字典类型不存在，ID: " + id);
        }

        // 检查字典类型编码是否与其他记录冲突
        LambdaQueryWrapper<SysDictType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysDictType::getDictCode, request.getDictCode());
        queryWrapper.ne(SysDictType::getId, id);
        if (dictTypeMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException(ResultCode.DICT_TYPE_CODE_EXISTS, "字典类型编码已存在: " + request.getDictCode());
        }

        // 处理parentId变更
        Long newParentId = request.getParentId() != null ? request.getParentId() : 0L;
        Long oldParentId = existEntity.getParentId() != null ? existEntity.getParentId() : 0L;

        if (!newParentId.equals(oldParentId)) {
            // 检查是否设置自己为父节点
            if (newParentId.equals(id)) {
                throw new BusinessException(ResultCode.DICT_TYPE_SELF_PARENT);
            }

            // 检查是否设置自己的子节点为父节点（避免循环引用）
            List<SysDictType> descendants = getDescendantEntities(id);
            boolean isCircular = descendants.stream().anyMatch(entity -> entity.getId().equals(newParentId));
            if (isCircular) {
                throw new BusinessException(ResultCode.DICT_TYPE_CIRCULAR_REFERENCE);
            }

            // 更新新的父节点
            if (newParentId != 0) {
                SysDictType newParent = dictTypeMapper.selectById(newParentId);
                if (newParent == null) {
                    throw new BusinessException(ResultCode.DICT_TYPE_PARENT_NOT_FOUND, "新的父节点不存在，ID: " + newParentId);
                }
            }
        }

        dictTypeConverter.updateEntity(request, existEntity);
        existEntity.setParentId(newParentId);
        dictTypeMapper.updateById(existEntity);
        DictTypeResponse response = dictTypeConverter.toResponse(existEntity);
        response.setIsLeaf(checkIsLeaf(existEntity.getId()));
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // 1. 收集所有需要删除的字典类型ID（包括所有子孙节点）
        List<Long> allIdsToDelete = new ArrayList<>(ids);

        // 2. 对每个ID，递归获取其所有子孙节点
        for (Long id : ids) {
            List<SysDictType> descendants = getDescendantEntities(id);
            descendants.forEach(descendant -> {
                if (!allIdsToDelete.contains(descendant.getId())) {
                    allIdsToDelete.add(descendant.getId());
                }
            });
        }

        // 3. 删除所有字典类型及其子孙节点
        if (!allIdsToDelete.isEmpty()) {
            dictTypeMapper.deleteBatchIds(allIdsToDelete);

            // 4. 删除这些字典类型关联的所有字典数据项
            LambdaQueryWrapper<SysDictData> dataWrapper = new LambdaQueryWrapper<>();
            dataWrapper.in(SysDictData::getDictTypeId, allIdsToDelete);
            dictDataMapper.delete(dataWrapper);
        }
    }

    @Override
    public DictTypeResponse getByCode(String dictCode) {
        LambdaQueryWrapper<SysDictType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysDictType::getDictCode, dictCode);
        SysDictType entity = dictTypeMapper.selectOne(queryWrapper);
        if (entity == null) {
            throw new BusinessException(ResultCode.DICT_TYPE_NOT_FOUND, "字典类型不存在，编码: " + dictCode);
        }
        DictTypeResponse response = dictTypeConverter.toResponse(entity);
        response.setIsLeaf(checkIsLeaf(entity.getId()));
        return response;
    }

    @Override
    public List<DictTypeResponse> getByCodes(List<String> dictCodes) {
        if (dictCodes == null || dictCodes.isEmpty()) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<SysDictType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysDictType::getDictCode, dictCodes);
        queryWrapper.orderByAsc(SysDictType::getSortOrder);
        List<SysDictType> entities = dictTypeMapper.selectList(queryWrapper);
        return entities.stream()
                .map(dictTypeConverter::toResponse)
                .peek(response -> response.setIsLeaf(checkIsLeaf(response.getId())))
                .toList();
    }

    @Override
    public List<DictTypeResponse> listAll() {
        LambdaQueryWrapper<SysDictType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(SysDictType::getSortOrder);
        List<SysDictType> entities = dictTypeMapper.selectList(queryWrapper);
        return entities.stream()
                .map(dictTypeConverter::toResponse)
                .peek(response -> response.setIsLeaf(checkIsLeaf(response.getId())))
                .toList();
    }

    @Override
    public List<DictTypeTreeResponse> getTree() {
        List<SysDictType> allTypes = dictTypeMapper.selectList(null);
        return buildTree(allTypes, 0L);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        SysDictType entity = dictTypeMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.DICT_TYPE_NOT_FOUND, "字典类型不存在，ID: " + id);
        }

        // 验证状态值
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ResultCode.DICT_TYPE_INVALID_STATUS);
        }

        // 获取所有子孙节点（包括子节点、孙节点等）
        List<SysDictType> descendants = getDescendantEntities(id);

        // 构建需要更新的ID列表（当前节点 + 所有子孙节点）
        List<Long> idsToUpdate = new ArrayList<>();
        idsToUpdate.add(id);
        descendants.forEach(descendant -> idsToUpdate.add(descendant.getId()));

        // 批量更新所有节点状态
        if (!idsToUpdate.isEmpty()) {
            LambdaQueryWrapper<SysDictType> updateWrapper = new LambdaQueryWrapper<>();
            updateWrapper.in(SysDictType::getId, idsToUpdate);

            // 创建更新实体
            SysDictType updateEntity = new SysDictType();
            updateEntity.setStatus(status);

            // 使用 MyBatis-Plus 的批量更新方法
            dictTypeMapper.update(updateEntity, updateWrapper);
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 构建树形结构
     */
    private List<DictTypeTreeResponse> buildTree(List<SysDictType> allTypes, Long parentId) {
        List<DictTypeTreeResponse> tree = new ArrayList<>();

        for (SysDictType type : allTypes) {
            if ((parentId == 0L && (type.getParentId() == null || type.getParentId() == 0L)) ||
                (type.getParentId() != null && type.getParentId().equals(parentId))) {

                // 先尝试查找子节点
                List<DictTypeTreeResponse> children = buildTree(allTypes, type.getId());

                DictTypeTreeResponse treeNode = new DictTypeTreeResponse(
                    type.getId(),
                    type.getDictName(),
                    type.getDictCode(),
                    type.getParentId(),
                    type.getSortOrder(),
                    type.getStatus(),
                    type.getRemark(),
                    children.isEmpty(), // isLeaf根据是否有子节点动态判断
                    children
                );

                tree.add(treeNode);
            }
        }

        return tree;
    }

    /**
     * 获取所有子孙节点实体（递归查询）
     */
    private List<SysDictType> getDescendantEntities(Long id) {
        List<SysDictType> allDescendants = new ArrayList<>();
        collectDescendants(id, allDescendants);
        return allDescendants;
    }

    /**
     * 递归收集子孙节点
     */
    private void collectDescendants(Long parentId, List<SysDictType> result) {
        List<SysDictType> children = getChildEntities(parentId);
        for (SysDictType child : children) {
            result.add(child);
            collectDescendants(child.getId(), result); // 递归收集子节点的子孙
        }
    }

    /**
     * 获取子节点实体
     */
    private List<SysDictType> getChildEntities(Long parentId) {
        LambdaQueryWrapper<SysDictType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysDictType::getParentId, parentId);
        queryWrapper.orderByAsc(SysDictType::getSortOrder);
        return dictTypeMapper.selectList(queryWrapper);
    }

    /**
     * 检查节点是否为叶子节点
     */
    private Boolean checkIsLeaf(Long id) {
        LambdaQueryWrapper<SysDictType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysDictType::getParentId, id);
        Long count = dictTypeMapper.selectCount(queryWrapper);
        return count == 0 || count == null; // 没有子节点则为叶子节点
    }
}
