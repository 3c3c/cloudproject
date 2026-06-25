# Cloud Auth 认证服务 API 文档

## 基础信息
- **服务名称**: cloud-auth
- **基础路径**: `/auth`
- **端口**: 8081
- **内容类型**: application/json
- **字符编码**: UTF-8

---

## 认证接口

### 1. 用户登录
**接口描述**: 用户名密码登录

**请求信息**:
- **接口路径**: `POST /auth/login`
- **请求头**: `Content-Type: application/json`

**请求参数**:
```json
{
  "username": "string (用户名)",
  "password": "string (密码)"
}
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 |
| ------ | ---- | ---- | ---- |
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "string (JWT令牌)",
    "tokenHead": "string (令牌前缀，如 Bearer )",
    "userId": 1234567890123456789,
    "username": "string (用户名)",
    "avatar": "string (头像URL)",
    "authorities": ["string (权限列表)"],
    "mustChangePassword": false
  }
}
```

---

### 2. 用户注册
**接口描述**: 新用户注册

**请求信息**:
- **接口路径**: `POST /auth/register`
- **请求头**: `Content-Type: application/json`

**请求参数**:
```json
{
  "username": "string (用户名)",
  "nickname": "string (昵称)",
  "password": "string (密码)",
  "mobile": "string (手机号)",
  "email": "string (邮箱)",
  "avatar": "string (头像URL，可选)"
}
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 |
| ------ | ---- | ---- | ---- |
| username | String | 是 | 用户名（必须唯一） |
| nickname | String | 是 | 昵称 |
| password | String | 是 | 密码（6-20位） |
| mobile | String | 否 | 手机号 |
| email | String | 否 | 邮箱 |
| avatar | String | 否 | 头像URL |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": 1234567890123456789
}
```

---

### 3. 用户登出
**接口描述**: 用户退出登录

**请求信息**:
- **接口路径**: `POST /auth/logout`
- **请求头**: 
  - `Authorization: Bearer {token}` (必需)

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 4. 刷新令牌
**接口描述**: 刷新访问令牌

**请求信息**:
- **接口路径**: `POST /auth/refresh`
- **请求头**: 
  - `Authorization: Bearer {token}` (必需)

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "string (新的JWT令牌)",
    "tokenHead": "string (令牌前缀)",
    "userId": 1234567890123456789,
    "username": "string (用户名)",
    "avatar": "string (头像URL)",
    "authorities": ["string (权限列表)"],
    "mustChangePassword": false
  }
}
```

---

## 短信验证码登录（通过MobileAuthenticationFilter处理）
**接口描述**: 手机号验证码登录

**请求信息**:
- **接口路径**: `POST /auth/sms/login`
- **请求头**: `Content-Type: application/json`

**请求参数**:
```json
{
  "mobile": "string (手机号)",
  "code": "string (验证码)",
  "client_id": "string (客户端ID)",
  "client_secret": "string (客户端密钥)"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "string (JWT令牌)",
    "tokenHead": "string (令牌前缀)",
    "userId": 1234567890123456789,
    "username": "string (用户名)",
    "avatar": "string (头像URL)",
    "authorities": ["string (权限列表)"],
    "mustChangePassword": false
  }
}
```

---

### 5. 发送验证码
**接口描述**: 发送短信验证码

**请求信息**:
- **接口路径**: `POST /auth/sms/send`
- **请求头**: `Content-Type: application/json`

**请求参数**:
```json
{
  "mobile": "string (手机号)"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

## 用户管理接口

> **认证要求**: 以下所有接口都需要在请求头中携带有效的JWT令牌
> `Authorization: Bearer {token}`

### 6. 分页查询用户列表
**接口描述**: 根据用户名称或账号，分页查询用户列表

**请求信息**:
- **接口路径**: `GET /users`
- **请求头**: `Authorization: Bearer {token}`

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| ------ | ---- | ---- | ------ | ---- |
| current | Integer | 否 | 1 | 当前页码 |
| size | Integer | 否 | 10 | 每页大小 |
| keyword | String | 否 | - | 用户名或手机号（模糊搜索） |

**请求示例**:
```
GET /users?current=1&size=10&keyword=admin
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1234567890123456789,
        "username": "admin",
        "nickname": "管理员",
        "mobile": "13800138000",
        "email": "admin@example.com",
        "avatar": "https://example.com/avatar.jpg",
        "enabled": 1,
        "mustChangePassword": false,
        "createTime": "2024-01-01T10:00:00",
        "updateTime": "2024-01-01T10:00:00",
        "createdBy": "system",
        "updatedBy": "admin"
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  }
}
```

**UserResponse 参数说明**:
| 参数名 | 类型 | 说明 |
| ------ | ---- | ---- |
| id | Long | 用户ID |
| username | String | 用户名 |
| nickname | String | 昵称 |
| mobile | String | 手机号 |
| email | String | 邮箱 |
| avatar | String | 头像URL |
| enabled | Integer | 状态（1启用 0禁用） |
| mustChangePassword | Boolean | 是否必须修改密码 |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |
| createdBy | String | 创建人 |
| updatedBy | String | 更新人 |

---

### 7. 创建用户
**接口描述**: 创建新用户

**请求信息**:
- **接口路径**: `POST /users`
- **请求头**: 
  - `Content-Type: application/json`
  - `Authorization: Bearer {token}`

**请求参数**:
```json
{
  "username": "string (用户名)",
  "nickname": "string (昵称)",
  "password": "string (密码)",
  "mobile": "string (手机号)",
  "email": "string (邮箱)",
  "avatar": "string (头像URL)",
  "enabled": 1,
  "mustChangePassword": false
}
```

**参数说明**:
| 参数名 | 类型 | 必填 | 验证规则 | 说明 |
| ------ | ---- | ---- | -------- | ---- |
| username | String | 是 | 最大50字符 | 用户名 |
| nickname | String | 否 | 最大50字符 | 昵称 |
| password | String | 是 | 6-20字符 | 密码 |
| mobile | String | 否 | 最大20字符 | 手机号 |
| email | String | 否 | 最大100字符 | 邮箱 |
| avatar | String | 否 | 最大200字符 | 头像URL |
| enabled | Integer | 否 | - | 状态（1启用 0禁用） |
| mustChangePassword | Boolean | 否 | - | 是否必须修改密码 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1234567890123456789,
    "username": "newuser",
    "nickname": "新用户",
    "mobile": "13800138000",
    "email": "newuser@example.com",
    "avatar": "https://example.com/avatar.jpg",
    "enabled": 1,
    "mustChangePassword": false,
    "createTime": "2024-01-01T10:00:00",
    "updateTime": "2024-01-01T10:00:00",
    "createdBy": "admin",
    "updatedBy": "admin"
  }
}
```

---

### 8. 更新用户
**接口描述**: 更新指定用户的信息

**请求信息**:
- **接口路径**: `PUT /users/{id}`
- **请求头**: 
  - `Content-Type: application/json`
  - `Authorization: Bearer {token}`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 用户ID |

**请求参数**: 同创建用户

**请求示例**:
```
PUT /users/1234567890123456789
```

**响应示例**: 同创建用户

---

### 9. 删除用户
**接口描述**: 删除指定用户

**请求信息**:
- **接口路径**: `DELETE /users/{id}`
- **请求头**: `Authorization: Bearer {token}`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 用户ID |

**请求示例**:
```
DELETE /users/1234567890123456789
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 10. 批量删除用户
**接口描述**: 批量删除多个用户

**请求信息**:
- **接口路径**: `DELETE /users/batch`
- **请求头**: 
  - `Content-Type: application/json`
  - `Authorization: Bearer {token}`

**请求参数**:
```json
[1234567890123456789, 1234567890123456790, 1234567890123456791]
```

**参数说明**: 用户ID列表（Long数组）

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 11. 根据ID查询用户
**接口描述**: 根据用户ID查询用户详情

**请求信息**:
- **接口路径**: `GET /users/{id}`
- **请求头**: `Authorization: Bearer {token}`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 用户ID |

**请求示例**:
```
GET /users/1234567890123456789
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1234567890123456789,
    "username": "admin",
    "nickname": "管理员",
    "mobile": "13800138000",
    "email": "admin@example.com",
    "avatar": "https://example.com/avatar.jpg",
    "enabled": 1,
    "mustChangePassword": false,
    "createTime": "2024-01-01T10:00:00",
    "updateTime": "2024-01-01T10:00:00",
    "createdBy": "system",
    "updatedBy": "admin"
  }
}
```

---

### 12. 更新用户状态
**接口描述**: 更新指定用户的启用/禁用状态

**请求信息**:
- **接口路径**: `PUT /users/{id}/status`
- **请求头**: `Authorization: Bearer {token}`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 用户ID |

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 |
| ------ | ---- | ---- | ---- |
| enabled | Integer | 是 | 状态值（1启用 0禁用） |

**请求示例**:
```
PUT /users/1234567890123456789/status?enabled=1
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

## 错误码说明
| 错误码 | 说明 |
| ------ | ---- |
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

**错误响应示例**:
```json
{
  "code": 400,
  "message": "用户名已存在",
  "data": null
}
```

---

## 注意事项

1. **认证要求**: 所有用户管理接口都需要在请求头中携带有效的JWT令牌
2. **时间格式**: 所有时间字段使用 ISO 8601 格式（如：`2024-01-01T10:00:00`）
3. **分页参数**: 分页接口的 `current` 从 1 开始计数
4. **状态值**: `enabled` 字段使用整数类型：`1` 表示启用，`0` 表示禁用
5. **令牌格式**: 使用 `Bearer` 令牌认证，格式为 `Authorization: Bearer {token}`
6. **验证规则**: 所有必填字段未通过验证时会返回 400 错误，`message` 字段包含具体的验证错误信息
7. **雪花ID**: 所有ID字段使用雪花算法生成的Long类型整数

---

## 角色管理接口

> **认证要求**: 以下所有接口都需要在请求头中携带有效的JWT令牌
> `Authorization: Bearer {token}`

### 13. 分页查询角色列表
**接口描述**: 根据角色名称分页查询所有角色

**请求信息**:
- **接口路径**: `GET /roles`
- **请求头**: `Authorization: Bearer {token}`

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| ------ | ---- | ---- | ------ | ---- |
| current | Integer | 否 | 1 | 当前页码 |
| size | Integer | 否 | 10 | 每页大小 |
| roleName | String | 否 | - | 角色名称（模糊搜索） |

**请求示例**:
```
GET /roles?current=1&size=10&roleName=admin
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1234567890123456789,
        "roleCode": "ROLE_ADMIN",
        "roleName": "管理员",
        "remark": "系统管理员角色",
        "enabled": 1,
        "createTime": "2024-01-01T10:00:00",
        "updateTime": "2024-01-01T10:00:00",
        "createdBy": "system",
        "updatedBy": "admin"
      }
    ],
    "total": 50,
    "size": 10,
    "current": 1,
    "pages": 5
  }
}
```

**RoleResponse 参数说明**:
| 参数名 | 类型 | 说明 |
| ------ | ---- | ---- |
| id | Long | 角色ID |
| roleCode | String | 角色编码 |
| roleName | String | 角色名称 |
| remark | String | 备注 |
| enabled | Integer | 状态（1启用 0禁用） |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |
| createdBy | String | 创建人 |
| updatedBy | String | 更新人 |

---

### 14. 创建角色
**接口描述**: 创建新角色（创建时默认启用）

**请求信息**:
- **接口路径**: `POST /roles`
- **请求头**: 
  - `Content-Type: application/json`
  - `Authorization: Bearer {token}`

**请求参数**:
```json
{
  "roleCode": "string (角色编码)",
  "roleName": "string (角色名称)",
  "remark": "string (备注)",
  "enabled": 1
}
```

**参数说明**:
| 参数名 | 类型 | 必填 | 验证规则 | 说明 |
| ------ | ---- | ---- | -------- | ---- |
| roleCode | String | 是 | 最大50字符 | 角色编码（唯一） |
| roleName | String | 是 | 最大50字符 | 角色名称 |
| remark | String | 否 | 最大200字符 | 备注 |
| enabled | Integer | 否 | - | 状态（1启用 0禁用） |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1234567890123456789,
    "roleCode": "ROLE_USER",
    "roleName": "普通用户",
    "remark": "普通用户角色",
    "enabled": 1,
    "createTime": "2024-01-01T10:00:00",
    "updateTime": "2024-01-01T10:00:00",
    "createdBy": "admin",
    "updatedBy": "admin"
  }
}
```

---

### 15. 更新角色
**接口描述**: 更新指定角色的信息

**请求信息**:
- **接口路径**: `PUT /roles/{id}`
- **请求头**: 
  - `Content-Type: application/json`
  - `Authorization: Bearer {token}`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 角色ID |

**请求参数**: 同创建角色

**请求示例**:
```
PUT /roles/1234567890123456789
```

**响应示例**: 同创建角色

---

### 16. 删除角色
**接口描述**: 删除指定角色（使用逻辑删除）

**请求信息**:
- **接口路径**: `DELETE /roles/{id}`
- **请求头**: `Authorization: Bearer {token}`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 角色ID |

**请求示例**:
```
DELETE /roles/1234567890123456789
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 17. 批量删除角色
**接口描述**: 批量删除多个角色（使用逻辑删除）

**请求信息**:
- **接口路径**: `DELETE /roles/batch`
- **请求头**: 
  - `Content-Type: application/json`
  - `Authorization: Bearer {token}`

**请求参数**:
```json
[1234567890123456789, 1234567890123456790, 1234567890123456791]
```

**参数说明**: 角色ID列表（Long数组）

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 18. 根据ID查询角色
**接口描述**: 根据角色ID查询角色详情

**请求信息**:
- **接口路径**: `GET /roles/{id}`
- **请求头**: `Authorization: Bearer {token}`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 角色ID |

**请求示例**:
```
GET /roles/1234567890123456789
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1234567890123456789,
    "roleCode": "ROLE_ADMIN",
    "roleName": "管理员",
    "remark": "系统管理员角色",
    "enabled": 1,
    "createTime": "2024-01-01T10:00:00",
    "updateTime": "2024-01-01T10:00:00",
    "createdBy": "system",
    "updatedBy": "admin"
  }
}
```

---

### 19. 更新角色状态
**接口描述**: 更新指定角色的启用/禁用状态

**请求信息**:
- **接口路径**: `PUT /roles/{id}/status`
- **请求头**: `Authorization: Bearer {token}`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 角色ID |

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 |
| ------ | ---- | ---- | ---- |
| enabled | Integer | 是 | 状态值（1启用 0禁用） |

**请求示例**:
```
PUT /roles/1234567890123456789/status?enabled=1
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

## 权限管理接口

> **认证要求**: 以下所有接口都需要在请求头中携带有效的JWT令牌
> `Authorization: Bearer {token}`

### 20. 分页查询权限列表
**接口描述**: 根据权限名称分页查询所有权限

**请求信息**:
- **接口路径**: `GET /permissions`
- **请求头**: `Authorization: Bearer {token}`

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| ------ | ---- | ---- | ------ | ---- |
| current | Integer | 否 | 1 | 当前页码 |
| size | Integer | 否 | 10 | 每页大小 |
| permName | String | 否 | - | 权限名称（模糊搜索） |

**请求示例**:
```
GET /permissions?current=1&size=10&permName=user
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1234567890123456789,
        "permCode": "user:read",
        "permName": "用户查询",
        "serviceCode": "cloud-auth",
        "createTime": "2024-01-01T10:00:00",
        "updateTime": "2024-01-01T10:00:00",
        "createdBy": "admin",
        "updatedBy": "admin"
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  }
}
```

**PermissionResponse 参数说明**:
| 参数名 | 类型 | 说明 |
| ------ | ---- | ---- |
| id | Long | 权限ID |
| permCode | String | 权限码 |
| permName | String | 权限名称 |
| serviceCode | String | 服务代码 |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |
| createdBy | String | 创建人 |
| updatedBy | String | 更新人 |

---

### 21. 创建权限
**接口描述**: 创建新权限

**请求信息**:
- **接口路径**: `POST /permissions`
- **请求头**: 
  - `Content-Type: application/json`
  - `Authorization: Bearer {token}`

**请求参数**:
```json
{
  "permCode": "string (权限码)",
  "permName": "string (权限名称)",
  "serviceCode": "string (服务代码)"
}
```

**参数说明**:
| 参数名 | 类型 | 必填 | 验证规则 | 说明 |
| ------ | ---- | ---- | -------- | ---- |
| permCode | String | 是 | 最大100字符 | 权限码（唯一） |
| permName | String | 是 | 最大50字符 | 权限名称 |
| serviceCode | String | 否 | 最大200字符 | 服务代码 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1234567890123456789,
    "permCode": "user:create",
    "permName": "用户创建",
    "serviceCode": "cloud-auth",
    "createTime": "2024-01-01T10:00:00",
    "updateTime": "2024-01-01T10:00:00",
    "createdBy": "admin",
    "updatedBy": "admin"
  }
}
```

---

### 22. 更新权限
**接口描述**: 更新指定权限的信息

**请求信息**:
- **接口路径**: `PUT /permissions/{id}`
- **请求头**: 
  - `Content-Type: application/json`
  - `Authorization: Bearer {token}`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 权限ID |

**请求参数**: 同创建权限

**请求示例**:
```
PUT /permissions/1234567890123456789
```

**响应示例**: 同创建权限

---

### 23. 删除权限
**接口描述**: 删除指定权限

**请求信息**:
- **接口路径**: `DELETE /permissions/{id}`
- **请求头**: `Authorization: Bearer {token}`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 权限ID |

**请求示例**:
```
DELETE /permissions/1234567890123456789
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 24. 批量删除权限
**接口描述**: 批量删除多个权限

**请求信息**:
- **接口路径**: `DELETE /permissions/batch`
- **请求头**: 
  - `Content-Type: application/json`
  - `Authorization: Bearer {token}`

**请求参数**:
```json
[1234567890123456789, 1234567890123456790, 1234567890123456791]
```

**参数说明**: 权限ID列表（Long数组）

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 25. 根据ID查询权限
**接口描述**: 根据权限ID查询权限详情

**请求信息**:
- **接口路径**: `GET /permissions/{id}`
- **请求头**: `Authorization: Bearer {token}`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 权限ID |

**请求示例**:
```
GET /permissions/1234567890123456789
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1234567890123456789,
    "permCode": "user:read",
    "permName": "用户查询",
    "serviceCode": "cloud-auth",
    "createTime": "2024-01-01T10:00:00",
    "updateTime": "2024-01-01T10:00:00",
    "createdBy": "admin",
    "updatedBy": "admin"
  }
}
```

---

### 26. 按服务代码查询权限
**接口描述**: 根据服务代码查询该服务的所有权限

**请求信息**:
- **接口路径**: `GET /permissions/by-service/{serviceCode}`
- **请求头**: `Authorization: Bearer {token}`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
| ------ | ---- | ---- | ---- |
| serviceCode | String | 是 | 服务代码 |

**请求示例**:
```
GET /permissions/by-service/cloud-auth
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1234567890123456789,
      "permCode": "user:read",
      "permName": "用户查询",
      "serviceCode": "cloud-auth",
      "createTime": "2024-01-01T10:00:00",
      "updateTime": "2024-01-01T10:00:00",
      "createdBy": "admin",
      "updatedBy": "admin"
    },
    {
      "id": 1234567890123456790,
      "permCode": "user:create",
      "permName": "用户创建",
      "serviceCode": "cloud-auth",
      "createTime": "2024-01-01T10:00:00",
      "updateTime": "2024-01-01T10:00:00",
      "createdBy": "admin",
      "updatedBy": "admin"
    }
  ]
}
```

---
