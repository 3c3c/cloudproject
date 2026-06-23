package com.cloud.auth.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * 拦截 POST /auth/sms/login（表单参数 mobile、code），封装为 MobileAuthenticationToken
 * 交给 AuthenticationManager 认证（aaa.md 第二节）。
 */
public class MobileAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final AntPathRequestMatcher MOBILE_LOGIN_MATCHER =
            new AntPathRequestMatcher("/auth/sms/login", "POST");

    private final String mobileParameter = "mobile";
    private final String codeParameter = "code";

    public MobileAuthenticationFilter() {
        super(MOBILE_LOGIN_MATCHER);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        if (!"POST".equals(request.getMethod())) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }
        String mobile = request.getParameter(mobileParameter);
        String code = request.getParameter(codeParameter);
        if (mobile == null) {
            mobile = "";
        }
        if (code == null) {
            code = "";
        }
        mobile = mobile.trim();

        MobileAuthenticationToken authRequest = new MobileAuthenticationToken(mobile, code);
        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
        return this.getAuthenticationManager().authenticate(authRequest);
    }
}
