# 用户管理 API 文档

## 基础信息
- **服务名称**: cloud-auth
- **基础路径**: `/users`
- **端口**: 8081
- **内容类型**: application/json
- **字符编码**: UTF-8

---

### 1. 分页查询用户
**接口描述**: 根据用户名称或者账号，分页查询用户列表

**请求信息**:
- **接口路径**: `GET /users`
- **请求头**: 
  - `Authorization: Bearer {token}` (必需)

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| ------ | ---- | ---- | ------ | ---- |
| current | Integer | 否 | 1 | 当前页码 |
| size | Integer | 否 | 10 | 每页大小 |
| keyword | String | 否 | - | 用户名或手机号（模糊查询） |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1234567890123456789,
        "username": "zhangsan",
        "nickname": "张三",
        "mobile": "13800138000",
        "email": "zhangsan@example.com",
        "avatar": "https://example.com/avatar.jpg",
        "enabled": 1,
        "mustChangePassword": false,
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

---

### 2. 创建用户
**接口描述**: 创建新用户

**请求信息**:
- **接口路径**: `POST /users`
- **请求头**: 
  - `Authorization: Bearer {token}` (必需)
  - `Content-Type: application/json`

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
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| ------ | ---- | ---- | ------ | ---- |
| username | String | 是 | - | 用户名（唯一） |
| nickname | String | 是 | - | 昵称 |
| password | String | 是 | - | 密码（6-20位） |
| mobile | String | 否 | - | 手机号 |
| email | String | 否 | - | 邮箱 |
| avatar | String | 否 | - | 头像URL |
| enabled | Integer | 否 | 1 | 状态：1启用 0禁用 |
| mustChangePassword | Boolean | 否 | false | 是否下次登录强制改密 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1234567890123456789,
    "username": "zhangsan",
    "nickname": "张三",
    "mobile": "13800138000",
    "email": "zhangsan@example.com",
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

### 3. 编辑用户
**接口描述**: 编辑用户信息

**请求信息**:
- **接口路径**: `PUT /users/{id}`
- **请求头**: 
  - `Authorization: Bearer {token}` (必需)
  - `Content-Type: application/json`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 用户ID |

**请求参数**:
```json
{
  "nickname": "string (昵称)",
  "mobile": "string (手机号)",
  "email": "string (邮箱)",
  "avatar": "string (头像URL)",
  "enabled": 1,
  "mustChangePassword": false
}
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 |
| ------ | ---- | ---- | ---- |
| nickname | String | 否 | 昵称 |
| mobile | String | 否 | 手机号 |
| email | String | 否 | 邮箱 |
| avatar | String | 否 | 头像URL |
| enabled | Integer | 否 | 状态：1启用 0禁用 |
| mustChangePassword | Boolean | 否 | 是否下次登录强制改密 |
| password | String | 否 | 密码（提供则更新） |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1234567890123456789,
    "username": "zhangsan",
    "nickname": "张三",
    "mobile": "13800138000",
    "email": "zhangsan@example.com",
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

### 4. 删除用户
**接口描述**: 删除指定用户

**请求信息**:
- **接口路径**: `DELETE /users/{id}`
- **请求头**: 
  - `Authorization: Bearer {token}` (必需)

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 用户ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 5. 批量删除用户
**接口描述**: 批量删除多个用户

**请求信息**:
- **接口路径**: `DELETE /users/batch`
- **请求头**: 
  - `Authorization: Bearer {token}` (必需)
  - `Content-Type: application/json`

**请求参数**:
```json
[1234567890123456789, 1234567890123456790, 1234567890123456791]
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

### 6. 查询单个用户
**接口描述**: 根据ID查询用户详情

**请求信息**:
- **接口路径**: `GET /users/{id}`
- **请求头**: 
  - `Authorization: Bearer {token}` (必需)

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 用户ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1234567890123456789,
    "username": "zhangsan",
    "nickname": "张三",
    "mobile": "13800138000",
    "email": "zhangsan@example.com",
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

### 7. 更新用户状态
**接口描述**: 启用/禁用用户

**请求信息**:
- **接口路径**: `PUT /users/{id}/status`
- **请求头**: 
  - `Authorization: Bearer {token}` (必需)

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 用户ID |

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| ------ | ---- | ---- | ------ | ---- |
| enabled | Integer | 是 | - | 状态：1启用 0禁用 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---
