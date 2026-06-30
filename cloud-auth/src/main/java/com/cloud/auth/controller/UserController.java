package com.cloud.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.auth.dto.user.BatchUpdateUserStatusRequest;
import com.cloud.auth.dto.user.UserInfoRequest;
import com.cloud.auth.dto.user.UserRequest;
import com.cloud.auth.dto.user.UserResponse;
import com.cloud.auth.service.UserService;
import com.cloud.common.entity.BasePage;
import com.cloud.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理接口：增删改查、状态管理
 */
@RestController
@RequestMapping("/auth/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 根据用户名称或者账号，分页查询用户列表的功能，列表上有用户账号、用户名称、用户状态
     * @param basePage 分页参数
     * @param keyword 用户名或手机号（可选）
     * @return 用户分页列表
     */
    // @PreAuthorize("hasAuthority('user:query')")
    @GetMapping
    public Result<Page<UserResponse>> page(BasePage basePage,
            @RequestParam(required = false) String keyword) {
        return Result.ok(userService.pageUsers(basePage, keyword));
    }

    /**
     * 创建用户功能
     * @param request 用户创建请求
     * @return 创建的用户信息
     */
    // @PreAuthorize("hasAuthority('user:update')")
    @PostMapping
    public Result<UserResponse> create(@Valid @RequestBody UserRequest request) {
        return Result.ok(userService.createUser(request));
    }

    /**
     * 编辑用户功能
     * @param id 用户ID
     * @param request 用户编辑请求
     * @return 编辑后的用户信息
     */
    // @PreAuthorize("hasAuthority('user:update')")
    @PutMapping("/{id}")
    public Result<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserInfoRequest request) {
        return Result.ok(userService.updateUser(id, request));
    }

    /**
     * 删除用户功能
     * @param id 用户ID
     * @return 删除结果
     */
    // @PreAuthorize("hasAuthority('user:delete')")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.ok(true);
    }

    /**
     * 批量删除用户功能
     * @param ids 用户ID列表
     * @return 删除结果
     */
    // @PreAuthorize("hasAuthority('user:delete')")
    @DeleteMapping("/batch")
    public Result<Boolean> batchDelete(@RequestBody List<Long> ids) {
        userService.batchDeleteUsers(ids);
        return Result.ok(true);
    }

    /**
     * 根据ID查询用户功能
     * @param id 用户ID
     * @return 用户信息
     */
    // @PreAuthorize("hasAuthority('user:query')")
    @GetMapping("/{id}")
    public Result<UserResponse> getById(@PathVariable Long id) {
        return Result.ok(userService.getUserById(id));
    }

    /**
     * 更新用户状态功能
     * @param id 用户ID
     * @param enabled 状态值（1启用 0禁用）
     * @return 更新结果
     */
    // @PreAuthorize("hasAuthority('user:update')")
    @PutMapping("/{id}/status")
    public Result<Boolean> updateStatus(@PathVariable Long id, @RequestParam Integer enabled) {
        userService.updateUserStatus(id, enabled);
        return Result.ok(true);
    }

    /**
     * 批量更新用户状态功能
     * @param request 批量更新请求
     * @return 更新结果
     */
    // @PreAuthorize("hasAuthority('user:update')")
    @PutMapping("/batch/status")
    public Result<Boolean> batchUpdateStatus(@Valid @RequestBody BatchUpdateUserStatusRequest request) {
        userService.batchUpdateUserStatus(request.getUserIds(), request.getEnabled());
        return Result.ok(true);
    }


}