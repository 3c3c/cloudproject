-- ========================================
-- 字典类型表添加状态字段
-- ========================================

-- 1. 添加状态字段
ALTER TABLE `sys_dict_type`
ADD COLUMN `status` INT DEFAULT 1 COMMENT '状态：0-禁用，1-启用';

-- 2. 更新现有数据的状态为启用
UPDATE `sys_dict_type` SET `status` = 1 WHERE `status` IS NULL;

-- 3. 添加索引以提高查询效率
ALTER TABLE `sys_dict_type`
ADD INDEX `idx_status` (`status`) COMMENT '状态索引';
