-- ========================================
-- 字典类型表添加树形结构支持
-- ========================================

-- 1. 修改 sys_dict_type 表，添加树形结构字段
ALTER TABLE `sys_dict_type`
ADD COLUMN `parent_id` BIGINT DEFAULT 0 COMMENT '父级ID，0表示根节点';

-- 2. 添加索引
ALTER TABLE `sys_dict_type`
ADD INDEX `idx_parent_id` (`parent_id`) COMMENT '父级ID索引';

-- 3. 更新现有数据为根节点、
UPDATE `sys_dict_type` SET `parent_id` = 0;

-- 4. 示例：创建有层级关系的字典类型数据
-- 先插入根节点
INSERT INTO `sys_dict_type` (`dict_name`, `dict_code`, `parent_id`, `sort_order`, `remark`, `created_by`) VALUES
('系统配置', 'system_config', 0, 10, '系统相关配置', 'system'),
('业务配置', 'business_config', 0, 20, '业务相关配置', 'system');

-- 插入系统配置的子节点
INSERT INTO `sys_dict_type` (`dict_name`, `dict_code`, `parent_id`, `sort_order`, `remark`, `created_by`) VALUES
('用户配置', 'user_config', 1, 1, '用户相关配置', 'system'),
('权限配置', 'permission_config', 1, 2, '权限相关配置', 'system');

-- 插入用户配置的子节点
INSERT INTO `sys_dict_type` (`dict_name`, `dict_code`, `parent_id`, `sort_order`, `remark`, `created_by`) VALUES
('用户状态', 'user_status', 3, 1, '用户启用/禁用状态', 'system'),
('用户类型', 'user_type', 3, 2, '用户类型分类', 'system');

-- 插入权限配置的子节点
INSERT INTO `sys_dict_type` (`dict_name`, `dict_code`, `parent_id`, `sort_order`, `remark`, `created_by`) VALUES
('角色类型', 'role_type', 4, 1, '角色类型分类', 'system');

-- 插入业务配置的子节点
INSERT INTO `sys_dict_type` (`dict_name`, `dict_code`, `parent_id`, `sort_order`, `remark`, `created_by`) VALUES
('订单配置', 'order_config', 2, 1, '订单相关配置', 'system'),
('商品配置', 'product_config', 2, 2, '商品相关配置', 'system');

-- 插入订单配置的子节点
INSERT INTO `sys_dict_type` (`dict_name`, `dict_code`, `parent_id`, `sort_order`, `remark`, `created_by`) VALUES
('订单状态', 'order_status', 7, 1, '订单状态分类', 'system'),
('支付方式', 'payment_method', 7, 2, '支付方式分类', 'system');
