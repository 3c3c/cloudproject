package com.cloud.auth.service;

import com.cloud.auth.dto.RegisterRequest;
import com.cloud.auth.dto.UserInfo;
import com.cloud.auth.entity.SysUser;
import com.cloud.common.security.LoginUser;

public interface UserService {

    /** 按用户名或手机号加载登录用户 */
    LoginUser loadLoginUserByKeyword(String keyword);

    /** 按手机号加载登录用户 */
    LoginUser loadLoginUserByMobile(String mobile);

    /** 注册新用户（自动赋予默认角色） */
    SysUser register(RegisterRequest req);

    /** 构建当前用户信息（角色 + 权限） */
    UserInfo buildUserInfo(LoginUser loginUser);
}
