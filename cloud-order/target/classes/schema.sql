-- =====================================================
-- cloud_order 库订单表（幂等，可重复执行）
-- 表名使用 t_order 避开 SQL 关键字 ORDER
-- 数据库 cloud_order 由连接串 createDatabaseIfNotExist=true 自动创建
-- =====================================================

CREATE TABLE IF NOT EXISTS t_order (
    id           BIGINT          NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    order_no     VARCHAR(64)     NOT NULL COMMENT '订单号',
    user_id      BIGINT          DEFAULT NULL COMMENT '下单用户ID',
    product_id   BIGINT          DEFAULT NULL COMMENT '产品ID',
    product_name VARCHAR(128)    DEFAULT NULL COMMENT '产品名称（冗余）',
    quantity     INT             DEFAULT 1 COMMENT '数量',
    amount       DECIMAL(10, 2)  DEFAULT 0 COMMENT '金额',
    status       INT             DEFAULT 1 COMMENT '状态：1已下单 2已支付 3已取消',
    create_time  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '订单表';
