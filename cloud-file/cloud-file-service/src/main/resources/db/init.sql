-- cloud-file 模块数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS cloud_file DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE cloud_file;

-- 创建文件信息表
CREATE TABLE IF NOT EXISTS file_info (
    id                  BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    file_key            VARCHAR(255) NOT NULL COMMENT '文件唯一标识（存储路径）',
    original_file_name  VARCHAR(255) NOT NULL COMMENT '原始文件名',
    file_extension      VARCHAR(50) COMMENT '文件扩展名',
    file_size           BIGINT NOT NULL COMMENT '文件大小（字节）',
    content_type        VARCHAR(100) COMMENT 'MIME 类型',
    storage_type        VARCHAR(20) NOT NULL COMMENT '存储类型（minio/oss）',
    file_url            VARCHAR(500) COMMENT '访问 URL',
    file_md5            VARCHAR(32) COMMENT '文件 MD5（用于去重）',
    business_type       VARCHAR(50) COMMENT '业务类型（avatar/product/order）',
    business_id         BIGINT COMMENT '业务 ID',
    deleted             TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    create_time         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by          VARCHAR(64) COMMENT '创建人',
    updated_by          VARCHAR(64) COMMENT '更新人',
    PRIMARY KEY (id),
    UNIQUE KEY uk_file_key (file_key),
    KEY idx_md5 (file_md5),
    KEY idx_business (business_type, business_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件信息表';

-- 插入测试数据
INSERT INTO file_info (file_key, original_file_name, file_extension, file_size,
    content_type, storage_type, file_url, file_md5, business_type, business_id)
VALUES
('avatar/2026-06-30/a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg',
 'profile.jpg', 'jpg', 102400, 'image/jpeg', 'minio',
 'http://localhost:9001/file/download?key=avatar/2026-06-30/a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg',
 'd41d8cd98f00b204e9800998ecf8427e', 'avatar', NULL);