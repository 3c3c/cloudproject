package com.cloud.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.admin.dto.DictRequest;
import com.cloud.admin.dto.DictResponse;
import com.cloud.admin.service.DictService;
import com.cloud.common.entity.BasePage;
import com.cloud.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典管理接口：增删改查、按类型查询
 */
@RestController
@RequestMapping("/dicts")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    /**
     * 根据字典名称分页查询全部字典列表功能
     * @param basePage 分页参数
     * @param dictName 字典名称（可选）
     * @return 字典分页列表
     */
    @PreAuthorize("hasAuthority('dict:query')")
    @GetMapping
    public Result<Page<DictResponse>> page(BasePage basePage,
            @RequestParam(required = false) String dictName) {
        return Result.ok(dictService.page(basePage, dictName));
    }

    /**
     * 添加字典功能
     * @param request 字典创建请求
     * @return 创建的字典信息
     */
    @PreAuthorize("hasAuthority('dict:update')")
    @PostMapping
    public Result<DictResponse> create(@Valid @RequestBody DictRequest request) {
        return Result.ok(dictService.create(request));
    }

    /**
     * 编辑字典功能
     * @param id 字典ID
     * @param request 字典编辑请求
     * @return 编辑后的字典信息
     */
    @PreAuthorize("hasAuthority('dict:update')")
    @PutMapping("/{id}")
    public Result<DictResponse> update(@PathVariable Long id, @Valid @RequestBody DictRequest request) {
        return Result.ok(dictService.update(id, request));
    }

    /**
     * 删除字典功能
     * @param id 字典ID
     * @return 删除结果
     */
    @PreAuthorize("hasAuthority('dict:delete')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dictService.delete(id);
        return Result.ok();
    }

    /**
     * 批量删除字典功能
     * @param ids 字典ID列表
     * @return 删除结果
     */
    @PreAuthorize("hasAuthority('dict:delete')")
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        dictService.batchDelete(ids);
        return Result.ok();
    }

    /**
     * 根据ID查询字典功能
     * @param id 字典ID
     * @return 字典信息
     */
    @PreAuthorize("hasAuthority('dict:query')")
    @GetMapping("/{id}")
    public Result<DictResponse> getById(@PathVariable Long id) {
        return Result.ok(dictService.getById(id));
    }

    /**
     * 按字典类型查询字典列表功能
     * @param dictType 字典类型
     * @return 字典列表
     */
    @PreAuthorize("hasAuthority('dict:query')")
    @GetMapping("/by-type/{dictType}")
    public Result<List<DictResponse>> getByDictType(@PathVariable String dictType) {
        return Result.ok(dictService.getByDictType(dictType));
    }
}