package com.cloud.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.auth.dto.role.BatchUpdateStatusRequest;
import com.cloud.auth.dto.role.RoleRequest;
import com.cloud.auth.dto.role.RoleResponse;
import com.cloud.auth.service.RoleService;
import com.cloud.common.entity.BasePage;
import com.cloud.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理接口：增删改查、状态管理
 */
@RestController
@RequestMapping("/auth/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * 分页查询所有角色的功能
     * @param basePage 分页参数
     * @return 角色分页列表
     */
    // @PreAuthorize("hasAuthority('role:query')")
    @GetMapping
    public Result<Page<RoleResponse>> page(BasePage basePage) {
        return Result.ok(roleService.page(basePage));
    }

    /**
     * 删除角色的功能，使用mybatis plus的逻辑删除
     * @param id 角色ID
     * @return 删除结果
     */
    // @PreAuthorize("hasAuthority('role:delete')")
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
    // @PreAuthorize("hasAuthority('role:delete')")
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
    // @PreAuthorize("hasAuthority('role:update')")
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
    // @PreAuthorize("hasAuthority('role:update')")
    @PutMapping("/{id}")
    public Result<RoleResponse> update(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        return Result.ok(roleService.update(id, request));
    }

    /**
     * 根据ID查询角色功能
     * @param id 角色ID
     * @return 角色信息
     */
    // @PreAuthorize("hasAuthority('role:query')")
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
    // @PreAuthorize("hasAuthority('role:update')")
    @PutMapping("/{id}/status")
    public Result<Boolean> updateStatus(@PathVariable Long id, @RequestParam Integer enabled) {
        roleService.updateStatus(id, enabled);
        return Result.ok(true);
    }

    /**
     * 查询所有启用的角色功能
     * @return 启用的角色列表（不分页）
     */
    // @PreAuthorize("hasAuthority('role:query')")
    @GetMapping("/all-enabled")
    public Result<List<RoleResponse>> getAllEnabledRoles() {
        return Result.ok(roleService.getAllEnabledRoles());
    }

    /**
     * 批量更新角色状态功能
     * @param request 批量更新请求（包含角色ID列表和状态值）
     * @return 更新结果
     */
    // @PreAuthorize("hasAuthority('role:update')")
    @PutMapping("/batch/status")
    public Result<Boolean> batchUpdateStatus(@Valid @RequestBody BatchUpdateStatusRequest request) {
        roleService.batchUpdateStatus(request.getIds(), request.getEnabled());
        return Result.ok(true);
    }

    //


    /**
     * 根据用户查询当前用户还没拥有的所有角色，可以根据角色名称搜索
     * @param userId 用户ID
     * @param roleName 角色名称（可选，支持模糊搜索）
     * @return 用户未拥有的角色列表
     */
    // @PreAuthorize("hasAuthority('role:query')")
    @GetMapping("/not-assigned")
    public Result<List<RoleResponse>> getRolesNotAssignedToUser(
            @RequestParam Long userId,
            @RequestParam(required = false) String roleName) {
        return Result.ok(roleService.getRolesNotAssignedToUser(userId, roleName));
    }




}