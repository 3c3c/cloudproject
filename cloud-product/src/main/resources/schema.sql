-- =====================================================
-- cloud_product 库产品表（幂等，可重复执行）
-- 数据库 cloud_product 由连接串 createDatabaseIfNotExist=true 自动创建
-- =====================================================

CREATE TABLE IF NOT EXISTS product (
    id          BIGINT          NOT NULL AUTO_INCREMENT COMMENT '产品ID',
    name        VARCHAR(128)    NOT NULL COMMENT '产品名称',
    description VARCHAR(255)    DEFAULT NULL COMMENT '产品描述',
    price       DECIMAL(10, 2)  DEFAULT 0 COMMENT '价格',
    stock       INT             DEFAULT 0 COMMENT '库存',
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '产品表';

INSERT IGNORE INTO product (id, name, description, price, stock) VALUES
    (1, '云服务器 ECS', '2核4G 通用型入门款', 199.00, 1000),
    (2, '对象存储 OSS', '标准存储包 1TB',      99.00, 9999),
    (3, '云数据库 RDS', 'MySQL 8.0 高可用版',  599.00, 500);
