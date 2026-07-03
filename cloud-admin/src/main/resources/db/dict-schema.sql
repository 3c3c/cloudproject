-- ========================================
-- 字典表双表设计SQL脚本
-- ========================================

-- 1. 字典类型表（存储下拉框的大类）
CREATE TABLE `sys_dict_type` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dict_name` VARCHAR(100) NOT NULL COMMENT '字典类型名称，如：性别、学历、用户状态',
  `dict_code` VARCHAR(50) NOT NULL COMMENT '字典类型编码，如：gender、education、user_status',
  `sort_order` INT DEFAULT 0 COMMENT '排序号',
  `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
  `created_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict_code` (`dict_code`) COMMENT '字典类型编码唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典类型表';

-- 2. 字典数据表（存储大类下的具体选项）
CREATE TABLE `sys_dict_data` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dict_type_id` BIGINT NOT NULL COMMENT '字典类型ID，关联sys_dict_type表',
  `dict_label` VARCHAR(100) NOT NULL COMMENT '字典标签，显示值，如：男、女',
  `dict_value` VARCHAR(100) NOT NULL COMMENT '字典值，实际值，如：1、0',
  `sort_order` INT DEFAULT 0 COMMENT '排序号',
  `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
  `created_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_dict_type_id` (`dict_type_id`) COMMENT '字典类型ID索引',
  CONSTRAINT `fk_dict_data_type` FOREIGN KEY (`dict_type_id`) REFERENCES `sys_dict_type` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典数据表';

-- 3. 初始化示例数据

-- 字典类型示例数据
INSERT INTO `sys_dict_type` (`dict_name`, `dict_code`, `sort_order`, `remark`, `created_by`) VALUES
('性别', 'gender', 1, '用户性别字典', 'system'),
('学历', 'education', 2, '用户学历字典', 'system'),
('用户状态', 'user_status', 3, '用户启用/禁用状态', 'system'),
('是否', 'yes_no', 4, '通用是否选项', 'system');

-- 字典数据示例数据（性别）
INSERT INTO `sys_dict_data` (`dict_type_id`, `dict_label`, `dict_value`, `sort_order`, `remark`, `created_by`) VALUES
(1, '男', '1', 1, '男性', 'system'),
(1, '女', '2', 2, '女性', 'system');

-- 字典数据示例数据（学历）
INSERT INTO `sys_dict_data` (`dict_type_id`, `dict_label`, `dict_value`, `sort_order`, `remark`, `created_by`) VALUES
(2, '小学', 'primary', 1, '小学学历', 'system'),
(2, '初中', 'junior', 2, '初中学历', 'system'),
(2, '高中', 'senior', 3, '高中学历', 'system'),
(2, '本科', 'bachelor', 4, '本科学历', 'system'),
(2, '硕士', 'master', 5, '硕士研究生', 'system'),
(2, '博士', 'doctor', 6, '博士研究生', 'system');

-- 字典数据示例数据（用户状态）
INSERT INTO `sys_dict_data` (`dict_type_id`, `dict_label`, `dict_value`, `sort_order`, `remark`, `created_by`) VALUES
(3, '启用', '1', 1, '用户启用状态', 'system'),
(3, '禁用', '0', 2, '用户禁用状态', 'system');

-- 字典数据示例数据（是否）
INSERT INTO `sys_dict_data` (`dict_type_id`, `dict_label`, `dict_value`, `sort_order`, `remark`, `created_by`) VALUES
(4, '是', '1', 1, '是', 'system'),
(4, '否', '0', 2, '否', 'system');
