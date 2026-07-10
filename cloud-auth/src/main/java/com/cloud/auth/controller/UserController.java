package com.cloud.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.auth.dto.role.RoleResponse;
import com.cloud.auth.dto.user.BatchUpdateUserStatusRequest;
import com.cloud.auth.dto.user.UserInfoRequest;
import com.cloud.auth.dto.user.UserRequest;
import com.cloud.auth.dto.user.UserResponse;
import com.cloud.auth.dto.user.UserRoleBindRequest;
import com.cloud.auth.service.RoleService;
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
    private final RoleService roleService;

    /**
     * 根据用户名称或者账号，分页查询用户列表的功能，列表上有用户账号、用户名称、用户状态
     * @param basePage 分页参数
     * @param keyword 用户名称或者用户账号
     * @return 用户分页列表
     */
    // @PreAuthorize("hasAuthority('user:query')")
    @GetMapping
    public Result<Page<UserResponse>> page(BasePage basePage,
            @RequestParam(required = false) String keyword) {
        return Result.success(userService.pageUsers(basePage, keyword));
    }

    /**
     * 创建用户功能
     * @param request 用户创建请求
     * @return 创建的用户信息
     */
    // @PreAuthorize("hasAuthority('user:update')")
    @PostMapping
    public Result<UserResponse> create(@Valid @RequestBody UserRequest request) {
        return Result.success(userService.createUser(request));
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
        return Result.success(userService.updateUser(id, request));
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
        return Result.success(true);
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
        return Result.success(true);
    }

    /**
     * 根据ID查询用户功能
     * @param id 用户ID
     * @return 用户信息
     */
    // @PreAuthorize("hasAuthority('user:query')")
    @GetMapping("/{id}")
    public Result<UserResponse> getById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
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
        return Result.success(true);
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
        return Result.success(true);
    }

    /**
     * 根据用户Id查询当前用户拥有的所有角色，支持根据关键字（角色编码或角色说明）模糊搜索
     * @param userId 用户ID
     * @param keyword 关键字（可选，可匹配角色编码或角色说明）
     * @return 用户拥有的角色列表
     */
    // @PreAuthorize("hasAuthority('role:query')")
    @GetMapping("/getRolesByUserId/{userId}")
    public Result<List<RoleResponse>> getRolesByUserId(
            @PathVariable Long userId,
            @RequestParam(required = false) String keyword) {
        return Result.success(roleService.getRolesByUserId(userId, keyword));
    }

    /**
     * 根据用户查询当前用户还没拥有的所有角色，支持根据关键字（角色编码或角色说明）模糊搜索
     * @param userId 用户ID
     * @param keyword 关键字（可选，可匹配角色编码或角色说明）
     * @return 用户未拥有的角色列表
     */
    // @PreAuthorize("hasAuthority('role:query')")
    @GetMapping("/notAssignedRole")
    public Result<List<RoleResponse>> getRolesNotAssignedToUser(
            @RequestParam Long userId,
            @RequestParam(required = false) String keyword) {
        return Result.success(roleService.getRolesNotAssignedToUser(userId, keyword));
    }


    /**
     * 用户绑定角色功能（一个用户Id可以绑定多个角色Id，覆盖式分配）
     * @param request 角色绑定请求（userId 指定用户，roleIds 为该用户的最终角色列表，覆盖原有绑定；为空表示解除全部角色绑定）
     * @return 绑定结果
     */
    // @PreAuthorize("hasAuthority('user:update')")
    @PutMapping("/roles")
    public Result<Boolean> assignRoles(@Valid @RequestBody UserRoleBindRequest request) {
        userService.assignUserRoles(request.getUserId(), request.getRoleIds());
        return Result.success(true);
    }

    /**
     * 根据用户Id，角色Id列表批量删除一个用户拥有的角色
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @return 删除结果
     */
    // @PreAuthorize("hasAuthority('user:update')")
    @DeleteMapping("/{userId}/roles")
    public Result<Boolean> removeUserRoles(@PathVariable Long userId, @RequestBody List<Long> roleIds) {
        userService.removeUserRoles(userId, roleIds);
        return Result.success(true);
    }



}