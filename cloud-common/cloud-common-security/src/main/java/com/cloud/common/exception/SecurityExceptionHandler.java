package com.cloud.common.exception;

import com.cloud.common.result.Result;
import com.cloud.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 安全相关异常处理（认证 / 鉴权），与 {@link GlobalExceptionHandler} 配合使用。
 *
 * <p>仅当 classpath 存在 Spring Security 时才激活（{@link ConditionalOnClass}），
 * 这样不依赖 security 的纯净服务（如各 api 模块）在扫描到本类时不会因缺类而启动失败。</p>
 */
@Slf4j
@RestControllerAdvice
@ConditionalOnClass(AuthenticationException.class)
public class SecurityExceptionHandler {

    /** 认证异常：未登录 / token 失效 */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleAuthentication(AuthenticationException e) {
        log.warn("认证异常: {}", e.getMessage());
        return Result.error(ResultCode.UNAUTHORIZED, e.getMessage());
    }

    /** 权限不足 */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDenied(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        return Result.error(ResultCode.FORBIDDEN);
    }
}
