package com.cloud.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.auth.entity.SysRole;
import com.cloud.auth.service.RoleService;
import com.cloud.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 角色管理接口：增删改查、状态管理
 */
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    // 添加根据角色名称分页查询所有角色的功能
    @GetMapping
    public Result<Page<SysRole>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String roleName) {
        return Result.ok(roleService.page(current, size, roleName));
    }

    // 添加删除角色的功能 要求使用mybatis plus的逻辑删除
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.ok();
    }

    // 添加角色的创建，编辑功能。创建的时候默认启用
    @PostMapping
    public Result<SysRole> create(@Valid @RequestBody SysRole role) {
        return Result.ok(roleService.create(role));
    }

    @PutMapping("/{id}")
    public Result<SysRole> update(@PathVariable Long id, @Valid @RequestBody SysRole role) {
        return Result.ok(roleService.update(id, role));
    }

    @GetMapping("/{id}")
    public Result<SysRole> getById(@PathVariable Long id) {
        return Result.ok(roleService.getById(id));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer enabled) {
        SysRole role = new SysRole();
        role.setId(id);
        role.setEnabled(enabled);
        roleService.update(id, role);
        return Result.ok();
    }
}