package com.cloud.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.auth.dto.permission.PermissionRequest;
import com.cloud.auth.dto.permission.PermissionResponse;
import com.cloud.auth.service.PermissionService;
import com.cloud.common.entity.BasePage;
import com.cloud.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限管理接口：增删改查、按服务查询
 */
@RestController
@RequestMapping("/auth/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * 根据权限名称分页查询全部权限列表功能
     * @param basePage 分页参数
     * @param permName 权限名称（可选）
     * @return 权限分页列表
     */
    @GetMapping
    public Result<Page<PermissionResponse>> page(BasePage basePage,
            @RequestParam(required = false) String permName) {
        return Result.ok(permissionService.page(basePage, permName));
    }

    /**
     * 添加权限功能
     * @param request 权限创建请求
     * @return 创建的权限信息
     */
    @PostMapping
    public Result<PermissionResponse> create(@Valid @RequestBody PermissionRequest request) {
        return Result.ok(permissionService.create(request));
    }

    /**
     * 编辑权限功能
     * @param id 权限ID
     * @param request 权限编辑请求
     * @return 编辑后的权限信息
     */
    @PutMapping("/{id}")
    public Result<PermissionResponse> update(@PathVariable Long id, @Valid @RequestBody PermissionRequest request) {
        return Result.ok(permissionService.update(id, request));
    }

    /**
     * 删除权限功能
     * @param id 权限ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return Result.ok();
    }

    /**
     * 批量删除权限功能
     * @param ids 权限ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        permissionService.batchDelete(ids);
        return Result.ok();
    }

    /**
     * 根据ID查询权限功能
     * @param id 权限ID
     * @return 权限信息
     */
    @GetMapping("/{id}")
    public Result<PermissionResponse> getById(@PathVariable Long id) {
        return Result.ok(permissionService.getById(id));
    }

    /**
     * 按服务代码查询权限功能
     * @param serviceCode 服务代码
     * @return 权限列表
     */
    @GetMapping("/by-service/{serviceCode}")
    public Result<List<PermissionResponse>> getByServiceCode(@PathVariable String serviceCode) {
        return Result.ok(permissionService.getByServiceCode(serviceCode));
    }
}