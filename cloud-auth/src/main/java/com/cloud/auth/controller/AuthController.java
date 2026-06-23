package com.cloud.auth.controller;

import com.cloud.auth.dto.LoginRequest;
import com.cloud.auth.dto.LoginResponse;
import com.cloud.auth.dto.RegisterRequest;
import com.cloud.auth.dto.UserInfo;
import com.cloud.auth.entity.SysUser;
import com.cloud.auth.service.AuthService;
import com.cloud.auth.service.UserService;
import com.cloud.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口：登录 / 注册 / 退出 / 刷新 / 当前用户。
 * 注意：手机号验证码登录（/auth/sms/login）由 MobileAuthenticationFilter 拦截，不在此 Controller。
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return Result.ok(authService.login(req));
    }

    @PostMapping("/register")
    public Result<Long> register(@Valid @RequestBody RegisterRequest req) {
        SysUser user = userService.register(req);
        return Result.ok(user.getId());
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authorization) {
        authService.logout(authorization);
        return Result.ok();
    }

    @PostMapping("/refresh")
    public Result<LoginResponse> refresh() {
        return Result.ok(authService.refresh());
    }

    @GetMapping("/user/info")
    public Result<UserInfo> currentUser() {
        return Result.ok(authService.currentUser());
    }
}
