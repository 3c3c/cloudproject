package com.cloud.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.auth.dto.register.RegisterRequest;
import com.cloud.auth.dto.user.UserInfoRequest;
import com.cloud.auth.dto.user.UserRequest;
import com.cloud.auth.dto.user.UserResponse;
import com.cloud.auth.entity.SysUser;
import com.cloud.common.entity.BasePage;
import com.cloud.common.security.LoginUser;

import java.util.List;

/**
 * 用户服务接口：认证相关 + 用户管理
 */
public interface UserService {

    // ========== 认证相关方法 ==========

    /** 按用户名或手机号加载登录用户 */
    LoginUser loadLoginUserByKeyword(String keyword);

    /** 按手机号加载登录用户 */
    LoginUser loadLoginUserByMobile(String mobile);

    /** 注册新用户（自动赋予默认角色） */
    SysUser register(RegisterRequest req);

    /** 构建当前用户信息（角色 + 权限） */
    // UserInfo buildUserInfo(LoginUser loginUser);

    // ========== 用户管理方法 ==========

    /** 创建用户 */
    UserResponse createUser(UserRequest request);

    /** 编辑用户 */
    UserResponse updateUser(Long id, UserInfoRequest request);

    /** 删除用户 */
    void deleteUser(Long id);

    /** 批量删除用户 */
    void batchDeleteUsers(List<Long> ids);

    /** 根据ID查询用户 */
    UserResponse getUserById(Long id);

    /** 分页查询用户（支持按用户名或手机号筛选） */
    Page<UserResponse> pageUsers(BasePage basePage, String keyword);

    /** 更新用户状态 */
    void updateUserStatus(Long id, Integer enabled);

    /** 批量更新用户状态 */
    void batchUpdateUserStatus(List<Long> ids, Integer enabled);
}