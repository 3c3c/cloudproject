-- =====================================================
-- cloud_auth 初始数据（幂等，可重复执行）
-- password 列已写入 BCrypt 哈希（明文均为 123456，BCrypt 默认强度 10），
-- 可直接被 BCryptPasswordEncoder 校验通过。请勿以明文存储。
-- =====================================================

-- 角色
INSERT IGNORE INTO sys_role (id, role_code, role_name, remark, enabled, created_by, updated_by) VALUES
    (1, 'ROLE_ADMIN', '管理员',  '系统超级管理员', 1, 'SYSTEM', 'SYSTEM'),
    (2, 'ROLE_USER',  '普通用户', '默认注册用户', 1, 'SYSTEM', 'SYSTEM');

-- 权限（service_code 标识所属产品/服务）
INSERT IGNORE INTO sys_permission (id, perm_code, remark, service_code, created_by, updated_by) VALUES
    (1, 'product:query',  '产品查询', 'product', 'SYSTEM', 'SYSTEM'),
    (2, 'product:add',    '产品新增', 'product', 'SYSTEM', 'SYSTEM'),
    (3, 'product:update', '产品修改', 'product', 'SYSTEM', 'SYSTEM'),
    (4, 'product:delete', '产品删除', 'product', 'SYSTEM', 'SYSTEM'),
    (5, 'order:query',    '订单查询', 'order', 'SYSTEM', 'SYSTEM'),
    (6, 'order:create',   '订单创建', 'order', 'SYSTEM', 'SYSTEM'),
    (7, 'order:update',   '订单修改', 'order', 'SYSTEM', 'SYSTEM'),
    (8, 'order:delete',   '订单删除', 'order', 'SYSTEM', 'SYSTEM');

-- 用户（password 已是 BCrypt 哈希，明文 123456）
INSERT IGNORE INTO sys_user (id, username, nickname, password, mobile, email, enabled, must_change_password, created_by, updated_by) VALUES
    (1, 'admin', '超级管理员', '$2a$10$aWBN/GBkfQ7FmFckKK442uo2s3mlz22MLKPAkySwzxIEUL6pNt/LO', '13800000001', 'admin@cloud.com', 1, 0, 'SYSTEM', 'SYSTEM'),
    (2, 'user',  '测试用户',   '$2a$10$W2ZXwoXWZCBuC/A6rn4O/OL920Ur4vJmXhKdZPcYb4AJ5JaV00rHy', '13800000002', 'user@cloud.com',  1, 0, 'SYSTEM', 'SYSTEM');

-- 兜底修复：把历史遗留的明文密码（不以 $2 开头，即非 BCrypt 哈希）批量重置为哈希。
-- 幂等——已经是哈希的行不会被改（password = '$2a...%' 只匹配明文）。
UPDATE sys_user
SET password = CASE
    WHEN id = 1 THEN '$2a$10$aWBN/GBkfQ7FmFckKK442uo2s3mlz22MLKPAkySwzxIEUL6pNt/LO'
    WHEN id = 2 THEN '$2a$10$W2ZXwoXWZCBuC/A6rn4O/OL920Ur4vJmXhKdZPcYb4AJ5JaV00rHy'
END
WHERE id IN (1, 2)
  AND password NOT LIKE '$2%';

-- 用户-角色
INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES
    (1, 1), -- admin -> ADMIN
    (2, 2); -- user  -> USER

-- 角色-权限：ADMIN 拥有全部权限
INSERT IGNORE INTO sys_role_permission (role_id, perm_id) VALUES
    (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8);

-- 角色-权限：USER 仅 product:query + order:query（无 order:create，用于演示 403）
INSERT IGNORE INTO sys_role_permission (role_id, perm_id) VALUES
    (2, 1), (2, 5);
