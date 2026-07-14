package com.cloud.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloud.admin.dto.*;
import com.cloud.admin.service.DictDataService;
import com.cloud.admin.service.DictTypeService;
import com.cloud.common.entity.BasePage;
import com.cloud.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典管理统一接口：包含字典类型和字典数据的增删改查
 */
@RestController
@RequestMapping("/admin/dict")
@RequiredArgsConstructor
public class DictController {

    private final DictTypeService dictTypeService;
    private final DictDataService dictDataService;

    /**
     * 创建字典类型
     *
     * @param request 字典类型创建请求
     * @return 创建的字典类型信息
     */
//    @PreAuthorize("hasAuthority('dict:update')")
    @PostMapping("/types")
    public Result<DictTypeResponse> createType(@Valid @RequestBody DictTypeRequest request) {
        return Result.success(dictTypeService.create(request));
    }

    /**
     * 更新字典类型
     *
     * @param id      字典类型ID
     * @param request 字典类型更新请求
     * @return 更新后的字典类型信息
     */
//    @PreAuthorize("hasAuthority('dict:update')")
    @PutMapping("/types/{id}")
    public Result<DictTypeResponse> updateType(@PathVariable Long id,
                                                 @Valid @RequestBody DictTypeRequest request) {
        return Result.success(dictTypeService.update(id, request));
    }

    /**
     * 批量删除字典类型
     *
     * @param ids 字典类型ID列表
     * @return 删除结果
     */
//    @PreAuthorize("hasAuthority('dict:delete')")
    @DeleteMapping("/types/batch")
    public Result<Void> batchDeleteTypes(@RequestBody List<Long> ids) {
        dictTypeService.batchDelete(ids);
        return Result.success();
    }

    /**
     * 根据字典类型编码查询
     *
     * @param dictCode 字典类型编码
     * @return 字典类型信息
     */
//    @PreAuthorize("hasAuthority('dict:query')")
    @GetMapping("/types/by-code/{dictCode}")
    public Result<DictTypeResponse> searchType(@PathVariable String dictCode) {
        return Result.success(dictTypeService.getByCode(dictCode));
    }

    /**
     * 根据多个字典类型编码批量查询
     *
     * @param dictCodes 字典类型编码列表
     * @return 字典类型列表
     */
//    @PreAuthorize("hasAuthority('dict:query')")
    @PostMapping("/types/by-codes")
    public Result<List<DictTypeResponse>> searchTypesByCodes(@RequestBody List<String> dictCodes) {
        return Result.success(dictTypeService.getByCodes(dictCodes));
    }

    /**
     * 查询字典类型树形结构
     *
     * @return 字典类型树
     */
//    @PreAuthorize("hasAuthority('dict:query')")
    @GetMapping("/types/tree")
    public Result<List<DictTypeTreeResponse>> getTypeTree() {
        return Result.success(dictTypeService.getTree());
    }

    /**
     * 更新字典类型状态功能
     * @param id 字典类型ID
     * @param status 状态值（1启用 0禁用）
     * @return 更新结果
     */
//    @PreAuthorize("hasAuthority('dict:update')")
    @PutMapping("/types/{id}/status")
    public Result<Boolean> updateTypeStatus(@PathVariable Long id, @RequestParam Integer status) {
        dictTypeService.updateStatus(id, status);
        return Result.success(true);
    }


    // ==================== 字典数据接口 ====================

    /**
     * 分页查询字典数据列表
     *
     * @param basePage   分页参数
     * @param dictTypeId 字典类型ID（可选）
     * @return 字典数据分页列表
     */
//    @PreAuthorize("hasAuthority('dict:query')")
    @GetMapping("/data")
    public Result<IPage<DictDataResponse>> pageData(BasePage basePage,
                                                    @RequestParam(required = false) Long dictTypeId) {
        return Result.success(dictDataService.page(basePage, dictTypeId));
    }

    /**
     * 根据字典类型编码查询字典数据列表
     *
     * @param dictCode 字典类型编码
     * @return 字典数据列表
     */
//    @PreAuthorize("hasAuthority('dict:query')")
    @GetMapping("/data/getDictDataByCode")
    public Result<List<DictDataResponse>> listDataByCode(@RequestParam String dictCode) {
        return Result.success(dictDataService.listByDictCode(dictCode));
    }

    /**
     * 创建字典数据
     *
     * @param request 字典数据创建请求
     * @return 创建的字典数据信息
     */
//    @PreAuthorize("hasAuthority('dict:update')")
    @PostMapping("/data")
    public Result<DictDataResponse> createData(@Valid @RequestBody DictDataRequest request) {
        return Result.success(dictDataService.create(request));
    }

    /**
     * 更新字典数据
     *
     * @param id      字典数据ID
     * @param request 字典数据更新请求
     * @return 更新后的字典数据信息
     */
//    @PreAuthorize("hasAuthority('dict:update')")
    @PutMapping("/data/{id}")
    public Result<DictDataResponse> updateData(@PathVariable Long id,
                                                @Valid @RequestBody DictDataRequest request) {
        return Result.success(dictDataService.update(id, request));
    }

    /**
     * 批量删除字典数据
     *
     * @param ids 字典数据ID列表
     * @return 删除结果
     */
//    @PreAuthorize("hasAuthority('dict:delete')")
    @DeleteMapping("/data/batch")
    public Result<Void> batchDeleteData(@RequestBody List<Long> ids) {
        dictDataService.batchDelete(ids);
        return Result.success();
    }
}
