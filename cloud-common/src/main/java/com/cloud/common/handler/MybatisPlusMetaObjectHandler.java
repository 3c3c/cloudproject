package com.cloud.common.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.cloud.common.security.SecurityUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis Plus 元数据处理器，用于自动填充审计字段
 */
@Component
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

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
     * 获取当前登录用户名
     */
    private String getCurrentUsername() {
        try {
            return SecurityUtils.getCurrentUsername();
        } catch (Exception e) {
            // 如果无法获取当前用户，使用默认值
            return "system";
        }
    }
}