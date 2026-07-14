-- =============================================
-- 文件信息表 (file_info) 建表语句
-- =============================================
-- 作者: Claude Code
-- 创建时间: 2025-01-14
-- 描述: 存储文件元数据信息，支持多种存储类型和业务场景
-- =============================================

DROP TABLE IF EXISTS `file_info`;

CREATE TABLE `file_info` (
    -- 主键
                             `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 文件基本信息
                             `file_key` VARCHAR(255) NOT NULL COMMENT '文件唯一标识（存储路径，格式：日期/随机码.扩展名）',
                             `original_file_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
                             `file_extension` VARCHAR(50) NOT NULL COMMENT '文件扩展名（如：.jpg, .pdf, .docx）',
                             `file_size` BIGINT(20) NOT NULL COMMENT '文件大小（字节）',
                             `content_type` VARCHAR(100) NOT NULL COMMENT 'MIME类型（如：image/jpeg, application/pdf）',
                             `storage_type` VARCHAR(50) NOT NULL DEFAULT 'minio' COMMENT '存储类型（minio/aliyun-oss/aws-s3）',
                             `file_url` TEXT COMMENT '文件访问URL（完整路径）',
                             `file_md5` VARCHAR(32) DEFAULT NULL COMMENT '文件MD5值（用于去重校验）',

    -- 业务关联信息
                             `business_type` VARCHAR(50) DEFAULT NULL COMMENT '业务类型（avatar/document/image/video/audio等）',
                             `business_id` BIGINT(20) DEFAULT NULL COMMENT '业务关联ID（如用户ID、订单ID等）',

    -- 审计字段
                             `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             `created_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
                             `updated_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',

    -- 逻辑删除
                             `deleted` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，非0-删除时间戳',

    -- 主键约束
                             PRIMARY KEY (`id`),

    -- 唯一索引：文件唯一标识
                             UNIQUE KEY `uk_file_key` (`file_key`),

    -- 普通索引：业务查询优化
                             KEY `idx_business_type` (`business_type`),
                             KEY `idx_business_id` (`business_id`),
                             KEY `idx_business_type_id` (`business_type`, `business_id`),

    -- 普通索引：MD5去重查询
                             KEY `idx_file_md5` (`file_md5`),

    -- 普通索引：逻辑删除过滤
                             KEY `idx_deleted` (`deleted`),

    -- 普通索引：创建时间排序
                             KEY `idx_create_time` (`create_time`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件信息表';

-- =============================================
-- 索引说明
-- =============================================
-- 1. uk_file_key: 唯一索引，确保文件唯一标识不重复
-- 2. idx_business_type: 按业务类型查询（如查询所有头像）
-- 3. idx_business_id: 按业务ID查询（如查询某用户的所有文件）
-- 4. idx_business_type_id: 组合索引，支持按业务类型+业务ID精确查询
-- 5. idx_file_md5: MD5去重查询，用于秒传功能
-- 6. idx_deleted: 逻辑删除过滤，提高查询性能
-- 7. idx_create_time: 按创建时间排序，支持时间范围查询

-- =============================================
-- 字段约束说明
-- =============================================
-- 1. NOT NULL字段: id, file_key, original_file_name, file_extension, file_size, content_type, storage_type, create_time, update_time, deleted
-- 2. DEFAULT值: storage_type='minio', create_time=CURRENT_TIMESTAMP, update_time自动更新, deleted=0
-- 3. TEXT类型: file_url（可能很长，不建立索引）

-- =============================================
-- 业务类型枚举说明
-- =============================================
-- avatar      - 用户头像
-- idcard      - 证件照片
-- document    - 通用文档
-- image       - 图片文件
-- video       - 视频文件
-- audio       - 音频文件
-- attachment  - 附件文件
-- contract    - 合同文件
-- certificate - 证书文件
-- report      - 报告文件
-- template    - 模板文件
-- log         - 日志文件
-- backup      - 备份文件
-- temp        - 临时文件
-- other       - 其他类型

-- =============================================
-- 存储类型枚举说明
-- =============================================
-- minio      - MinIO对象存储
-- aliyun-oss - 阿里云OSS
-- aws-s3     - AWS S3
-- local      - 本地存储

-- =============================================
-- 逻辑删除说明
-- =============================================
-- deleted = 0           - 未删除（正常数据）
-- deleted > 0          - 已删除（存储删除时间戳）
-- 查询时需要添加条件: WHERE deleted = 0
-- 删除操作执行: UPDATE file_info SET deleted = UNIX_TIMESTAMP() * 1000 WHERE id = ?

-- =============================================
-- 示例查询语句
-- =============================================

-- 1. 根据 fileKey 查询文件信息
-- SELECT * FROM file_info WHERE file_key = '20250114/abc123def456.jpg' AND deleted = 0;

-- 2. 查询用户所有头像文件
-- SELECT * FROM file_info WHERE business_type = 'avatar' AND business_id = 1001 AND deleted = 0;

-- 3. 查询某日期范围内的文件
-- SELECT * FROM file_info WHERE create_time >= '2025-01-01' AND create_time <= '2025-01-31' AND deleted = 0;

-- 4. 根据 MD5 查询文件（用于去重/秒传）
-- SELECT * FROM file_info WHERE file_md5 = 'abc123def456...' AND deleted = 0;

-- 5. 分页查询文件列表
-- SELECT * FROM file_info WHERE deleted = 0 ORDER BY create_time DESC LIMIT 0, 10;

-- 6. 统计各业务类型的文件数量
-- SELECT business_type, COUNT(*) as count FROM file_info WHERE deleted = 0 GROUP BY business_type;

-- 7. 统计用户文件总大小
-- SELECT SUM(file_size) as total_size FROM file_info WHERE business_id = 1001 AND deleted = 0;

-- =============================================
-- 数据清理策略（可选）
-- =============================================
-- 1. 清理30天前的临时文件
-- DELETE FROM file_info WHERE business_type = 'temp' AND create_time < DATE_SUB(NOW(), INTERVAL 30 DAY) AND deleted = 0;

-- 2. 清理90天前已删除的文件（物理删除）
-- DELETE FROM file_info WHERE deleted > 0 AND deleted < UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 90 DAY)) * 1000;
