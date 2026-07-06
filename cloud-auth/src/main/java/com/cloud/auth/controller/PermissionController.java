package com.cloud.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.auth.dto.permission.PermissionRequest;
import com.cloud.auth.dto.permission.PermissionResponse;
import com.cloud.auth.dto.permission.PermissionTreeResponse;
import com.cloud.auth.service.PermissionService;
import com.cloud.common.entity.BasePage;
import com.cloud.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限管理接口
 */
@RestController
@RequestMapping("/auth/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * 查询权限树形列表
     * @param basePage 分页信息
     * @param permName 权限名称（可选，模糊查询）
     * @return 树形权限列表
     */
//    @PreAuthorize("hasAuthority('permission:query')")
    @GetMapping("/tree")
    public Result<Page<PermissionTreeResponse>> getTree(
            BasePage basePage,
            @RequestParam(required = false) String permName) {
        return Result.ok(permissionService.getTree(basePage, permName));
    }

    /**
     * 查询所有权限列表（扁平化）
     *
     * @return 权限列表
     */
    @PreAuthorize("hasAuthority('permission:query')")
    @GetMapping("/list")
    public Result<List<PermissionResponse>> listAll() {
        return Result.ok(permissionService.listAll());
    }

    /**
     * 根据ID查询权限
     *
     * @param id 权限ID
     * @return 权限信息
     */
//    @PreAuthorize("hasAuthority('permission:query')")
    @GetMapping("/{id}")
    public Result<PermissionResponse> getById(@PathVariable Long id) {
        return Result.ok(permissionService.getById(id));
    }

    /**
     * 创建权限
     *
     * @param request 创建请求
     * @return 创建的权限
     */
//    @PreAuthorize("hasAuthority('permission:create')")
    @PostMapping
    public Result<PermissionResponse> create(@Valid @RequestBody PermissionRequest request) {
        return Result.ok(permissionService.create(request));
    }

    /**
     * 更新权限
     *
     * @param id      权限ID
     * @param request 更新请求
     * @return 更新后的权限
     */
//    @PreAuthorize("hasAuthority('permission:update')")
    @PutMapping("/{id}")
    public Result<PermissionResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PermissionRequest request) {
        return Result.ok(permissionService.update(id, request));
    }

    /**
     * 删除权限
     *
     * @param id 权限ID
     * @return 删除结果
     */
//    @PreAuthorize("hasAuthority('permission:delete')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return Result.ok();
    }

    /**
     * 批量删除权限
     *
     * @param ids 权限ID列表
     * @return 删除结果
     */
//    @PreAuthorize("hasAuthority('permission:delete')")
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        permissionService.batchDelete(ids);
        return Result.ok();
    }

    /**
     * 更新权限状态（级联更新所有子节点）
     *
     * @param id      权限ID
     * @param enabled 状态：1=启用，0=禁用
     * @return 更新结果
     */
//    @PreAuthorize("hasAuthority('permission:update')")
    @PutMapping("/{id}/enabled")
    public Result<Void> updateEnabled(
            @PathVariable Long id,
            @RequestParam Integer enabled) {
        permissionService.updateEnabled(id, enabled);
        return Result.ok();
    }

    /**
     * 更新权限可见性（级联更新所有子节点）
     *
     * @param id      权限ID
     * @param visible 可见性：1=显示，0=隐藏
     * @return 更新结果
     */
//    @PreAuthorize("hasAuthority('permission:update')")
    @PutMapping("/{id}/visible")
    public Result<Void> updateVisible(
            @PathVariable Long id,
            @RequestParam Integer visible) {
        permissionService.updateVisible(id, visible);
        return Result.ok();
    }
}
