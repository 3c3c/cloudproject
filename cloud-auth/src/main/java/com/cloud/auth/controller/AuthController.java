package com.cloud.auth.controller;

import cn.hutool.crypto.asymmetric.RSA;
import com.cloud.auth.dto.login.LoginRequest;
import com.cloud.auth.dto.login.LoginResponse;
import com.cloud.auth.dto.register.RegisterRequest;
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
 * 认证接口：登录 / 注册 / 退出 / 刷新。
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final RSA rsaObject;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return Result.success(authService.login(req));
    }

    /**
     * 获取RSA公钥接口
     * 前端调用此接口获取公钥，用于加密登录密码
     *
     * @return Base64编码的公钥字符串
     */
    @GetMapping("/public-key")
    public Result<String> getPublicKey() {
        String publicKeyBase64 = rsaObject.getPublicKeyBase64();
        return Result.success(publicKeyBase64);
    }

    @PostMapping("/register")
    public Result<Long> register(@Valid @RequestBody RegisterRequest req) {
        SysUser user = userService.register(req);
        return Result.success(user.getId());
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authorization) {
        authService.logout(authorization);
        return Result.success();
    }

    @PostMapping("/refresh")
    public Result<LoginResponse> refresh() {
        return Result.success(authService.refresh());
    }
}
