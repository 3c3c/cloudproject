# PermissionController API 接口文档

## 📋 接口概述

**基础路径：** `/auth/permissions`

**功能说明：** 权限管理统一接口，支持权限的增删改查、树形结构管理、状态管理等功能。

**通用响应格式：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

---

## 🌳 权限树形接口

### 1. 查询权限树形列表

**接口信息：**
- **方法：** `GET`
- **路径：** `/auth/permissions/tree`
- **权限：** `permission:query`

**功能描述：**
查询权限树形结构，支持按权限名称或类型进行过滤查询。

**查询参数：**

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| permName | String | 否 | 权限名称（模糊查询） |
| type | Integer | 否 | 权限类型：1=目录，2=菜单，3=按钮 |

**请求示例：**
```bash
# 查询所有权限树
curl -X GET "http://localhost:8080/auth/permissions/tree" \
  -H "Authorization: Bearer your-token"

# 按名称模糊查询
curl -X GET "http://localhost:8080/auth/permissions/tree?permName=用户" \
  -H "Authorization: Bearer your-token"

# 按类型查询
curl -X GET "http://localhost:8080/auth/permissions/tree?type=2" \
  -H "Authorization: Bearer your-token"
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "permCode": "system",
      "permName": "系统管理",
      "type": 1,
      "parentId": 0,
      "icon": "setting",
      "path": null,
      "component": null,
      "visible": 1,
      "serviceCode": "system",
      "enabled": 1,
      "sort": 100,
      "remark": "系统管理模块",
      "children": [
        {
          "id": 10,
          "permCode": "system:user",
          "permName": "用户管理",
          "type": 2,
          "parentId": 1,
          "icon": "user",
          "path": "/system/user",
          "component": "views/system/user/index",
          "visible": 1,
          "serviceCode": "system",
          "enabled": 1,
          "sort": 1,
          "remark": "用户管理",
          "children": [
            {
              "id": 1001,
              "permCode": "system:user:add",
              "permName": "新增用户",
              "type": 3,
              "parentId": 10,
              "icon": null,
              "path": null,
              "component": null,
              "visible": 0,
              "serviceCode": "system",
              "enabled": 1,
              "sort": 1,
              "remark": "新增用户按钮",
              "children": []
            }
          ]
        }
      ]
    }
  ]
}
```

---

## 📊 权限查询接口

### 2. 查询所有权限列表（扁平化）

**接口信息：**
- **方法：** `GET`
- **路径：** `/auth/permissions/list`
- **权限：** `permission:query`

**功能描述：**
查询所有权限列表，按排序号和ID升序排列，返回扁平化数据。

**请求示例：**
```bash
curl -X GET "http://localhost:8080/auth/permissions/list" \
  -H "Authorization: Bearer your-token"
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "permCode": "system",
      "permName": "系统管理",
      "type": 1,
      "parentId": 0,
      "icon": "setting",
      "path": null,
      "component": null,
      "visible": 1,
      "serviceCode": "system",
      "enabled": 1,
      "sort": 100,
      "remark": "系统管理模块"
    },
    {
      "id": 10,
      "permCode": "system:user",
      "permName": "用户管理",
      "type": 2,
      "parentId": 1,
      "icon": "user",
      "path": "/system/user",
      "component": "views/system/user/index",
      "visible": 1,
      "serviceCode": "system",
      "enabled": 1,
      "sort": 1,
      "remark": "用户管理"
    }
  ]
}
```

---

### 3. 根据ID查询权限

**接口信息：**
- **方法：** `GET`
- **路径：** `/auth/permissions/{id}`
- **权限：** `permission:query`

**功能描述：**
根据权限ID查询单个权限的详细信息。

**路径参数：**
- `id` - 权限ID

**请求示例：**
```bash
curl -X GET "http://localhost:8080/auth/permissions/10" \
  -H "Authorization: Bearer your-token"
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 10,
    "permCode": "system:user",
    "permName": "用户管理",
    "type": 2,
    "parentId": 1,
    "icon": "user",
    "path": "/system/user",
    "component": "views/system/user/index",
    "visible": 1,
    "serviceCode": "system",
    "enabled": 1,
    "sort": 1,
    "remark": "用户管理"
  }
}
```

---

## ➕ 权限创建接口

### 4. 创建权限

**接口信息：**
- **方法：** `POST`
- **路径：** `/auth/permissions`
- **权限：** `permission:create`

**功能描述：**
创建新的权限，支持目录、菜单、按钮三种类型。

**请求头：**
```
Content-Type: application/json
Authorization: Bearer your-token
```

**请求体：**
```json
{
  "permCode": "system:role",
  "permName": "角色管理",
  "type": 2,
  "parentId": 1,
  "icon": "team",
  "path": "/system/role",
  "component": "views/system/role/index",
  "visible": 1,
  "serviceCode": "system",
  "enabled": 1,
  "sort": 2,
  "remark": "角色管理菜单"
}
```

**请求参数说明：**

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| permCode | String | 是 | 权限码，格式如：system:role，系统唯一 |
| permName | String | 是 | 权限名称，最大50字符 |
| type | Integer | 是 | 权限类型：1=目录，2=菜单，3=按钮 |
| parentId | Long | 否 | 父级ID，0或null表示根节点 |
| icon | String | 否 | 菜单图标，最大100字符 |
| path | String | 否 | 路由地址，最大200字符 |
| component | String | 否 | 前端组件路径，最大200字符 |
| visible | Integer | 否 | 是否可见：0=隐藏，1=显示，默认1 |
| serviceCode | String | 否 | 所属产品/服务，最大50字符 |
| enabled | Integer | 否 | 状态：0=禁用，1=启用，默认1 |
| sort | Integer | 否 | 排序字段，默认0 |
| remark | String | 否 | 说明，最大500字符 |

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 11,
    "permCode": "system:role",
    "permName": "角色管理",
    "type": 2,
    "parentId": 1,
    "icon": "team",
    "path": "/system/role",
    "component": "views/system/role/index",
    "visible": 1,
    "serviceCode": "system",
    "enabled": 1,
    "sort": 2,
    "remark": "角色管理菜单"
  }
}
```

---

## ✏️ 权限更新接口

### 5. 更新权限

**接口信息：**
- **方法：** `PUT`
- **路径：** `/auth/permissions/{id}`
- **权限：** `permission:update`

**功能描述：**
更新已存在的权限信息。

**路径参数：**
- `id` - 权限ID

**请求体：**
```json
{
  "permCode": "system:role",
  "permName": "角色管理（修改）",
  "type": 2,
  "parentId": 1,
  "icon": "team",
  "path": "/system/role",
  "component": "views/system/role/index",
  "visible": 1,
  "serviceCode": "system",
  "enabled": 1,
  "sort": 3,
  "remark": "角色管理菜单（已修改）"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 11,
    "permCode": "system:role",
    "permName": "角色管理（修改）",
    "type": 2,
    "parentId": 1,
    "icon": "team",
    "path": "/system/role",
    "component": "views/system/role/index",
    "visible": 1,
    "serviceCode": "system",
    "enabled": 1,
    "sort": 3,
    "remark": "角色管理菜单（已修改）"
  }
}
```

---

## 🗑️ 权限删除接口

### 6. 删除权限

**接口信息：**
- **方法：** `DELETE`
- **路径：** `/auth/permissions/{id}`
- **权限：** `permission:delete`

**功能描述：**
删除指定的权限。如果该权限下有子节点，则拒绝删除并提示错误。

**路径参数：**
- `id` - 权限ID

**请求示例：**
```bash
curl -X DELETE "http://localhost:8080/auth/permissions/1001" \
  -H "Authorization: Bearer your-token"
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

**错误示例：**
```json
{
  "code": 2107,
  "message": "该权限下有子权限，请先删除子权限",
  "data": null
}
```

---

### 7. 批量删除权限

**接口信息：**
- **方法：** `DELETE`
- **路径：** `/auth/permissions/batch`
- **权限：** `permission:delete`

**功能描述：**
批量删除权限，自动级联删除所有子孙节点。

**请求体：**
```json
[1, 10, 11]
```

**请求示例：**
```bash
curl -X DELETE "http://localhost:8080/auth/permissions/batch" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your-token" \
  -d '[1, 10, 11]'
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

**注意事项：**
- 批量删除会自动删除所有子孙节点
- 删除操作在事务中执行，确保数据一致性

---

## 🔄 权限状态管理接口

### 8. 更新权限状态

**接口信息：**
- **方法：** `PUT`
- **路径：** `/auth/permissions/{id}/enabled`
- **权限：** `permission:update`

**功能描述：**
启用或禁用指定的权限，**同时会级联更新当前节点下所有子孙节点的状态**。

**路径参数：**
- `id` - 权限ID

**查询参数：**
- `enabled` - 状态值（1启用，0禁用）

**请求示例：**
```bash
# 启用权限（会同时启用所有子节点）
curl -X PUT "http://localhost:8080/auth/permissions/1/enabled?enabled=1" \
  -H "Authorization: Bearer your-token"

# 禁用权限（会同时禁用所有子节点）
curl -X PUT "http://localhost:8080/auth/permissions/1/enabled?enabled=0" \
  -H "Authorization: Bearer your-token"
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

**注意事项：**
- ⚠️ 状态更新是级联的，会影响当前节点及其所有子孙节点
- 批量更新操作在事务中执行，确保数据一致性
- 建议在操作前确认树形结构，避免意外影响大量节点

---

### 9. 更新权限可见性

**接口信息：**
- **方法：** `PUT`
- **路径：** `/auth/permissions/{id}/visible`
- **权限：** `permission:update`

**功能描述：**
设置权限的可见性（显示/隐藏），**同时会级联更新当前节点下所有子孙节点的可见性**。

**路径参数：**
- `id` - 权限ID

**查询参数：**
- `visible` - 可见性值（1显示，0隐藏）

**请求示例：**
```bash
# 显示权限（会同时显示所有子节点）
curl -X PUT "http://localhost:8080/auth/permissions/1/visible?visible=1" \
  -H "Authorization: Bearer your-token"

# 隐藏权限（会同时隐藏所有子节点）
curl -X PUT "http://localhost:8080/auth/permissions/1/visible?visible=0" \
  -H "Authorization: Bearer your-token"
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

**注意事项：**
- ⚠️ 可见性更新是级联的，会影响当前节点及其所有子孙节点
- 隐藏父节点会自动隐藏所有子节点
- 批量更新操作在事务中执行

---

## 🔐 权限说明

| 权限代码 | 说明 | 适用接口 |
|---------|------|---------|
| `permission:query` | 权限查询权限 | 所有GET接口 |
| `permission:create` | 权限创建权限 | POST接口 |
| `permission:update` | 权限更新权限 | PUT接口、状态管理 |
| `permission:delete` | 权限删除权限 | DELETE接口 |

---

## 📝 数据模型

### PermissionRequest（权限请求）

```json
{
  "permCode": "String      // 权限码，必填",
  "permName": "String      // 权限名称，必填",
  "type": "Integer         // 权限类型，必填：1=目录，2=菜单，3=按钮",
  "parentId": "Long         // 父级ID，可选",
  "icon": "String          // 菜单图标，可选",
  "path": "String          // 路由地址，可选",
  "component": "String     // 前端组件路径，可选",
  "visible": "Integer      // 是否可见：0=隐藏，1=显示，可选",
  "serviceCode": "String   // 所属产品/服务，可选",
  "enabled": "Integer      // 状态：0=禁用，1=启用，可选",
  "sort": "Integer         // 排序字段，可选",
  "remark": "String        // 说明，可选"
}
```

### PermissionResponse（权限响应）

```json
{
  "id": "Long              // 权限ID",
  "permCode": "String      // 权限码",
  "permName": "String      // 权限名称",
  "type": "Integer         // 权限类型：1=目录，2=菜单，3=按钮",
  "parentId": "Long         // 父级ID",
  "icon": "String          // 菜单图标",
  "path": "String          // 路由地址",
  "component": "String     // 前端组件路径",
  "visible": "Integer      // 是否可见：0=隐藏，1=显示",
  "serviceCode": "String   // 所属产品/服务",
  "enabled": "Integer      // 状态：0=禁用，1=启用",
  "sort": "Integer         // 排序字段",
  "remark": "String        // 说明"
}
```

### PermissionTreeResponse（权限树形响应）

```json
{
  "id": "Long              // 权限ID",
  "permCode": "String      // 权限码",
  "permName": "String      // 权限名称",
  "type": "Integer         // 权限类型：1=目录，2=菜单，3=按钮",
  "parentId": "Long         // 父级ID",
  "icon": "String          // 菜单图标",
  "path": "String          // 路由地址",
  "component": "String     // 前端组件路径",
  "visible": "Integer      // 是否可见：0=隐藏，1=显示",
  "serviceCode": "String   // 所属产品/服务",
  "enabled": "Integer      // 状态：0=禁用，1=启用",
  "sort": "Integer         // 排序字段",
  "remark": "String        // 说明",
  "children": "Array       // 子节点列表"
}
```

---

## 🎯 错误码说明

### 权限管理相关错误码 (2100-2199)

| 错误码 | 说明 |
|-------|------|
| 2101 | 权限码已存在 |
| 2102 | 权限不存在 |
| 2103 | 父级权限不存在 |
| 2104 | 不能将权限设置为自己的子孙权限的子权限（循环引用） |
| 2105 | 不能将权限设置为自己的父权限 |
| 2106 | 状态值无效，只能是0（禁用）或1（启用） |
| 2107 | 该权限下有子权限，请先删除子权限 |
| 2108 | 权限类型无效 |
| 2109 | 父级权限类型无效 |
| 2110 | 该权限已分配给角色，无法删除 |

### 通用错误码

| 错误码 | 说明 |
|-------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未授权（token无效或过期） |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 🚀 快速开始

### 1. 创建权限目录

```bash
curl -X POST "http://localhost:8080/auth/permissions" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your-token" \
  -d '{
    "permCode": "system",
    "permName": "系统管理",
    "type": 1,
    "parentId": 0,
    "icon": "setting",
    "serviceCode": "system",
    "sort": 100,
    "enabled": 1,
    "visible": 1
  }'
```

### 2. 创建权限菜单

```bash
curl -X POST "http://localhost:8080/auth/permissions" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your-token" \
  -d '{
    "permCode": "system:user",
    "permName": "用户管理",
    "type": 2,
    "parentId": 1,
    "icon": "user",
    "path": "/system/user",
    "component": "views/system/user/index",
    "serviceCode": "system",
    "sort": 1,
    "enabled": 1,
    "visible": 1
  }'
```

### 3. 创建按钮权限

```bash
curl -X POST "http://localhost:8080/auth/permissions" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your-token" \
  -d '{
    "permCode": "system:user:add",
    "permName": "新增用户",
    "type": 3,
    "parentId": 10,
    "serviceCode": "system",
    "sort": 1,
    "enabled": 1,
    "visible": 0
  }'
```

### 4. 查询权限树

```bash
curl -X GET "http://localhost:8080/auth/permissions/tree" \
  -H "Authorization: Bearer your-token"
```

---

## 📊 完整接口列表

| 序号 | 方法 | 路径 | 功能 |
|------|------|------|------|
| 1 | GET | `/auth/permissions/tree` | 查询权限树形列表 |
| 2 | GET | `/auth/permissions/list` | 查询所有权限列表（扁平化） |
| 3 | GET | `/auth/permissions/{id}` | 根据ID查询权限 |
| 4 | POST | `/auth/permissions` | 创建权限 |
| 5 | PUT | `/auth/permissions/{id}` | 更新权限 |
| 6 | DELETE | `/auth/permissions/{id}` | 删除权限 |
| 7 | DELETE | `/auth/permissions/batch` | 批量删除权限 |
| 8 | PUT | `/auth/permissions/{id}/enabled` | 更新权限状态（级联） |
| 9 | PUT | `/auth/permissions/{id}/visible` | 更新权限可见性（级联） |

---

## 📌 注意事项

### 权限类型约束
- **目录（type=1）**：作为根节点或挂在根节点下，不需要component字段
- **菜单（type=2）**：必须挂在目录（type=1）或根节点下，需要path和component字段
- **按钮（type=3）**：必须挂在菜单（type=2）下，path和component自动设置为null

### 删除操作注意事项
- 单个删除：如果有子节点，拒绝删除并提示错误
- 批量删除：自动级联删除所有子孙节点
- 删除操作在事务中执行，保证数据一致性

### 状态管理注意事项
- 更新状态或可见性时会级联更新所有子孙节点
- 禁用父节点会自动禁用所有子节点
- 隐藏父节点会自动隐藏所有子节点
- 批量更新在事务中执行，确保数据一致性

### 权限码唯一性
- `permCode` 在系统中必须唯一
- 创建重复的权限码会抛出异常
- 更新时如果权限码与其他记录冲突也会抛出异常

### 树形结构注意事项
- `parentId` 为 0 或 null 表示根节点
- 创建时如果 `parentId` 不存在，会抛出异常
- 不能将权限设置为自己的子权限（防止循环引用）
- 不能将权限设置为自己的子孙权限的子权限

---

## 📌 更新日志

### v1.0.0 (2026-07-03)
- 初始版本发布
- 支持权限树形结构管理
- 支持权限类型校验
- 支持权限状态管理（级联更新）
- 支持权限可见性管理（级联更新）
- 支持权限的完整CRUD操作

---

**文档版本：** v1.0.0  
**最后更新：** 2026-07-03  
**维护团队：** Cloud Auth Team
