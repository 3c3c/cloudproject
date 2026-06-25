package com.cloud.auth.service;

import com.cloud.auth.dto.login.LoginRequest;
import com.cloud.auth.dto.login.LoginResponse;
import com.cloud.common.security.LoginUser;

public interface AuthService {

    /** 账号密码登录 */
    LoginResponse login(LoginRequest req);

    /** 为登录用户签发 JWT 并写入 Redis（单点登录：覆盖旧 token） */
    LoginResponse issueToken(LoginUser loginUser);

    /** 退出登录：删除 Redis token + 加入黑名单 */
    void logout(String bearerToken);

    /** 刷新 token（基于当前上下文重新签发） */
    LoginResponse refresh();
}
