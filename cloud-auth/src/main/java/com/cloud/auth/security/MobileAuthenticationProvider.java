package com.cloud.auth.security;

import com.cloud.auth.dto.register.RegisterRequest;
import com.cloud.auth.service.SmsService;
import com.cloud.auth.service.UserService;
import com.cloud.common.exception.BusinessException;
import com.cloud.common.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 手机号验证码认证逻辑（aaa.md 第二节）：校验验证码 -> 加载或自动注册用户 -> 返回已认证 Token。
 */
@Component
@RequiredArgsConstructor
public class MobileAuthenticationProvider implements AuthenticationProvider {

    private final SmsService smsService;
    private final UserService userService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String mobile = (String) authentication.getPrincipal();
        String code = (String) authentication.getCredentials();

        // 1. 校验验证码
        try {
            smsService.verifyCode(mobile, code);
        } catch (BusinessException e) {
            throw new BadCredentialsException(e.getMessage());
        }

        // 2. 加载用户，不存在则自动注册（手机号登录首次自动开户）
        LoginUser loginUser = userService.loadLoginUserByMobile(mobile);
        if (loginUser == null) {
            loginUser = autoRegister(mobile);
        }

        // 3. 返回认证成功的 Token
        MobileAuthenticationToken result =
                new MobileAuthenticationToken(loginUser, null, loginUser.getAuthorities());
        result.setDetails(authentication.getDetails());
        return result;
    }

    private LoginUser autoRegister(String mobile) {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("user_" + mobile);
        // 用户不会用密码登录，随机生成（register 内部会 BCrypt 加密）
        req.setPassword(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        req.setMobile(mobile);
        userService.register(req);
        return userService.loadLoginUserByMobile(mobile);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return MobileAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
