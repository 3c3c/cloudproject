# DictController API 接口文档

## 📋 接口概述

**基础路径：** `/admin/dict`

**功能说明：** 字典管理统一接口，包含字典类型和字典数据的增删改查操作，支持树形结构管理。

**通用响应格式：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

---

## 🌳 字典类型接口

### 1. 查询所有字典类型列表

**接口信息：**
- **方法：** `GET`
- **路径：** `/admin/dict/types/all`
- **权限：** `dict:query`

**功能描述：**
获取系统中所有字典类型的列表，按排序号升序排列。

**请求示例：**
```bash
curl -X GET "http://localhost:8080/admin/dict/types/all" \
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
      "dictName": "性别",
      "dictCode": "gender",
      "parentId": 0,
      "sortOrder": 1,
      "status": 1,
      "remark": "用户性别字典",
      "isLeaf": true
    },
    {
      "id": 2,
      "dictName": "学历",
      "dictCode": "education",
      "parentId": 0,
      "sortOrder": 2,
      "status": 1,
      "remark": "用户学历字典",
      "isLeaf": true
    }
  ]
}
```

---

### 2. 创建字典类型

**接口信息：**
- **方法：** `POST`
- **路径：** `/admin/dict/types`
- **权限：** `dict:update`

**功能描述：**
创建新的字典类型，支持树形结构（可指定父节点）。

**请求头：**
```
Content-Type: application/json
Authorization: Bearer your-token
```

**请求体：**
```json
{
  "dictName": "用户状态",
  "dictCode": "user_status",
  "parentId": 0,
  "sortOrder": 3,
  "status": 1,
  "remark": "用户启用/禁用状态"
}
```

**请求参数说明：**

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| dictName | String | 是 | 字典类型名称 |
| dictCode | String | 是 | 字典类型编码，唯一 |
| parentId | Long | 否 | 父级ID，0表示根节点 |
| sortOrder | Integer | 否 | 排序号，默认为0 |
| status | Integer | 否 | 状态：0-禁用，1-启用，默认为1 |
| remark | String | 否 | 备注 |

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 3,
    "dictName": "用户状态",
    "dictCode": "user_status",
    "parentId": 0,
    "sortOrder": 3,
    "status": 1,
    "remark": "用户启用/禁用状态",
    "isLeaf": true
  }
}
```

---

### 3. 更新字典类型

**接口信息：**
- **方法：** `PUT`
- **路径：** `/admin/dict/types/{id}`
- **权限：** `dict:update`

**功能描述：**
更新已存在的字典类型信息。

**路径参数：**
- `id` - 字典类型ID

**请求体：**
```json
{
  "dictName": "用户状态（修改）",
  "dictCode": "user_status",
  "parentId": 0,
  "sortOrder": 4,
  "status": 1,
  "remark": "用户启用/禁用状态（已修改）"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 3,
    "dictName": "用户状态（修改）",
    "dictCode": "user_status",
    "parentId": 0,
    "sortOrder": 4,
    "status": 1,
    "remark": "用户启用/禁用状态（已修改）",
    "isLeaf": true
  }
}
```

---

### 4. 批量删除字典类型

**接口信息：**
- **方法：** `DELETE`
- **路径：** `/admin/dict/types/batch`
- **权限：** `dict:delete`

**功能描述：**
批量删除字典类型。

**请求体：**
```json
[1, 2, 3]
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

---

### 5. 根据字典类型编码查询

**接口信息：**
- **方法：** `GET`
- **路径：** `/admin/dict/types/by-code/{dictCode}`
- **权限：** `dict:query`

**功能描述：**
根据字典类型编码查询单个字典类型。

**路径参数：**
- `dictCode` - 字典类型编码

**请求示例：**
```bash
curl -X GET "http://localhost:8080/admin/dict/types/by-code/gender" \
  -H "Authorization: Bearer your-token"
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "dictName": "性别",
    "dictCode": "gender",
    "parentId": 0,
    "sortOrder": 1,
    "status": 1,
    "remark": "用户性别字典",
    "isLeaf": true
  }
}
```

---

### 6. 根据多个字典类型编码批量查询

**接口信息：**
- **方法：** `POST`
- **路径：** `/admin/dict/types/by-codes`
- **权限：** `dict:query`

**功能描述：**
根据多个字典类型编码批量查询字典类型，按排序号升序排列。

**请求体：**
```json
["gender", "education", "user_status"]
```

**请求示例：**
```bash
curl -X POST "http://localhost:8080/admin/dict/types/by-codes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your-token" \
  -d '["gender", "education", "user_status"]'
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "dictName": "性别",
      "dictCode": "gender",
      "parentId": 0,
      "sortOrder": 1,
      "status": 1,
      "remark": "用户性别字典",
      "isLeaf": true
    },
    {
      "id": 2,
      "dictName": "学历",
      "dictCode": "education",
      "parentId": 0,
      "sortOrder": 2,
      "status": 1,
      "remark": "用户学历字典",
      "isLeaf": true
    },
    {
      "id": 3,
      "dictName": "用户状态",
      "dictCode": "user_status",
      "parentId": 0,
      "sortOrder": 3,
      "status": 1,
      "remark": "用户启用/禁用状态",
      "isLeaf": true
    }
  ]
}
```

---

### 7. 查询字典类型树形结构

**接口信息：**
- **方法：** `GET`
- **路径：** `/admin/dict/types/tree`
- **权限：** `dict:query`

**功能描述：**
获取完整的字典类型树形结构，包含父子层级关系。

**请求示例：**
```bash
curl -X GET "http://localhost:8080/admin/dict/types/tree" \
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
      "dictName": "系统配置",
      "dictCode": "system_config",
      "parentId": 0,
      "sortOrder": 10,
      "status": 1,
      "remark": "系统相关配置",
      "isLeaf": false,
      "children": [
        {
          "id": 3,
          "dictName": "用户配置",
          "dictCode": "user_config",
          "parentId": 1,
          "sortOrder": 1,
          "status": 1,
          "remark": "用户相关配置",
          "isLeaf": true,
          "children": []
        }
      ]
    },
    {
      "id": 2,
      "dictName": "业务配置",
      "dictCode": "business_config",
      "parentId": 0,
      "sortOrder": 20,
      "status": 1,
      "remark": "业务相关配置",
      "isLeaf": false,
      "children": []
    }
  ]
}
```

---

### 8. 根据ID查询所有子孙节点

**接口信息：**
- **方法：** `GET`
- **路径：** `/admin/dict/types/{id}/descendants`
- **权限：** `dict:query`

**功能描述：**
递归查询指定字典类型的所有子孙节点（包括子节点的子节点）。

**路径参数：**
- `id` - 当前节点ID

**请求示例：**
```bash
curl -X GET "http://localhost:8080/admin/dict/types/1/descendants" \
  -H "Authorization: Bearer your-token"
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 3,
      "dictName": "用户配置",
      "dictCode": "user_config",
      "parentId": 1,
      "sortOrder": 1,
      "status": 1,
      "remark": "用户相关配置",
      "isLeaf": true
    }
  ]
}
```

---

### 9. 更新字典类型状态

**接口信息：**
- **方法：** `PUT`
- **路径：** `/admin/dict/types/{id}/status`
- **权限：** `dict:update`

**功能描述：**
启用或禁用指定的字典类型，**同时会级联更新当前节点下所有子孙节点的状态**。

例如：禁用某个父节点，该节点下的所有子节点、孙节点等都会被同步禁用。

**路径参数：**
- `id` - 字典类型ID

**查询参数：**
- `status` - 状态值（1启用，0禁用）

**请求示例：**
```bash
# 启用字典类型（会同时启用所有子孙节点）
curl -X PUT "http://localhost:8080/admin/dict/types/1/status?status=1" \
  -H "Authorization: Bearer your-token"

# 禁用字典类型（会同时禁用所有子孙节点）
curl -X PUT "http://localhost:8080/admin/dict/types/1/status?status=0" \
  -H "Authorization: Bearer your-token"
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

**注意事项：**
- ⚠️ 状态更新是级联的，会影响当前节点及其所有子孙节点
- 批量更新操作在事务中执行，确保数据一致性
- 建议在操作前确认树形结构，避免意外影响大量节点

---

## 📊 字典数据接口

### 10. 分页查询字典数据列表

**接口信息：**
- **方法：** `GET`
- **路径：** `/admin/dict/data`
- **权限：** `dict:query`

**功能描述：**
分页查询字典数据列表，可按字典类型ID筛选。

**查询参数：**
- `page` - 页码，从1开始
- `size` - 每页大小
- `dictTypeId` - 字典类型ID（可选）

**请求示例：**
```bash
curl -X GET "http://localhost:8080/admin/dict/data?page=1&size=10&dictTypeId=1" \
  -H "Authorization: Bearer your-token"
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "dictTypeId": 1,
        "dictLabel": "男",
        "dictValue": "1",
        "sortOrder": 1,
        "remark": "男性"
      },
      {
        "id": 2,
        "dictTypeId": 1,
        "dictLabel": "女",
        "dictValue": "2",
        "sortOrder": 2,
        "remark": "女性"
      }
    ],
    "total": 2,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

---

### 11. 根据字典类型编码查询字典数据列表

**接口信息：**
- **方法：** `GET`
- **路径：** `/admin/dict/data/by-code/{dictCode}`
- **权限：** `dict:query`

**功能描述：**
根据字典类型编码查询该类型下的所有字典数据。

**路径参数：**
- `dictCode` - 字典类型编码

**请求示例：**
```bash
curl -X GET "http://localhost:8080/admin/dict/data/by-code/gender" \
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
      "dictTypeId": 1,
      "dictLabel": "男",
      "dictValue": "1",
      "sortOrder": 1,
      "remark": "男性"
    },
    {
      "id": 2,
      "dictTypeId": 1,
      "dictLabel": "女",
      "dictValue": "2",
      "sortOrder": 2,
      "remark": "女性"
    }
  ]
}
```

---

### 12. 创建字典数据

**接口信息：**
- **方法：** `POST`
- **路径：** `/admin/dict/data`
- **权限：** `dict:update`

**功能描述：**
创建新的字典数据项。

**请求体：**
```json
{
  "dictTypeId": 1,
  "dictLabel": "未知",
  "dictValue": "0",
  "sortOrder": 0,
  "remark": "未知性别"
}
```

**请求参数说明：**

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| dictTypeId | Long | 是 | 字典类型ID |
| dictLabel | String | 是 | 字典标签（显示值） |
| dictValue | String | 是 | 字典值（实际值） |
| sortOrder | Integer | 否 | 排序号 |
| remark | String | 否 | 备注 |

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 3,
    "dictTypeId": 1,
    "dictLabel": "未知",
    "dictValue": "0",
    "sortOrder": 0,
    "remark": "未知性别"
  }
}
```

---

### 13. 更新字典数据

**接口信息：**
- **方法：** `PUT`
- **路径：** `/admin/dict/data/{id}`
- **权限：** `dict:update`

**功能描述：**
更新已存在的字典数据项。

**路径参数：**
- `id` - 字典数据ID

**请求体：**
```json
{
  "dictTypeId": 1,
  "dictLabel": "未知（修改）",
  "dictValue": "0",
  "sortOrder": 0,
  "remark": "未知性别（已修改）"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 3,
    "dictTypeId": 1,
    "dictLabel": "未知（修改）",
    "dictValue": "0",
    "sortOrder": 0,
    "remark": "未知性别（已修改）"
  }
}
```

---

### 14. 批量删除字典数据

**接口信息：**
- **方法：** `DELETE`
- **路径：** `/admin/dict/data/batch`
- **权限：** `dict:delete`

**功能描述：**
批量删除字典数据项。

**请求体：**
```json
[1, 2, 3]
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

---

## 🔐 权限说明

| 权限代码 | 说明 | 适用接口 |
|---------|------|---------|
| `dict:query` | 字典查询权限 | 所有GET接口 |
| `dict:update` | 字典更新权限 | POST、PUT接口 |
| `dict:delete` | 字典删除权限 | DELETE接口 |

---

## 📝 数据模型

### DictTypeResponse（字典类型响应）

```json
{
  "id": "Long              // 字典类型ID",
  "dictName": "String      // 字典类型名称",
  "dictCode": "String      // 字典类型编码",
  "parentId": "Long         // 父级ID，0表示根节点",
  "sortOrder": "Integer     // 排序号",
  "status": "Integer       // 状态：0-禁用，1-启用",
  "remark": "String        // 备注",
  "isLeaf": "Boolean       // 是否叶子节点"
}
```

### DictTypeTreeResponse（字典类型树形响应）

```json
{
  "id": "Long              // 字典类型ID",
  "dictName": "String      // 字典类型名称",
  "dictCode": "String      // 字典类型编码",
  "parentId": "Long         // 父级ID，0表示根节点",
  "sortOrder": "Integer     // 排序号",
  "status": "Integer       // 状态：0-禁用，1-启用",
  "remark": "String        // 备注",
  "isLeaf": "Boolean       // 是否叶子节点",
  "children": "Array       // 子节点列表"
}
```

### DictDataResponse（字典数据响应）

```json
{
  "id": "Long              // 字典数据ID",
  "dictTypeId": "Long       // 字典类型ID",
  "dictLabel": "String     // 字典标签（显示值）",
  "dictValue": "String     // 字典值（实际值）",
  "sortOrder": "Integer     // 排序号",
  "remark": "String        // 备注"
}
```

---

## 🚀 快速开始

### 1. 创建字典类型

```bash
curl -X POST "http://localhost:8080/admin/dict/types" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your-token" \
  -d '{
    "dictName": "性别",
    "dictCode": "gender",
    "parentId": 0,
    "sortOrder": 1,
    "status": 1,
    "remark": "用户性别字典"
  }'
```

### 2. 为字典类型添加数据项

```bash
curl -X POST "http://localhost:8080/admin/dict/data" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your-token" \
  -d '{
    "dictTypeId": 1,
    "dictLabel": "男",
    "dictValue": "1",
    "sortOrder": 1,
    "remark": "男性"
  }'
```

### 3. 查询字典数据

```bash
# 根据编码查询
curl -X GET "http://localhost:8080/admin/dict/data/by-code/gender" \
  -H "Authorization: Bearer your-token"

# 分页查询
curl -X GET "http://localhost:8080/admin/dict/data?page=1&size=10" \
  -H "Authorization: Bearer your-token"
```

### 4. 批量查询字典类型

```bash
curl -X POST "http://localhost:8080/admin/dict/types/by-codes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your-token" \
  -d '["gender", "education", "user_status"]'
```

---

## 📊 完整接口列表

### 字典类型接口

| 序号 | 方法 | 路径 | 功能 |
|------|------|------|------|
| 1 | GET | `/admin/dict/types/all` | 查询所有字典类型 |
| 2 | POST | `/admin/dict/types` | 创建字典类型 |
| 3 | PUT | `/admin/dict/types/{id}` | 更新字典类型 |
| 4 | DELETE | `/admin/dict/types/batch` | 批量删除字典类型 |
| 5 | GET | `/admin/dict/types/by-code/{dictCode}` | 根据编码查询字典类型 |
| 6 | POST | `/admin/dict/types/by-codes` | 根据多个编码批量查询字典类型 |
| 7 | GET | `/admin/dict/types/tree` | 查询树形结构 |
| 8 | GET | `/admin/dict/types/{id}/descendants` | 根据ID查询所有子孙节点 |
| 9 | PUT | `/admin/dict/types/{id}/status` | 更新字典类型状态 |

### 字典数据接口

| 序号 | 方法 | 路径 | 功能 |
|------|------|------|------|
| 10 | GET | `/admin/dict/data` | 分页查询字典数据 |
| 11 | GET | `/admin/dict/data/by-code/{dictCode}` | 根据编码查询字典数据 |
| 12 | POST | `/admin/dict/data` | 创建字典数据 |
| 13 | PUT | `/admin/dict/data/{id}` | 更新字典数据 |
| 14 | DELETE | `/admin/dict/data/batch` | 批量删除字典数据 |

---

## 🎯 错误码说明

| 错误码 | 说明 |
|-------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未授权（token无效或过期） |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 📌 注意事项

### 删除操作注意事项
- 删除字典类型前需要考虑是否有关联的字典数据
- 批量删除操作不支持事务回滚，请谨慎操作

### 树形结构注意事项
- `parentId` 为 0 表示根节点
- 创建时如果 `parentId` 不存在，会抛出异常
- 不能将节点设置为自己的子节点（防止循环引用）
- 不能将节点设置为自己的子孙节点的子节点

### 状态管理注意事项
- 状态值只能是 0（禁用）或 1（启用）
- 禁用的字典类型仍然可以查询，但前端可能需要根据状态进行过滤

### 编码唯一性
- `dictCode` 在系统中必须唯一
- 创建重复的编码会抛出异常
- 更新时如果编码与其他记录冲突也会抛出异常

### 批量查询注意事项
- 批量查询接口（`/types/by-codes`）使用POST方法，请求体为字符串数组
- 如果传入的编码列表为空或null，返回空数组
- 查询结果按排序号升序排列

---

## 📌 更新日志

### v1.1.0 (2026-07-03)
- ✅ 新增：根据字典类型编码查询单个字典类型接口
- ✅ 新增：根据多个字典类型编码批量查询接口
- 🔧 优化：移除了部分不常用的接口，简化API结构
- 📝 完善：更新了完整的API接口文档

### v1.0.0
- 初始版本发布
- 支持字典类型树形结构管理
- 支持字典类型状态管理
- 支持字典数据的完整CRUD操作

---

**文档版本：** v1.1.0  
**最后更新：** 2026-07-03  
**维护团队：** Cloud Admin Team
