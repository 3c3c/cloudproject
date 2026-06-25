# cloud-admin 后台管理服务

## 模块描述
后台管理服务，提供用户管理、角色管理、权限管理等系统管理功能。

## 主要功能
- 用户管理：创建、编辑、删除、查询用户
- 角色管理：创建、编辑、删除、查询角色
- 权限管理：创建、编辑、删除、查询权限
- 系统配置：管理系统参数和配置

## 技术栈
- Spring Boot 3.2.5
- Spring Cloud 2023.0.1
- Spring Cloud Alibaba 2023.0.1.0
- MyBatis Plus 3.5.5
- Nacos (服务发现 + 配置中心)
- MapStruct (实体转换)

## 端口配置
- 服务端口：8084
- 健康检查：http://localhost:8084/admin/health

## 依赖关系
- cloud-common：公共模块
- cloud-auth：认证服务（调用用户角色权限管理接口）

## 启动顺序
1. Nacos Server
2. cloud-admin

## 配置说明
- 数据库配置：连接到cloud_auth数据库
- Nacos配置：服务发现和配置中心

## API接口
### 健康检查
- GET `/admin/health` - 服务健康状态检查

### 用户管理
- 待实现...

### 角色管理
- 待实现...

### 权限管理
- 待实现...