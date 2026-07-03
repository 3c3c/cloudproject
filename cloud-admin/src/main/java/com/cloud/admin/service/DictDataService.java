package com.cloud.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloud.admin.dto.DictDataRequest;
import com.cloud.admin.dto.DictDataResponse;
import com.cloud.common.entity.BasePage;

import java.util.List;

/**
 * 字典数据服务接口
 */
public interface DictDataService {

    /**
     * 分页查询字典数据列表
     *
     * @param basePage   分页参数
     * @param dictTypeId 字典类型ID（可选）
     * @return 字典数据分页列表
     */
    IPage<DictDataResponse> page(BasePage basePage, Long dictTypeId);

    /**
     * 创建字典数据
     *
     * @param request 创建请求
     * @return 创建的字典数据
     */
    DictDataResponse create(DictDataRequest request);

    /**
     * 更新字典数据
     *
     * @param id      字典数据ID
     * @param request 更新请求
     * @return 更新后的字典数据
     */
    DictDataResponse update(Long id, DictDataRequest request);

    /**
     * 批量删除字典数据
     *
     * @param ids 字典数据ID列表
     */
    void batchDelete(List<Long> ids);

    /**
     * 根据字典类型编码查询所有字典数据
     *
     * @param dictCode 字典类型编码
     * @return 字典数据列表
     */
    List<DictDataResponse> listByDictCode(String dictCode);
}
