-- ============================================
-- 消息服务数据库初始化脚本
-- 审计字段（create_time/update_time/created_by/updated_by）与 cloud-common BaseEntity 对齐，
-- 由 MyBatis-Plus 自动填充；逻辑删除字段 deleted 与实体 @TableLogic 配置一致（0=未删除，删除后为时间戳）。
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS cloud_message DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE cloud_message;

-- ============================================
-- 1. 消息模板表（message_template）
-- ============================================
DROP TABLE IF EXISTS message_template;
CREATE TABLE message_template (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法）',
    template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
    template_code VARCHAR(50) NOT NULL UNIQUE COMMENT '模板编码（唯一标识）',
    message_type TINYINT NOT NULL COMMENT '消息类型：1-邮件 2-WebSocket 3-站内信',
    channel VARCHAR(20) NOT NULL COMMENT '渠道：aliyun/tencent/email/websocket/inbox',
    title VARCHAR(200) COMMENT '标题（邮件/站内信使用）',
    content TEXT NOT NULL COMMENT '模板内容（支持变量：${code}、${expireTime}等）',
    variables JSON COMMENT '模板变量定义（可选）',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    updated_by VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    deleted BIGINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 删除后为时间戳',
    INDEX idx_template_code (template_code),
    INDEX idx_message_type (message_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息模板表';

-- ============================================
-- 2. 消息记录表（message_record）
-- ============================================
DROP TABLE IF EXISTS message_record;
CREATE TABLE message_record (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法）',
    template_id BIGINT COMMENT '模板ID',
    message_type TINYINT NOT NULL COMMENT '消息类型：1-邮件 2-WebSocket 3-站内信',
    title VARCHAR(200) COMMENT '消息标题',
    content TEXT NOT NULL COMMENT '消息内容',
    receiver VARCHAR(500) COMMENT '接收者（手机号/邮箱/用户ID，多个用逗号分隔）',
    sender VARCHAR(100) DEFAULT 'system' COMMENT '发送者',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待发送 1-发送中 2-成功 3-失败',
    send_time BIGINT COMMENT '发送时间（毫秒时间戳）',
    error_msg TEXT COMMENT '错误信息',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    updated_by VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    deleted BIGINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 删除后为时间戳',
    INDEX idx_template_id (template_id),
    INDEX idx_status (status),
    INDEX idx_message_type (message_type),
    INDEX idx_create_time (create_time),
    INDEX idx_receiver (receiver)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息记录表';

-- ============================================
-- 3. 消息接收记录表（message_receive_record）
-- ============================================
DROP TABLE IF EXISTS message_receive_record;
CREATE TABLE message_receive_record (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法）',
    message_id BIGINT NOT NULL COMMENT '消息ID（关联 message_record）',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    read_status TINYINT DEFAULT 0 COMMENT '阅读状态：0-未读 1-已读',
    read_time BIGINT COMMENT '阅读时间（毫秒时间戳）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    updated_by VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    deleted BIGINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 删除后为时间戳',
    UNIQUE KEY uk_user_message (user_id, message_id),
    INDEX idx_user_id (user_id),
    INDEX idx_message_id (message_id),
    INDEX idx_read_status (read_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息接收记录表（站内信）';

-- ============================================
-- 4. 消息配置表（message_config）
-- ============================================
DROP TABLE IF EXISTS message_config;
CREATE TABLE message_config (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法）',
    config_key VARCHAR(50) NOT NULL UNIQUE COMMENT '配置键（唯一标识）',
    config_value TEXT NOT NULL COMMENT '配置值（JSON 格式）',
    description VARCHAR(200) COMMENT '配置描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    updated_by VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    deleted BIGINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 删除后为时间戳',
    INDEX idx_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息配置表';

-- ============================================
-- 初始化数据
-- message_type 取值与 MessageType 枚举一致：1-EMAIL 2-WEBSOCKET 3-INBOX
-- ============================================

-- 插入示例模板
INSERT INTO message_template (id, template_name, template_code, message_type, channel, title, content, variables, status, create_time, update_time, created_by, updated_by, deleted) VALUES
(2, '注册欢迎邮件', 'EMAIL_REGISTER_WELCOME', 1, 'email', '欢迎注册', '亲爱的${username}：\n\n欢迎注册我们的平台！\n\n您的账号：${username}\n注册时间：${registerTime}\n\n如有任何问题，请联系客服。', '{"username":"用户名","registerTime":"注册时间"}', 1, NOW(), NOW(), 'system', 'system', 0),
(3, '系统通知站内信', 'INBOX_SYSTEM_NOTICE', 3, 'inbox', '系统通知', '${content}', '{"content":"通知内容"}', 1, NOW(), NOW(), 'system', 'system', 0);

-- 插入示例配置
INSERT INTO message_config (id, config_key, config_value, description, create_time, update_time, created_by, updated_by, deleted) VALUES
(4, 'email.smtp.host', '{"value":"smtp.qq.com"}', '邮件 SMTP 服务器地址', NOW(), NOW(), 'system', 'system', 0),
(5, 'email.smtp.port', '{"value":"587"}', '邮件 SMTP 端口', NOW(), NOW(), 'system', 'system', 0),
(6, 'email.smtp.username', '{"value":"your-email@qq.com"}', '邮件发送账号', NOW(), NOW(), 'system', 'system', 0),
(7, 'email.smtp.password', '{"value":"your-password"}', '邮件发送密码或授权码', NOW(), NOW(), 'system', 'system', 0);
