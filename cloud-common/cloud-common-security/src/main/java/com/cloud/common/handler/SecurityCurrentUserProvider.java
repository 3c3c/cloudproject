package com.cloud.common.handler;

import com.cloud.common.security.SecurityUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * {@link CurrentUserProvider} 的安全实现：委托给 {@link SecurityUtils}，从 SecurityContext 取真实登录用户。
 *
 * <p>以 {@code @Primary} + {@code @ConditionalOnClass} 优先生效，覆盖 cloud-common 的
 * {@link DefaultCurrentUserProvider}，使依赖 security 模块的服务能填充真实操作人。</p>
 */
@Component
@Primary
@ConditionalOnClass(name = "org.springframework.security.core.context.SecurityContextHolder")
public class SecurityCurrentUserProvider implements CurrentUserProvider {

    @Override
    public String getCurrentUsername() {
        return SecurityUtils.getCurrentUsername();
    }
}
