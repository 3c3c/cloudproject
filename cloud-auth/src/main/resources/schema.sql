-- =====================================================
-- cloud_auth 库 RBAC 表结构（幂等，可重复执行）
-- 注意：数据库 cloud_auth 需事先创建（连接串 createDatabaseIfNotExist=true 会自动建库）
-- =====================================================

CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT          NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username    VARCHAR(64)     NOT NULL COMMENT '用户名',
    password    VARCHAR(128)    NOT NULL DEFAULT '' COMMENT '密码（BCrypt）',
    mobile      VARCHAR(20)     DEFAULT NULL COMMENT '手机号',
    enabled     TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '是否启用 1是 0否',
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_mobile (mobile)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

CREATE TABLE IF NOT EXISTS sys_role (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    role_code   VARCHAR(64)     NOT NULL COMMENT '角色编码，形如 ROLE_ADMIN',
    role_name   VARCHAR(64)     NOT NULL COMMENT '角色名称',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '角色表';

CREATE TABLE IF NOT EXISTS sys_permission (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    perm_code   VARCHAR(64)     NOT NULL COMMENT '权限编码，形如 product:add',
    perm_name   VARCHAR(64)     NOT NULL COMMENT '权限名称',
    PRIMARY KEY (id),
    UNIQUE KEY uk_perm_code (perm_code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '权限表';

CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户-角色关联表';

CREATE TABLE IF NOT EXISTS sys_role_permission (
    role_id BIGINT NOT NULL,
    perm_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, perm_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '角色-权限关联表';
