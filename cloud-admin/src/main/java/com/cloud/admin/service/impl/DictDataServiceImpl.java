package com.cloud.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.admin.converter.DictDataConverter;
import com.cloud.admin.dto.DictDataRequest;
import com.cloud.admin.dto.DictDataResponse;
import com.cloud.admin.entity.SysDictData;
import com.cloud.admin.entity.SysDictType;
import com.cloud.admin.mapper.SysDictDataMapper;
import com.cloud.admin.mapper.SysDictTypeMapper;
import com.cloud.admin.service.DictDataService;
import com.cloud.common.entity.BasePage;
import com.cloud.common.exception.BusinessException;
import com.cloud.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 字典数据服务实现
 */
@Service
@RequiredArgsConstructor
public class DictDataServiceImpl implements DictDataService {

    private final SysDictDataMapper dictDataMapper;
    private final SysDictTypeMapper dictTypeMapper;
    private final DictDataConverter dictDataConverter;

    @Override
    public IPage<DictDataResponse> page(BasePage basePage, Long dictTypeId) {
        Page<SysDictData> page = new Page<>(basePage.getCurrent(), basePage.getSize());
        LambdaQueryWrapper<SysDictData> queryWrapper = new LambdaQueryWrapper<>();

        if (dictTypeId != null) {
            queryWrapper.eq(SysDictData::getDictTypeId, dictTypeId);
        }

        queryWrapper.orderByAsc(SysDictData::getSortOrder);

        Page<SysDictData> entityPage = dictDataMapper.selectPage(page, queryWrapper);
        return entityPage.convert(dictDataConverter::toResponse);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DictDataResponse create(DictDataRequest request) {
        // 验证字典类型是否存在
        SysDictType dictType = dictTypeMapper.selectById(request.getDictTypeId());
        if (dictType == null) {
            throw new BusinessException(ResultCode.DICT_TYPE_NOT_FOUND, "字典类型不存在，ID: " + request.getDictTypeId());
        }

        SysDictData entity = dictDataConverter.toEntity(request);
        dictDataMapper.insert(entity);
        return dictDataConverter.toResponse(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DictDataResponse update(Long id, DictDataRequest request) {
        SysDictData existEntity = dictDataMapper.selectById(id);
        if (existEntity == null) {
            throw new BusinessException(ResultCode.DICT_DATA_NOT_FOUND, "字典数据不存在，ID: " + id);
        }

        // 验证字典类型是否存在
        SysDictType dictType = dictTypeMapper.selectById(request.getDictTypeId());
        if (dictType == null) {
            throw new BusinessException(ResultCode.DICT_TYPE_NOT_FOUND, "字典类型不存在，ID: " + request.getDictTypeId());
        }

        dictDataConverter.updateEntity(request, existEntity);
        dictDataMapper.updateById(existEntity);
        return dictDataConverter.toResponse(existEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        dictDataMapper.deleteBatchIds(ids);
    }

    @Override
    public List<DictDataResponse> listByDictCode(String dictCode) {
        // 先根据字典类型编码查询字典类型
        LambdaQueryWrapper<SysDictType> typeQueryWrapper = new LambdaQueryWrapper<>();
        typeQueryWrapper.eq(SysDictType::getDictCode, dictCode);
        SysDictType dictType = dictTypeMapper.selectOne(typeQueryWrapper);
        if (dictType == null) {
            throw new BusinessException(ResultCode.DICT_TYPE_NOT_FOUND, "字典类型不存在，编码: " + dictCode);
        }

        // 再根据字典类型ID查询字典数据
        LambdaQueryWrapper<SysDictData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysDictData::getDictTypeId, dictType.getId());
        queryWrapper.orderByAsc(SysDictData::getSortOrder);
        List<SysDictData> entities = dictDataMapper.selectList(queryWrapper);
        return entities.stream()
                .map(dictDataConverter::toResponse)
                .toList();
    }
}
