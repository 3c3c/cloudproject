package com.cloud.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.admin.converter.DictConverter;
import com.cloud.admin.dto.DictRequest;
import com.cloud.admin.dto.DictResponse;
import com.cloud.admin.entity.SysDict;
import com.cloud.admin.mapper.SysDictMapper;
import com.cloud.admin.service.DictService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 字典管理服务实现类
 */
@Service
@RequiredArgsConstructor
public class DictServiceImpl implements DictService {

    private final SysDictMapper dictMapper;
    private final DictConverter dictConverter;

    @Override
    @Transactional
    public DictResponse create(DictRequest request) {
        SysDict dict = dictConverter.toEntity(request);
        dictMapper.insert(dict);
        return dictConverter.toResponse(dict);
    }

    @Override
    @Transactional
    public DictResponse update(Long id, DictRequest request) {
        SysDict existing = dictMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("字典不存在");
        }

        SysDict dict = dictConverter.toEntity(request);
        dict.setId(id);
        dictMapper.updateById(dict);
        return dictConverter.toResponse(dictMapper.selectById(id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        dictMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void batchDelete(java.util.List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        dictMapper.deleteBatchIds(ids);
    }

    @Override
    public DictResponse getById(Long id) {
        SysDict dict = dictMapper.selectById(id);
        return dictConverter.toResponse(dict);
    }

    @Override
    public Page<DictResponse> page(Integer current, Integer size, String dictName) {
        Page<SysDict> page = new Page<>(current, size);
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();

        if (dictName != null && !dictName.trim().isEmpty()) {
            wrapper.like(SysDict::getDictName, dictName);
        }

        dictMapper.selectPage(page, wrapper);

        // 转换为Response DTO分页对象
        Page<DictResponse> responsePage = new Page<>(current, size, page.getTotal());
        responsePage.setRecords(dictConverter.toResponseList(page.getRecords()));
        return responsePage;
    }

    @Override
    public java.util.List<DictResponse> getByDictType(String dictType) {
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDict::getDictType, dictType);
        wrapper.orderByAsc(SysDict::getSortOrder);
        return dictConverter.toResponseList(dictMapper.selectList(wrapper));
    }
}