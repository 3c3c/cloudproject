# cloud-file 文件存储微服务

## 模块概述

cloud-file 是一个独立的文件存储微服务模块，为整个 cloud-project 提供统一的文件存储能力。

## 核心特性

- ✅ **统一的文件存储抽象**：屏蔽底层存储差异
- ✅ **支持多存储方案**：MinIO / 阿里云 OSS，通过配置灵活切换
- ✅ **完整的文件管理功能**：上传、下载、预览、删除、查询
- ✅ **文件去重**：基于 MD5 哈希值实现去重
- ✅ **业务关联**：支持文件与业务数据关联
- ✅ **权限控制**：基于 Spring Security 的细粒度权限控制
- ✅ **企业级安全**：文件类型校验、大小限制、临时访问 URL

## 技术栈

- Java 17
- Spring Boot 3.2.5
- Spring Cloud 2023.0.1
- MyBatis-Plus 3.5.5
- MinIO SDK 8.5.7
- Aliyun OSS SDK 3.17.4
- Nacos 2.x
- MySQL 8.x

## 快速开始

### 1. 环境准备

确保以下环境已安装并运行：
- MySQL 8.0+
- Nacos 2.x
- MinIO (使用 MinIO 模式时)

### 2. 数据库初始化

执行 `src/main/resources/db/init.sql` 脚本：

```bash
mysql -u root -p < src/main/resources/db/init.sql
```

### 3. Nacos 配置

在 Nacos 配置中心创建 `cloud-file.yaml` 配置，参考 `nacos-config-example.yaml`：

```yaml
file:
  storage:
    type: minio  # 或 oss
    max-size: 10485760  # 10MB
    base-url: http://localhost:9001
    minio:
      endpoint: http://127.0.0.1:9000
      access-key: minioadmin
      secret-key: minioadmin
      bucket-name: cloud-files
```

### 4. 启动应用

```bash
mvn clean install
cd cloud-file
mvn spring-boot:run
```

## API 接口

### 文件上传

```bash
# 单文件上传
curl -X POST http://localhost:9001/file/upload \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@test.jpg" \
  -F "businessType=avatar"

# 批量上传
curl -X POST http://localhost:9001/file/batch-upload \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "files=@file1.jpg" \
  -F "files=@file2.png" \
  -F "businessType=product"
```

### 文件下载

```bash
curl -X GET http://localhost:9001/file/download/1 \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -o downloaded.jpg
```

### 文件预览

```bash
# 获取文件信息
curl -X GET http://localhost:9001/file/preview/1 \
  -H "Authorization: Bearer YOUR_TOKEN"

# 获取临时访问 URL
curl -X GET "http://localhost:9001/file/presigned-url/1?expireSeconds=3600" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 文件删除

```bash
# 单文件删除
curl -X DELETE http://localhost:9001/file/1 \
  -H "Authorization: Bearer YOUR_TOKEN"

# 批量删除
curl -X DELETE http://localhost:9001/file/batch \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"fileIds": [1, 2, 3]}'
```

### 文件查询

```bash
# 分页查询
curl -X GET "http://localhost:9001/file/page?current=1&size=10&businessType=avatar" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 权限定义

需要在 `sys_permission` 表中添加以下权限：

| 权限编码 | 权限名称 | 说明 |
|---------|---------|------|
| file:upload | 文件上传 | 上传文件权限 |
| file:download | 文件下载 | 下载文件权限 |
| file:preview | 文件预览 | 预览文件权限 |
| file:delete | 文件删除 | 删除文件权限 |
| file:query | 文件查询 | 查询文件列表权限 |

## 存储方案切换

### MinIO 模式（默认）

```yaml
file:
  storage:
    type: minio
    minio:
      endpoint: http://127.0.0.1:9000
      access-key: minioadmin
      secret-key: minioadmin
      bucket-name: cloud-files
```

### 阿里云 OSS 模式

```yaml
file:
  storage:
    type: oss
    oss:
      endpoint: oss-cn-hangzhou.aliyuncs.com
      access-key-id: YOUR_ACCESS_KEY_ID
      access-key-secret: YOUR_ACCESS_KEY_SECRET
      bucket-name: cloud-files
      region: cn-hangzhou
```

## 架构设计

### 存储抽象层（策略模式）

```
FileStorageService (接口)
├── MinioFileServiceImpl (MinIO 实现)
└── OssFileServiceImpl (阿里云 OSS 实现)
```

通过 Spring 的条件装配机制，根据配置自动选择存储实现。

### 核心流程

1. **文件上传流程**：
   - 文件校验（大小、类型）
   - 计算 MD5（去重检查）
   - 生成 fileKey
   - 上传到存储服务
   - 保存元数据到数据库

2. **文件下载流程**：
   - 查询文件元数据
   - 从存储服务下载
   - 返回文件流

## 部署说明

### Docker 部署

```bash
# 启动 MinIO
docker run -d \
  --name minio \
  -p 9000:9000 \
  -p 9001:9001 \
  -e "MINIO_ROOT_USER=minioadmin" \
  -e "MINIO_ROOT_PASSWORD=minioadmin" \
  -v /data/minio:/data \
  minio/minio server /data --console-address ":9001"
```

### 生产环境配置

生产环境建议使用阿里云 OSS，配置示例：

```yaml
file:
  storage:
    type: oss
    max-size: 524288000  # 500MB
    base-url: https://file.example.com
    oss:
      endpoint: oss-cn-hangzhou.aliyuncs.com
      access-key-id: ${OSS_ACCESS_KEY_ID}
      access-key-secret: ${OSS_ACCESS_KEY_SECRET}
      bucket-name: cloud-files-prod
      region: cn-hangzhou
```

## 测试

### 单元测试

```bash
mvn test
```

### API 测试

使用 Postman 或 curl 进行 API 测试，参考上面的 API 接口示例。

## 注意事项

1. **文件大小限制**：默认 10MB，可通过配置调整
2. **文件类型限制**：通过配置 allowedExtensions 限制
3. **权限控制**：所有接口都需要相应的权限
4. **临时 URL**：预览 URL 默认 1 小时过期
5. **文件去重**：相同 MD5 的文件只存储一份

## 常见问题

### 1. MinIO 连接失败

检查 MinIO 服务是否正常运行，网络是否可达。

### 2. OSS 配置错误

确保 AccessKey ID 和 Secret 正确，Bucket 存在且可访问。

### 3. 文件上传失败

检查文件大小是否超过限制，文件类型是否允许。

## 维护者

Cloud Project Team

## 许可证

MIT License