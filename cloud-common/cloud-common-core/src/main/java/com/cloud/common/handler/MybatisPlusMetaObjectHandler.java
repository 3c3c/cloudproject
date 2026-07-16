package com.cloud.common.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis Plus 元数据处理器，用于自动填充审计字段。
 *
 * <p>当前操作人通过 {@link CurrentUserProvider} SPI 获取，不直接依赖 Spring Security。
 * 需要鉴权的服务依赖 cloud-common-security 后，其 {@code SecurityCurrentUserProvider}
 * 会覆盖默认实现，自动填充真实登录用户。</p>
 */
@Component
@RequiredArgsConstructor
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

    private final CurrentUserProvider currentUserProvider;

    @Override
    public void insertFill(MetaObject metaObject) {
        // 插入时自动填充创建时间和更新时间
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

        // 插入时自动填充创建人和更新人
        String currentUser = getCurrentUsername();
        this.strictInsertFill(metaObject, "createdBy", String.class, currentUser);
        this.strictInsertFill(metaObject, "updatedBy", String.class, currentUser);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时自动填充更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

        // 更新时自动填充更新人
        String currentUser = getCurrentUsername();
        this.strictUpdateFill(metaObject, "updatedBy", String.class, currentUser);
    }

    /**
     * 获取当前登录用户名，无法获取时回退为 "system"。
     */
    private String getCurrentUsername() {
        try {
            String username = currentUserProvider.getCurrentUsername();
            return username != null ? username : "system";
        } catch (Exception e) {
            return "system";
        }
    }
}