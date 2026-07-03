package com.cloud.admin.service;

import com.cloud.admin.dto.DictTypeRequest;
import com.cloud.admin.dto.DictTypeResponse;
import com.cloud.admin.dto.DictTypeTreeResponse;

import java.util.List;

/**
 * 字典类型服务接口（支持树形结构）
 */
public interface DictTypeService {

    /**
     * 创建字典类型
     *
     * @param request 创建请求
     * @return 创建的字典类型
     */
    DictTypeResponse create(DictTypeRequest request);

    /**
     * 更新字典类型
     *
     * @param id      字典类型ID
     * @param request 更新请求
     * @return 更新后的字典类型
     */
    DictTypeResponse update(Long id, DictTypeRequest request);

    /**
     * 批量删除字典类型
     *
     * @param ids 字典类型ID列表
     */
    void batchDelete(List<Long> ids);

    /**
     * 根据字典类型编码查询
     *
     * @param dictCode 字典类型编码
     * @return 字典类型
     */
    DictTypeResponse getByCode(String dictCode);

    /**
     * 根据多个字典类型编码批量查询
     *
     * @param dictCodes 字典类型编码列表
     * @return 字典类型列表
     */
    List<DictTypeResponse> getByCodes(List<String> dictCodes);

    /**
     * 查询所有字典类型列表
     *
     * @return 字典类型列表
     */
    List<DictTypeResponse> listAll();

    /**
     * 查询字典类型树形结构
     *
     * @return 字典类型树
     */
    List<DictTypeTreeResponse> getTree();

    /**
     * 更新字典类型状态
     *
     * @param id 字典类型ID
     * @param status 状态值（1启用 0禁用）
     */
    void updateStatus(Long id, Integer status);
}
