package com.cloud.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.auth.dto.RoleRequest;
import com.cloud.auth.dto.RoleResponse;
import com.cloud.auth.service.RoleService;
import com.cloud.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理接口：增删改查、状态管理
 */
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * 根据角色名称分页查询所有角色的功能
     * @param current 当前页码
     * @param size 每页大小
     * @param roleName 角色名称（可选）
     * @return 角色分页列表
     */
    @GetMapping
    public Result<Page<RoleResponse>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String roleName) {
        return Result.ok(roleService.page(current, size, roleName));
    }

    /**
     * 删除角色的功能，使用mybatis plus的逻辑删除
     * @param id 角色ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.ok();
    }

    /**
     * 批量删除角色功能，使用mybatis plus的逻辑删除
     * @param ids 角色ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        roleService.batchDelete(ids);
        return Result.ok();
    }

    /**
     * 创建角色功能，创建的时候默认启用
     * @param request 角色创建请求
     * @return 创建的角色信息
     */
    @PostMapping
    public Result<RoleResponse> create(@Valid @RequestBody RoleRequest request) {
        return Result.ok(roleService.create(request));
    }

    /**
     * 编辑角色功能
     * @param id 角色ID
     * @param request 角色编辑请求
     * @return 编辑后的角色信息
     */
    @PutMapping("/{id}")
    public Result<RoleResponse> update(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        return Result.ok(roleService.update(id, request));
    }

    /**
     * 根据ID查询角色功能
     * @param id 角色ID
     * @return 角色信息
     */
    @GetMapping("/{id}")
    public Result<RoleResponse> getById(@PathVariable Long id) {
        return Result.ok(roleService.getById(id));
    }

    /**
     * 更新角色状态功能
     * @param id 角色ID
     * @param enabled 状态值（1启用 0禁用）
     * @return 更新结果
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer enabled) {
        roleService.updateStatus(id, enabled);
        return Result.ok();
    }
}