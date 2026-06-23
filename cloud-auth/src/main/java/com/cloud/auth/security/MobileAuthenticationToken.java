package com.cloud.auth.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * 手机号验证码认证凭证（aaa.md 第二节）。
 */
public class MobileAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal; // 手机号
    private Object credentials;      // 验证码

    /** 未认证：仅包含手机号 + 验证码 */
    public MobileAuthenticationToken(String mobile, String code) {
        super(null);
        this.principal = mobile;
        this.credentials = code;
        setAuthenticated(false);
    }

    /** 认证成功：包含用户信息与权限，credentials 置空 */
    public MobileAuthenticationToken(Object principal, Object credentials,
                                     Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        super.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
