# cloud-admin 后台管理服务

## 模块描述
后台管理服务，提供**数据字典**管理功能（字典类型 + 字典数据）。
供前端下拉框、枚举展示等场景使用。

> 说明：用户管理、角色管理、权限管理（RBAC）由 `cloud-auth` 服务提供（`sys_user` / `sys_role` / `sys_permission`），
> 本服务仅负责字典数据，不涉及用户/角色/权限。早期 README 的相关描述已过时，以此为准。

## 主要功能
- 字典类型管理：创建、编辑、删除、状态切换、树形查询、按编码批量查询
- 字典数据管理：分页查询、按字典编码查询数据列表、创建、编辑、批量删除

## 技术栈
- Spring Boot 3.2.5
- Spring Cloud 2023.0.1
- Spring Cloud Alibaba 2023.0.1.0
- MyBatis Plus 3.5.5
- Nacos（服务发现 + 配置中心）
- MapStruct（实体转换）

## 端口配置
- 服务端口：**8002**
- 服务名（注册到 Nacos）：`cloud-admin`
- 健康检查：如需暴露，请在 Nacos `cloud-admin.yaml` 配置 `management.endpoints.web.exposure.include=health`

## 依赖关系
- `cloud-common-security`：公共安全模块（Spring Security + JWT + Redis，传递 cloud-common-core）
- `cloud-message-api`：消息服务 Feign 客户端（供 `NotificationService` 发送通知邮件）

## 启动顺序
1. 基础设施：Nacos / MySQL / Redis（见 `docker/nacos/docker-compose.yml`）
2. cloud-admin

## 配置说明
- 数据库：连接独立的 **`cloud_admin`** 库（不是 cloud_auth）
- Nacos：服务发现 + 配置中心，私有配置在 `cloud-admin.yaml`

## API 接口
所有接口前缀：`/admin`

### 字典类型（/admin/dict/types）
- POST `/admin/dict/types` - 创建字典类型
- PUT `/admin/dict/types/{id}` - 更新字典类型
- DELETE `/admin/dict/types/batch` - 批量删除字典类型
- GET `/admin/dict/types/by-code/{dictCode}` - 按编码查询字典类型
- POST `/admin/dict/types/by-codes` - 按编码批量查询
- GET `/admin/dict/types/tree` - 获取字典类型树
- PUT `/admin/dict/types/{id}/status` - 切换字典类型状态

### 字典数据（/admin/dict/data）
- GET `/admin/dict/data` - 分页查询字典数据
- GET `/admin/dict/data/getDictDataByCode` - 按字典编码查询数据列表
- POST `/admin/dict/data` - 创建字典数据
- PUT `/admin/dict/data/{id}` - 更新字典数据
- DELETE `/admin/dict/data/batch` - 批量删除字典数据
