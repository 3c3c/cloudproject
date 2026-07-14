# 文件管理服务 API 文档

## 服务信息

- **服务名称**: cloud-file-service
- **基础路径**: `/file`
- **认证方式**: JWT Bearer Token
- **权限控制**: 基于 Spring Security + `@PreAuthorize` 注解

---

## API 接口列表

### 1. 单文件上传

上传单个文件并返回文件信息。

**接口地址:** `POST /file/upload`

**权限要求:** `file:upload`

**请求参数:**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | MultipartFile | 是 | 上传的文件 |
| businessType | String | 否 | 业务类型（如: avatar, document, image等） |
| businessId | Long | 否 | 业务ID（关联业务实体的ID） |

**响应示例:**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "fileKey": "20250114/abc123def456.jpg",
    "originalFileName": "photo.jpg",
    "fileExtension": ".jpg",
    "fileSize": 2048576,
    "contentType": "image/jpeg",
    "storageType": "minio",
    "fileUrl": "http://file-service.com/file/view?key=20250114/abc123def456.jpg",
    "businessType": "avatar",
    "businessId": 1001,
    "createTime": "2025-01-14T10:30:00"
  }
}
```

**cURL 示例:**
```bash
curl -X POST http://localhost:8002/file/upload \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@photo.jpg" \
  -F "businessType=avatar" \
  -F "businessId=1001"
```

---

### 2. 批量文件上传

一次上传多个文件。

**接口地址:** `POST /file/batch-upload`

**权限要求:** `file:upload`

**请求参数:**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| files | MultipartFile[] | 是 | 上传的文件列表 |
| businessType | String | 否 | 业务类型 |
| businessId | Long | 否 | 业务ID |

**响应示例:**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "fileKey": "20250114/file1.jpg",
      "originalFileName": "photo1.jpg",
      "fileSize": 1024000,
      "contentType": "image/jpeg",
      "fileUrl": "http://file-service.com/file/view?key=20250114/file1.jpg",
      "createTime": "2025-01-14T10:30:00"
    },
    {
      "id": 2,
      "fileKey": "20250114/file2.jpg",
      "originalFileName": "photo2.jpg",
      "fileSize": 2048000,
      "contentType": "image/jpeg",
      "fileUrl": "http://file-service.com/file/view?key=20250114/file2.jpg",
      "createTime": "2025-01-14T10:30:01"
    }
  ]
}
```

**cURL 示例:**
```bash
curl -X POST http://localhost:8002/file/batch-upload \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "files=@photo1.jpg" \
  -F "files=@photo2.jpg" \
  -F "businessType=document"
```

---

### 3. 文件下载

下载文件（以附件形式返回）。

**接口地址:** `GET /file/download`

**权限要求:** `file:download`

**请求参数:**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| key | String | 是 | 文件唯一标识（fileKey） |

**响应:** 文件流（二进制）

**响应头:**
```
Content-Disposition: attachment; filename="photo.jpg"
Content-Type: image/jpeg
Content-Length: 2048576
```

**cURL 示例:**
```bash
curl -X GET "http://localhost:8002/file/download?key=20250114/abc123def456.jpg" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -o downloaded.jpg
```

---

### 4. 文件预览

在浏览器中直接预览文件（支持图片、PDF等）。

**接口地址:** `GET /file/view`

**权限要求:** `file:preview`

**请求参数:**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| key | String | 是 | 文件唯一标识（fileKey） |

**响应:** 文件流（内联显示）

**响应头:**
```
Content-Disposition: inline
Content-Type: image/jpeg
Content-Length: 2048576
Cache-Control: max-age=2592000
```

**cURL 示例:**
```bash
curl -X GET "http://localhost:8002/file/view?key=20250114/abc123def456.jpg" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -O
```

---

### 5. 获取临时访问 URL

获取文件的临时访问URL（用于分享或授权访问）。

**接口地址:** `GET /file/presigned-url`

**权限要求:** `file:preview`

**请求参数:**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| key | String | 是 | - | 文件唯一标识 |
| expireSeconds | Integer | 否 | 3600 | URL有效期（秒） |

**响应示例:**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": "https://minio.example.com/bucket/20250114/abc123def456.jpg?X-Amz-Expires=3600&X-Amz-Signature=xxx"
}
```

**cURL 示例:**
```bash
curl -X GET "http://localhost:8002/file/presigned-url?key=20250114/abc123def456.jpg&expireSeconds=7200" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### 6. 单文件删除

删除指定的文件。

**接口地址:** `DELETE /file/delete`

**权限要求:** `file:delete`

**请求参数:**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| key | String | 是 | 文件唯一标识（fileKey） |

**响应示例:**
```json
{
  "code": 200,
  "message": "操作成功"
}
```

**cURL 示例:**
```bash
curl -X DELETE "http://localhost:8002/file/delete?key=20250114/abc123def456.jpg" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### 7. 批量文件删除

批量删除多个文件。

**接口地址:** `DELETE /file/batch`

**权限要求:** `file:delete`

**请求体 (JSON):**
```json
{
  "fileIds": [1, 2, 3, 4, 5]
}
```

**响应示例:**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalCount": 5,
    "successCount": 4,
    "failedCount": 1
  }
}
```

**cURL 示例:**
```bash
curl -X DELETE http://localhost:8002/file/batch \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"fileIds": [1,2,3,4,5]}'
```

---

### 8. 分页查询文件

分页查询文件列表，支持按业务类型筛选。

**接口地址:** `GET /file/page`

**权限要求:** `file:query`

**请求参数:**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| current | Integer | 否 | 1 | 当前页码 |
| size | Integer | 否 | 10 | 每页大小 |
| businessType | String | 否 | - | 业务类型筛选 |

**响应示例:**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "fileKey": "20250114/file1.jpg",
        "originalFileName": "photo1.jpg",
        "fileExtension": ".jpg",
        "fileSize": 1024000,
        "contentType": "image/jpeg",
        "storageType": "minio",
        "fileUrl": "http://file-service.com/file/view?key=20250114/file1.jpg",
        "businessType": "avatar",
        "businessId": 1001,
        "createTime": "2025-01-14T10:30:00"
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  }
}
```

**cURL 示例:**
```bash
curl -X GET "http://localhost:8002/file/page?current=1&size=10&businessType=avatar" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### 9. 获取文件信息

根据 fileKey 获取文件的详细信息。

**接口地址:** `GET /file/info`

**权限要求:** `file:query`

**请求参数:**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| key | String | 是 | 文件唯一标识（fileKey） |

**响应示例:**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "fileKey": "20250114/abc123def456.jpg",
    "originalFileName": "photo.jpg",
    "fileExtension": ".jpg",
    "fileSize": 2048576,
    "contentType": "image/jpeg",
    "storageType": "minio",
    "fileUrl": "http://file-service.com/file/view?key=20250114/abc123def456.jpg",
    "businessType": "avatar",
    "businessId": 1001,
    "createTime": "2025-01-14T10:30:00"
  }
}
```

**cURL 示例:**
```bash
curl -X GET "http://localhost:8002/file/info?key=20250114/abc123def456.jpg" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 数据模型

### FileResponse（文件响应）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 文件ID |
| fileKey | String | 文件唯一标识（存储路径） |
| originalFileName | String | 原始文件名 |
| fileExtension | String | 文件扩展名（如：.jpg） |
| fileSize | Long | 文件大小（字节） |
| contentType | String | MIME类型（如：image/jpeg） |
| storageType | String | 存储类型（minio/aliyun-oss） |
| fileUrl | String | 文件访问URL |
| businessType | String | 业务类型 |
| businessId | Long | 业务关联ID |
| createTime | LocalDateTime | 创建时间 |

### BatchDeleteRequest（批量删除请求）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| fileIds | List\<Long\> | 文件ID列表 |

### BatchDeleteResult（批量删除结果）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| totalCount | Integer | 总数 |
| successCount | Integer | 成功数量 |
| failedCount | Integer | 失败数量 |

---

## 业务类型枚举（BusinessType）

| 编码 | 说明 | 使用场景 |
|------|------|----------|
| avatar | 用户头像 | 用户头像上传 |
| idcard | 证件照片 | 身份证、护照等证件 |
| document | 通用文档 | Word、PDF等文档 |
| image | 图片文件 | 通用图片 |
| video | 视频文件 | MP4、AVI等视频 |
| audio | 音频文件 | MP3、WAV等音频 |
| attachment | 附件文件 | 邮件、消息附件 |
| contract | 合同文件 | 合同、协议文件 |
| certificate | 证书文件 | 各类证书 |
| report | 报告文件 | 报告、报表 |
| template | 模板文件 | 各类模板 |
| log | 日志文件 | 系统日志 |
| backup | 备份文件 | 数据备份 |
| temp | 临时文件 | 临时存储 |
| other | 其他类型 | 未分类文件 |

---

## 错误码说明

| 错误码 | 说明 | HTTP状态码 |
|--------|------|------------|
| 200 | 操作成功 | 200 |
| 400 | 请求参数错误 | 400 |
| 401 | 未授权（Token无效） | 401 |
| 403 | 权限不足 | 403 |
| 404 | 文件不存在 | 404 |
| 413 | 文件大小超过限制 | 413 |
| 415 | 不支持的文件类型 | 415 |
| 500 | 服务器内部错误 | 500 |
| 10001 | 文件上传失败 | 500 |
| 10002 | 文件下载失败 | 500 |
| 10003 | 文件删除失败 | 500 |
| 10004 | 文件不存在 | 404 |

---

## 使用场景示例

### 场景1：用户头像上传

```bash
# 1. 上传头像
curl -X POST http://localhost:8002/file/upload \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@avatar.jpg" \
  -F "businessType=avatar" \
  -F "businessId=1001"

# 2. 预览头像（返回的fileUrl可直接在img标签中使用）
GET http://localhost:8002/file/view?key=20250114/abc123def456.jpg
```

### 场景2：批量上传合同文件

```bash
curl -X POST http://localhost:8002/file/batch-upload \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "files=@contract1.pdf" \
  -F "files=@contract2.pdf" \
  -F "businessType=contract" \
  -F "businessId=5001"
```

### 场景3：获取临时分享链接

```bash
# 获取7天有效的临时访问链接
curl -X GET "http://localhost:8002/file/presigned-url?key=20250114/abc123def456.jpg&expireSeconds=604800" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 场景4：查询用户所有头像

```bash
# 分页查询 avatar 类型的文件
curl -X GET "http://localhost:8002/file/page?current=1&size=10&businessType=avatar" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 权限说明

所有接口都需要在请求头中携带有效的JWT Token：

```http
Authorization: Bearer YOUR_JWT_TOKEN
```

权限列表：
- `file:upload` - 文件上传权限
- `file:download` - 文件下载权限
- `file:preview` - 文件预览权限
- `file:delete` - 文件删除权限
- `file:query` - 文件查询权限

---

## 注意事项

1. **文件大小限制**: 单个文件大小默认不超过 100MB
2. **文件类型限制**: 支持常见文件格式（图片、文档、视频、音频等）
3. **缓存策略**: 预览接口默认缓存30天
4. **并发上传**: 批量上传建议一次不超过10个文件
5. **安全性**: 所有接口都需要认证和授权
6. **跨域配置**: 如需跨域访问，请联系运维配置CORS

---

**文档版本:** v1.0
**最后更新:** 2025-01-14
