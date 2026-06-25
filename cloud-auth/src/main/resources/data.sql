-- =====================================================
-- cloud_auth 初始数据（幂等，可重复执行）
-- !! password 暂以明文写入，应用启动后由 PasswordDataInitializer 自动转为 BCrypt
-- =====================================================

-- 角色
INSERT IGNORE INTO sys_role (id, role_code, role_name, remark, enabled) VALUES
    (1, 'ROLE_ADMIN', '管理员',  '系统超级管理员', 1),
    (2, 'ROLE_USER',  '普通用户', '默认注册用户', 1);

-- 权限（service_code 标识所属产品/服务）
INSERT IGNORE INTO sys_permission (id, perm_code, perm_name, service_code) VALUES
    (1, 'product:query',  '产品查询', 'product'),
    (2, 'product:add',    '产品新增', 'product'),
    (3, 'product:update', '产品修改', 'product'),
    (4, 'product:delete', '产品删除', 'product'),
    (5, 'order:query',    '订单查询', 'order'),
    (6, 'order:create',   '订单创建', 'order'),
    (7, 'order:update',   '订单修改', 'order'),
    (8, 'order:delete',   '订单删除', 'order');

-- 用户（密码明文 123456，启动后自动加密）
INSERT IGNORE INTO sys_user (id, username, nickname, password, mobile, email, enabled, must_change_password) VALUES
    (1, 'admin', '超级管理员', '123456', '13800000001', 'admin@cloud.com', 1, 0),
    (2, 'user',  '测试用户',   '123456', '13800000002', 'user@cloud.com',  1, 0);

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
