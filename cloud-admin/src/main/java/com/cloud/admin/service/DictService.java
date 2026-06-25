package com.cloud.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.admin.dto.DictRequest;
import com.cloud.admin.dto.DictResponse;

/**
 * 字典管理服务接口
 */
public interface DictService {

    /**
     * 创建字典
     */
    DictResponse create(DictRequest request);

    /**
     * 编辑字典
     */
    DictResponse update(Long id, DictRequest request);

    /**
     * 删除字典
     */
    void delete(Long id);

    /**
     * 批量删除字典
     */
    void batchDelete(java.util.List<Long> ids);

    /**
     * 根据ID查询字典
     */
    DictResponse getById(Long id);

    /**
     * 分页查询字典（支持按字典名称筛选）
     */
    Page<DictResponse> page(Integer current, Integer size, String dictName);

    /**
     * 按字典类型查询字典列表
     */
    java.util.List<DictResponse> getByDictType(String dictType);
}