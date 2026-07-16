package com.cloud.common.handler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * {@link CurrentUserProvider} 的默认实现：返回 {@code "system"}。
 *
 * <p>当应用未引入 cloud-common-security（即没有更具体的覆盖实现）时生效，
 * 保证纯净服务也能正常填充审计字段。</p>
 */
@Component
@ConditionalOnMissingBean(CurrentUserProvider.class)
public class DefaultCurrentUserProvider implements CurrentUserProvider {

    @Override
    public String getCurrentUsername() {
        return "system";
    }
}
