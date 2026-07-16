package com.cloud.common.security;

import com.cloud.common.exception.BusinessException;
import com.cloud.common.result.ResultCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 从 SecurityContext 获取当前登录用户。
 * 兼容两种 principal：
 *   1) 认证服务中为 {@link LoginUser}；
 *   2) 业务服务中为用户名 String（来自网关透传头），userId 存于 Authentication.details。
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /** 仅在认证服务中可用（principal 为 LoginUser） */
    public static LoginUser getCurrentUser() {
        Authentication auth = getAuthentication();
        if (auth == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof LoginUser loginUser) {
            return loginUser;
        }
        throw new BusinessException(ResultCode.UNAUTHORIZED);
    }

    public static Long getCurrentUserId() {
        Authentication auth = getAuthentication();
        if (auth == null) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof LoginUser loginUser) {
            return loginUser.getUserId();
        }
        Object details = auth.getDetails();
        if (details != null) {
            try {
                return Long.valueOf(details.toString());
            } catch (NumberFormatException ignore) {
                return null;
            }
        }
        return null;
    }

    public static String getCurrentUsername() {
        Authentication auth = getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            return null;
        }
        Object principal = auth.getPrincipal();
        return principal instanceof LoginUser loginUser ? loginUser.getUsername() : principal.toString();
    }
}
