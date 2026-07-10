# cloud-file 文件存储微服务设计文档

## 文档信息

| 项目 | 信息 |
|------|------|
| 文档版本 | v1.0 |
| 创建日期 | 2026-06-30 |
| 作者 | Cloud Project Team |
| 模块名称 | cloud-file |
| 服务端口 | 9001 |

---

## 1. 概述

### 1.1 背景与目标

cloud-file 是一个独立的文件存储微服务模块，为整个 cloud-project 提供统一的文件存储能力。该模块需要支持多种对象存储方案（MinIO、阿里云OSS等），允许通过配置文件灵活切换存储实现，同时提供文件上传、下载、预览、删除等完整的文件管理功能。

**核心目标：**
- 提供统一的文件存储抽象，屏蔽底层存储差异
- 支持多存储方案灵活切换（MinIO / OSS）
- 提供完整的文件管理 REST API
- 实现文件去重、业务关联、权限控制等企业级功能
- 遵循项目现有的架构规范和代码风格

### 1.2 系统定位

cloud-file 作为基础设施服务，为其他业务模块提供文件存储能力：

```
┌─────────────────┐
│   cloud-gateway │ ← 网关统一鉴权
└────────┬────────┘
         │
    ┌────┴────┐
    ↓         ↓
┌──────────┐ ┌──────────┐
│cloud-auth│ │cloud-file│ ← 独立文件存储服务
└──────────┘ └────┬─────┘
     ↑            │
     │        ┌───┴───┐
     │        ↓       ↓
┌────┴────┐ ┌────┐ ┌────┐
│cloud-   │ │cloud│ │cloud│
│product  │ │admin│ │order│
└─────────┘ └────┘ └────┘
     ↑            ↑      ↑
     └────────────┴──────┘
      通过 Feign 调用文件服务
```

### 1.3 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 编程语言 |
| Spring Boot | 3.2.5 | 应用框架 |
| Spring Cloud | 2023.0.1 | 微服务框架 |
| Spring Cloud Alibaba | 2023.0.1.0 | 阿里云组件 |
| MyBatis-Plus | 3.5.5 | ORM 框架 |
| MinIO SDK | 8.5.x | MinIO 客户端 |
| Aliyun OSS SDK | 3.17.x | 阿里云 OSS 客户端 |
| Nacos | 2.x | 配置中心与服务发现 |
| MySQL | 8.x | 文件元数据存储 |
| MapStruct | 1.5.5.Final | DTO 转换 |

---

## 2. 系统架构设计

### 2.1 整体架构

cloud-file 采用经典的分层架构设计：

```
┌─────────────────────────────────────────────────┐
│                  API 层 (Controller)             │
│  FileController - 统一 REST API 接口             │
└──────────────────┬──────────────────────────────┘
                   │
┌──────────────────┴──────────────────────────────┐
│                业务层 (Service)                  │
│    FileService - 文件业务逻辑处理                 │
│  - 文件校验                                       │
│  - 文件去重 (MD5)                                 │
│  - 业务关联                                       │
│  - 权限控制                                       │
└──────────────────┬──────────────────────────────┘
                   │
┌──────────────────┴──────────────────────────────┐
│              存储抽象层 (Storage)                  │
│         FileStorageService (策略模式)             │
│  ┌────────────────┐      ┌────────────────┐       │
│  │ MinioImpl     │      │ OssImpl        │       │
│  │ - MinIO 操作  │      │ - OSS 操作     │       │
│  └────────────────┘      └────────────────┘       │
└──────────────────┬──────────────────────────────┘
                   │
┌──────────────────┴──────────────────────────────┐
│              数据访问层 (Mapper)                  │
│          FileInfoMapper - MyBatis Plus            │
└──────────────────┬──────────────────────────────┘
                   │
┌──────────────────┴──────────────────────────────┐
│               持久化层 (Database)                 │
│              file_info 表                         │
└─────────────────────────────────────────────────┘
```

### 2.2 核心设计模式

#### 2.2.1 策略模式（Strategy Pattern）

通过策略模式实现多存储方案切换：

```java
// 策略接口
public interface FileStorageService {
    String upload(InputStream stream, String name, String contentType, long size);
    InputStream download(String fileKey);
    boolean delete(String fileKey);
    int batchDelete(List<String> fileKeys);
    String getPresignedUrl(String fileKey, long expireSeconds);
    boolean exists(String fileKey);
    StorageType getStorageType();
}

// 具体策略
public class MinioFileServiceImpl implements FileStorageService { ... }
public class OssFileServiceImpl implements FileStorageService { ... }
```

#### 2.2.2 条件装配（Conditional Bean）

使用 Spring 的条件装配机制实现运行时策略选择：

```java
@Configuration
public class FileStorageConfig {
    @Bean
    @ConditionalOnProperty(name = "file.storage.type", havingValue = "minio")
    public FileStorageService minioStorage(...) {
        return new MinioFileServiceImpl(...);
    }
    
    @Bean
    @ConditionalOnProperty(name = "file.storage.type", havingValue = "oss")
    public FileStorageService ossStorage(...) {
        return new OssFileServiceImpl(...);
    }
}
```

**优势：**
- 配置驱动，无需修改代码
- 运行时切换，支持多环境
- 扩展性强，添加新存储方案只需实现接口

### 2.3 存储切换流程

```
┌─────────────────┐
│ Nacos 配置中心   │
│ file.storage.   │
│ type = minio    │
└────────┬────────┘
         │ 应用启动时加载
         ↓
┌─────────────────┐
│  FileStorage    │
│     Config      │
└────────┬────────┘
         │ @ConditionalOnProperty
         ↓
┌─────────────────┐
│ 选择存储实现     │
│ MinioImpl /     │
│ OssImpl         │
└────────┬────────┘
         │
         ↓
┌─────────────────┐
│ 注入到          │
│ FileService     │
└─────────────────┘
```

---

## 3. 模块设计

### 3.1 包结构设计

```
com.cloud.file/
├── FileApplication.java                    # 启动类
│
├── config/                                 # 配置类
│   ├── FileStorageConfig.java             # 存储条件装配配置
│   └── MinioClientConfig.java             # MinIO 客户端配置
│
├── properties/                             # 配置属性
│   ├── FileStorageProperties.java         # 文件存储配置
│   ├── MinioProperties.java               # MinIO 配置
│   └── OssProperties.java                 # OSS 配置
│
├── storage/                                # 存储抽象层
│   ├── FileStorageService.java            # 存储服务接口
│   ├── StorageType.java                   # 存储类型枚举
│   ├── impl/
│   │   ├── MinioFileServiceImpl.java      # MinIO 实现
│   │   └── OssFileServiceImpl.java        # OSS 实现
│   └── FileKeyGenerator.java             # 文件 Key 生成器
│
├── entity/                                 # 实体类
│   └── FileInfo.java                      # 文件信息实体
│
├── mapper/                                 # 数据访问层
│   └── FileInfoMapper.java                # 文件信息 Mapper
│
├── service/                                # 业务服务层
│   ├── FileService.java                   # 文件服务接口
│   └── impl/
│       └── FileServiceImpl.java          # 文件服务实现
│
├── controller/                             # 控制器层
│   └── FileController.java                # 文件 REST API
│
├── dto/                                    # 数据传输对象
│   ├── request/
│   │   ├── FileUploadRequest.java        # 上传请求
│   │   ├── FileQueryRequest.java          # 查询请求
│   │   └── BatchDeleteRequest.java        # 批量删除请求
│   └── response/
│       └── FileResponse.java              # 文件响应
│
├── converter/                              # 转换器
│   └── FileConverter.java                 # MapStruct 转换器
│
└── utils/                                  # 工具类
    ├── FileValidator.java                 # 文件校验工具
    └── FileUtils.java                     # 文件工具类
```

### 3.2 模块依赖关系

```
┌──────────────────────────────────────────────┐
│                  cloud-file                   │
├──────────────────────────────────────────────┤
│                                              │
│  ┌──────────────┐    ┌──────────────┐       │
│  │ cloud-common │←──│  cloud-file   │       │
│  └──────────────┘    └───────┬──────┘       │
│      ↑                       │               │
│      │ 依赖                   │               │
│  ┌───┴───────────────────────┴───────┐       │
│  │         spring-boot-starter        │       │
│  │         mybatis-plus                │       │
│  │         minio / oss sdk             │       │
│  └─────────────────────────────────────┘       │
└──────────────────────────────────────────────┘

调用关系（其他服务 → cloud-file）：
┌─────────────┐         ┌─────────────┐
│cloud-product│─Feign──→│cloud-file   │
│cloud-order  │────────→│             │
│cloud-admin  │────────→│             │
└─────────────┘         └─────────────┘
```

---

## 4. 接口设计

### 4.1 REST API 设计规范

遵循 RESTful 设计风格，使用统一的 Result<T> 响应格式。

#### 通用响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

#### 响应码定义

| Code | 说明 |
|------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未授权（token 无效） |
| 403 | 权限不足 |
| 404 | 文件不存在 |
| 413 | 文件过大 |
| 415 | 不支持的文件类型 |
| 500 | 服务器错误 |

### 4.2 API 详细设计

#### 4.2.1 单文件上传

**接口：** `POST /file/upload`

**权限：** `file:upload`

**请求参数：**
```http
Content-Type: multipart/form-data

file: <binary>                    # 文件二进制流
businessType: <string>             # 业务类型（可选）
businessId: <long>                 # 业务 ID（可选）
```

**响应示例：**
```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "id": 1001,
    "fileKey": "avatar/2026-06-30/uuid.jpg",
    "originalFileName": "profile.jpg",
    "fileSize": 102400,
    "contentType": "image/jpeg",
    "fileUrl": "http://localhost:9001/file/download/1001",
    "storageType": "minio"
  }
}
```

#### 4.2.2 批量文件上传

**接口：** `POST /file/batch-upload`

**权限：** `file:upload`

**请求参数：**
```http
Content-Type: multipart/form-data

files: <binary>[]                 # 多个文件
businessType: <string>            # 业务类型（可选）
businessId: <long>                # 业务 ID（可选）
```

**响应示例：**
```json
{
  "code": 200,
  "message": "批量上传完成",
  "data": [
    { "id": 1001, "fileKey": "...", ... },
    { "id": 1002, "fileKey": "...", ... }
  ]
}
```

#### 4.2.3 文件下载

**接口：** `GET /file/download/{id}`

**权限：** `file:download`

**路径参数：**
- `id`: 文件 ID

**响应：**
- Content-Type: 文件的实际 MIME 类型
- Content-Disposition: attachment; filename="原始文件名"
- Body: 文件二进制流

#### 4.2.4 文件预览

**接口：** `GET /file/preview/{id}`

**权限：** `file:preview`

**路径参数：**
- `id`: 文件 ID

**响应：**
- 图片类型：直接返回文件流，Content-Type: image/*
- 其他类型：返回临时访问 URL
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "presignedUrl": "https://cloud-files.oss-cn-hangzhou.aliyuncs.com/...",
    "expireTime": "2026-06-30T13:00:00"
  }
}
```

#### 4.2.5 获取临时访问 URL

**接口：** `GET /file/presigned-url/{id}`

**权限：** `file:preview`

**路径参数：**
- `id`: 文件 ID

**查询参数：**
- `expireSeconds`: 过期时间（秒），默认 3600

**响应示例：**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "presignedUrl": "https://cloud-files.oss-cn-hangzhou.aliyuncs.com/...",
    "expireTime": "2026-06-30T13:00:00",
    "expireSeconds": 3600
  }
}
```

#### 4.2.6 单文件删除

**接口：** `DELETE /file/{id}`

**权限：** `file:delete`

**路径参数：**
- `id`: 文件 ID

**响应示例：**
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

#### 4.2.7 批量文件删除

**接口：** `DELETE /file/batch`

**权限：** `file:delete`

**请求体：**
```json
{
  "fileIds": [1001, 1002, 1003]
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "批量删除成功",
  "data": {
    "totalCount": 3,
    "successCount": 3,
    "failedCount": 0
  }
}
```

#### 4.2.8 分页查询文件

**接口：** `GET /file/page`

**权限：** `file:query`

**查询参数：**
- `current`: 当前页码，默认 1
- `size`: 每页大小，默认 10
- `businessType`: 业务类型（可选）
- `uploadedBy`: 上传用户 ID（可选）

**响应示例：**
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "current": 1,
    "size": 10,
    "total": 100,
    "records": [
      {
        "id": 1001,
        "fileKey": "avatar/2026-06-30/uuid.jpg",
        "originalFileName": "profile.jpg",
        "fileSize": 102400,
        "storageType": "minio",
        "createTime": "2026-06-30T10:00:00",
        "uploadedBy": 10001
      }
    ]
  }
}
```

### 4.3 权限定义

需要在 `sys_permission` 表中添加以下权限：

| 权限编码 | 权限名称 | 所属服务 | 类型 |
|---------|---------|---------|------|
| file:upload | 文件上传 | file | 功能权限 |
| file:download | 文件下载 | file | 功能权限 |
| file:preview | 文件预览 | file | 功能权限 |
| file:delete | 文件删除 | file | 功能权限 |
| file:query | 文件查询 | file | 功能权限 |

---

## 5. 数据库设计

### 5.1 数据库表结构

#### 表名：file_info

文件信息表，存储上传文件的元数据信息。

```sql
CREATE TABLE file_info (
    id                  BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    file_key            VARCHAR(255) NOT NULL COMMENT '文件唯一标识（存储路径）',
    original_file_name  VARCHAR(255) NOT NULL COMMENT '原始文件名',
    file_extension      VARCHAR(50) COMMENT '文件扩展名',
    file_size           BIGINT NOT NULL COMMENT '文件大小（字节）',
    content_type        VARCHAR(100) COMMENT 'MIME 类型',
    storage_type        VARCHAR(20) NOT NULL COMMENT '存储类型（minio/oss）',
    file_url            VARCHAR(500) COMMENT '访问 URL',
    file_md5            VARCHAR(32) COMMENT '文件 MD5（用于去重）',
    uploaded_by         BIGINT COMMENT '上传用户 ID',
    business_type       VARCHAR(50) COMMENT '业务类型（avatar/product/order）',
    business_id         BIGINT COMMENT '业务 ID',
    deleted             TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    create_time         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by          VARCHAR(64) COMMENT '创建人',
    updated_by          VARCHAR(64) COMMENT '更新人',
    PRIMARY KEY (id),
    UNIQUE KEY uk_file_key (file_key),
    KEY idx_md5 (file_md5),
    KEY idx_business (business_type, business_id),
    KEY idx_uploaded_by (uploaded_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件信息表';
```

### 5.2 字段说明

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键 ID，自增 |
| file_key | VARCHAR(255) | 文件唯一标识，格式：`{businessType}/{date}/{uuid}.{ext}` |
| original_file_name | VARCHAR(255) | 用户上传的原始文件名 |
| file_extension | VARCHAR(50) | 文件扩展名，如 jpg、png、pdf |
| file_size | BIGINT | 文件大小，单位：字节 |
| content_type | VARCHAR(100) | MIME 类型，如 image/jpeg |
| storage_type | VARCHAR(20) | 存储类型：minio 或 oss |
| file_url | VARCHAR(500) | 文件访问 URL |
| file_md5 | VARCHAR(32) | 文件 MD5 哈希值，用于去重 |
| uploaded_by | BIGINT | 上传用户 ID，来自 UserContext |
| business_type | VARCHAR(50) | 业务类型，标识文件所属业务 |
| business_id | BIGINT | 业务 ID，关联具体业务记录 |
| deleted | TINYINT(1) | 逻辑删除标记，0-未删除，1-已删除 |

### 5.3 索引设计

| 索引名 | 类型 | 字段 | 用途 |
|--------|------|------|------|
| PRIMARY | 主键 | id | 主键索引 |
| uk_file_key | 唯一索引 | file_key | 保证文件 Key 唯一性 |
| idx_md5 | 普通索引 | file_md5 | 用于 MD5 去重查询 |
| idx_business | 普通索引 | business_type, business_id | 用于业务关联查询 |
| idx_uploaded_by | 普通索引 | uploaded_by | 用于用户文件查询 |

### 5.4 数据示例

```sql
INSERT INTO file_info (file_key, original_file_name, file_extension, file_size, 
    content_type, storage_type, file_url, file_md5, uploaded_by, business_type, business_id) 
VALUES 
('avatar/2026-06-30/a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg', 
 'profile.jpg', 'jpg', 102400, 'image/jpeg', 'minio', 
 'http://localhost:9001/file/download/1001', 
 'd41d8cd98f00b204e9800998ecf8427e', 10001, 'avatar', NULL),
('product/2026-06-30/b2c3d4e5-f6g7-8901-bcde-f12345678901.png',
 'product-image.png', 'png', 204800, 'image/png', 'minio',
 'http://localhost:9001/file/download/1002',
 '5d41402abc4b2a76b9719d911017c592', 10001, 'product', 20001);
```

---

## 6. 配置设计

### 6.1 Maven 依赖配置

**父 POM 更新（pom.xml）：**

```xml
<modules>
    <module>cloud-common</module>
    <module>cloud-gateway</module>
    <module>cloud-auth</module>
    <module>cloud-admin</module>
    <module>cloud-product</module>
    <module>cloud-order</module>
    <module>cloud-file</module>  <!-- 新增 -->
</modules>

<dependencyManagement>
    <dependencies>
        <!-- MinIO SDK -->
        <dependency>
            <groupId>io.minio</groupId>
            <artifactId>minio</artifactId>
            <version>8.5.7</version>
        </dependency>
        <!-- Aliyun OSS SDK -->
        <dependency>
            <groupId>com.aliyun.oss</groupId>
            <artifactId>aliyun-sdk-oss</artifactId>
            <version>3.17.4</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

**cloud-file/pom.xml：**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>com.cloud</groupId>
        <artifactId>cloud-project</artifactId>
        <version>1.0.0</version>
    </parent>
    
    <artifactId>cloud-file</artifactId>
    <name>cloud-file</name>
    <description>文件存储微服务</description>
    
    <dependencies>
        <!-- cloud-common -->
        <dependency>
            <groupId>com.cloud</groupId>
            <artifactId>cloud-common</artifactId>
        </dependency>
        
        <!-- Spring Cloud -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>
        
        <!-- Nacos -->
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
        </dependency>
        
        <!-- Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- MyBatis-Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
        </dependency>
        
        <!-- MySQL -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <!-- MinIO -->
        <dependency>
            <groupId>io.minio</groupId>
            <artifactId>minio</artifactId>
        </dependency>
        
        <!-- Aliyun OSS -->
        <dependency>
            <groupId>com.aliyun.oss</groupId>
            <artifactId>aliyun-sdk-oss</artifactId>
        </dependency>
        
        <!-- MapStruct -->
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
        </dependency>
        
        <!-- Hutool -->
        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### 6.2 应用配置（application.yml）

```yaml
server:
  port: 9001

spring:
  application:
    name: cloud-file
  
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/cloud_file?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: root
  
  cloud:
    nacos:
      server-addr: ${NACOS_ADDR:127.0.0.1:8848}
      username: ${NACOS_USER:nacos}
      password: ${NACOS_PASSWORD:nacos}
      discovery:
        namespace: ${NACOS_NS:}
        group: DEFAULT_GROUP
      config:
        namespace: ${NACOS_NS:}
        group: DEFAULT_GROUP
        import:
          - optional:nacos:cloud-common.yaml
          - optional:nacos:cloud-file.yaml

mybatis-plus:
  mapper-locations: classpath*:mapper/**/*.xml
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

logging:
  level:
    com.cloud: debug
    com.cloud.file.mapper: debug
```

### 6.3 Nacos 配置（cloud-file.yaml）

```yaml
# 文件存储配置
file:
  storage:
    # 存储类型：minio 或 oss
    type: ${FILE_STORAGE_TYPE:minio}
    
    # 文件大小限制（字节），默认 10MB
    max-size: ${FILE_MAX_SIZE:10485760}
    
    # 文件访问基础 URL
    base-url: ${FILE_BASE_URL:http://localhost:9001}
    
    # 允许的文件扩展名
    allowed-extensions: ${FILE_ALLOWED_EXTENSIONS:jpg,jpeg,png,gif,bmp,webp,pdf,doc,docx,xls,xlsx,ppt,pptx,txt,zip,rar}
    
    # MinIO 配置
    minio:
      endpoint: ${MINIO_ENDPOINT:http://127.0.0.1:9000}
      access-key: ${MINIO_ACCESS_KEY:minioadmin}
      secret-key: ${MINIO_SECRET_KEY:minioadmin}
      bucket-name: ${MINIO_BUCKET:cloud-files}
      connect-timeout: 10000
      write-timeout: 60000
      read-timeout: 10000
    
    # OSS 配置
    oss:
      endpoint: ${OSS_ENDPOINT:oss-cn-hangzhou.aliyuncs.com}
      access-key-id: ${OSS_ACCESS_KEY_ID:your-access-key}
      access-key-secret: ${OSS_ACCESS_KEY_SECRET:your-secret}
      bucket-name: ${OSS_BUCKET:cloud-files}
      region: ${OSS_REGION:cn-hangzhou}

# Feign 配置
feign:
  client:
    config:
      default:
        connectTimeout: 5000
        readTimeout: 5000
```

### 6.4 配置属性类设计

#### FileStorageProperties.java

```java
package com.cloud.file.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "file.storage")
public class FileStorageProperties {
    
    /**
     * 存储类型
     */
    private StorageType type = StorageType.MINIO;
    
    /**
     * 文件最大大小（字节）
     */
    private Long maxSize = 10 * 1024 * 1024L; // 默认 10MB
    
    /**
     * 允许的文件扩展名
     */
    private List<String> allowedExtensions;
    
    /**
     * 文件访问基础 URL
     */
    private String baseUrl = "http://localhost:9001";
    
    /**
     * MinIO 配置
     */
    private MinioProperties minio = new MinioProperties();
    
    /**
     * OSS 配置
     */
    private OssProperties oss = new OssProperties();
}
```

#### MinioProperties.java

```java
package com.cloud.file.properties;

import lombok.Data;

@Data
public class MinioProperties {
    private String endpoint = "http://127.0.0.1:9000";
    private String accessKey = "minioadmin";
    private String secretKey = "minioadmin";
    private String bucketName = "cloud-files";
    private Integer connectTimeout = 10000;
    private Integer writeTimeout = 60000;
    private Integer readTimeout = 10000;
}
```

#### OssProperties.java

```java
package com.cloud.file.properties;

import lombok.Data;

@Data
public class OssProperties {
    private String endpoint = "oss-cn-hangzhou.aliyuncs.com";
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName = "cloud-files";
    private String region = "cn-hangzhou";
}
```

---

## 7. 安全设计

### 7.1 安全威胁分析

| 威胁 | 描述 | 风险等级 | 防护措施 |
|------|------|---------|---------|
| 未授权上传 | 恶意用户上传非法文件 | 高 | JWT 认证 + 权限控制 |
| 文件类型攻击 | 上传可执行文件（病毒、木马） | 高 | 文件类型白名单检查 |
| 路径遍历攻击 | 通过 file_key 访问其他文件 | 中 | file_key 格式校验 + 权限控制 |
| 文件过大导致 DoS | 上传超大文件耗尽存储 | 高 | 文件大小限制 |
| 敏感信息泄露 | 文件 URL 泄露导致未授权访问 | 中 | 临时 URL + 权限检查 |
| 文件覆盖 | 相同 file_key 覆盖已有文件 | 低 | file_key 唯一性约束 + UUID |

### 7.2 安全防护机制

#### 7.2.1 认证与授权

```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
public class FileController {
    
    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('file:upload')")
    public Result<FileResponse> upload(
        @RequestParam("file") MultipartFile file,
        @RequestParam(value = "businessType", required = false) String businessType,
        @RequestParam(value = "businessId", required = false) Long businessId
    ) {
        // 从 UserContext 获取当前用户 ID
        Long userId = UserContext.getUserId();
        // ...
    }
}
```

#### 7.2.2 文件类型校验

```java
@Component
public class FileValidator {
    
    @Autowired
    private FileStorageProperties properties;
    
    public void validate(MultipartFile file) {
        // 1. 文件大小检查
        if (file.getSize() > properties.getMaxSize()) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
        }
        
        // 2. 文件扩展名检查
        String extension = getFileExtension(file.getOriginalFilename());
        if (!properties.getAllowedExtensions().contains(extension)) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }
        
        // 3. MIME 类型检查
        String contentType = file.getContentType();
        if (!isAllowedContentType(contentType, extension)) {
            throw new BusinessException(ErrorCode.FILE_CONTENT_TYPE_MISMATCH);
        }
    }
}
```

#### 7.2.3 FileKey 格式校验

```java
public class FileKeyGenerator {
    
    private static final Pattern FILE_KEY_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9/_\\-\\.]+$");
    
    public static String generateKey(String businessType, String originalFileName) {
        String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String extension = FilenameUtils.getExtension(originalFileName);
        
        // 格式：{businessType}/{date}/{uuid}.{extension}
        String fileKey = String.format("%s/%s/%s.%s", 
            businessType, date, uuid, extension);
        
        // 校验生成的 fileKey 格式
        if (!FILE_KEY_PATTERN.matcher(fileKey).matches()) {
            throw new BusinessException(ErrorCode.INVALID_FILE_KEY);
        }
        
        return fileKey;
    }
}
```

#### 7.2.4 临时访问 URL

```java
// MinIO 临时 URL 生成（带签名和过期时间）
public String getPresignedUrl(String fileKey, long expireSeconds) {
    try {
        return minioClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(minioProperties.getBucketName())
                .object(fileKey)
                .expiry(expireSeconds)
                .build()
        );
    } catch (Exception e) {
        throw new BusinessException(ErrorCode.STORAGE_ERROR, e);
    }
}

// OSS 临时 URL 生成（带签名和过期时间）
public String getPresignedUrl(String fileKey, long expireSeconds) {
    Date expiration = new Date(System.currentTimeMillis() + expireSeconds * 1000);
    URL url = ossClient.generatePresignedUrl(
        ossProperties.getBucketName(),
        fileKey,
        expiration
    );
    return url.toString();
}
```

### 7.3 权限矩阵

| 操作 | 权限编码 | 管理员 | 普通用户 | 访客 |
|------|---------|--------|---------|------|
| 上传文件 | file:upload | ✓ | ✓ | ✗ |
| 下载文件 | file:download | ✓ | ✓ | ✗ |
| 预览文件 | file:preview | ✓ | ✓ | ✗ |
| 删除自己的文件 | file:delete | ✓ | ✓ | ✗ |
| 删除任意文件 | file:delete:all | ✓ | ✗ | ✗ |
| 查询文件列表 | file:query | ✓ | ✓ | ✗ |

---

## 8. 技术实现细节

### 8.1 存储服务接口实现

#### FileStorageService.java

```java
package com.cloud.file.storage;

import java.io.InputStream;
import java.util.List;

public interface FileStorageService {
    
    /**
     * 上传文件
     * @param inputStream 文件输入流
     * @param fileName 文件名
     * @param contentType MIME 类型
     * @param fileSize 文件大小
     * @return fileKey 文件唯一标识
     */
    String upload(InputStream inputStream, String fileName, 
                  String contentType, long fileSize);
    
    /**
     * 下载文件
     * @param fileKey 文件唯一标识
     * @return 文件输入流
     */
    InputStream download(String fileKey);
    
    /**
     * 删除文件
     * @param fileKey 文件唯一标识
     * @return 是否成功
     */
    boolean delete(String fileKey);
    
    /**
     * 批量删除文件
     * @param fileKeys 文件唯一标识列表
     * @return 成功删除的数量
     */
    int batchDelete(List<String> fileKeys);
    
    /**
     * 获取临时访问 URL
     * @param fileKey 文件唯一标识
     * @param expireSeconds 过期时间（秒）
     * @return 临时访问 URL
     */
    String getPresignedUrl(String fileKey, long expireSeconds);
    
    /**
     * 检查文件是否存在
     * @param fileKey 文件唯一标识
     * @return 是否存在
     */
    boolean exists(String fileKey);
    
    /**
     * 获取存储类型
     * @return 存储类型枚举
     */
    StorageType getStorageType();
}
```

#### StorageType.java

```java
package com.cloud.file.storage;

import lombok.Getter;

@Getter
public enum StorageType {
    MINIO("minio", "MinIO 对象存储"),
    OSS("oss", "阿里云 OSS 存储");
    
    private final String code;
    private final String description;
    
    StorageType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public static StorageType fromCode(String code) {
        for (StorageType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown storage type: " + code);
    }
}
```

### 8.2 MinIO 存储实现

#### MinioFileServiceImpl.java

```java
package com.cloud.file.storage.impl;

import io.minio.*;
import io.minio.http.Method;
import com.cloud.file.properties.MinioProperties;
import com.cloud.file.storage.FileStorageService;
import com.cloud.file.enums.StorageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioFileServiceImpl implements FileStorageService {
    
    private final MinioProperties minioProperties;
    private final MinioClient minioClient;
    
    @PostConstruct
    public void init() {
        try {
            // 检查并创建 Bucket
            boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .build()
            );
            if (!exists) {
                minioClient.makeBucket(
                    MakeBucketArgs.builder()
                        .bucket(minioProperties.getBucketName())
                        .build()
                );
                log.info("Created MinIO bucket: {}", minioProperties.getBucketName());
            }
        } catch (Exception e) {
            log.error("Failed to initialize MinIO bucket", e);
            throw new RuntimeException("MinIO initialization failed", e);
        }
    }
    
    @Override
    public String upload(InputStream inputStream, String fileName, 
                          String contentType, long fileSize) {
        try {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(fileName)
                    .stream(inputStream, fileSize, -1)
                    .contentType(contentType)
                    .build()
            );
            log.info("File uploaded to MinIO: {}", fileName);
            return fileName;
        } catch (Exception e) {
            log.error("Failed to upload file to MinIO: {}", fileName, e);
            throw new RuntimeException("Upload failed", e);
        }
    }
    
    @Override
    public InputStream download(String fileKey) {
        try {
            return minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(fileKey)
                    .build()
            );
        } catch (Exception e) {
            log.error("Failed to download file from MinIO: {}", fileKey, e);
            throw new RuntimeException("Download failed", e);
        }
    }
    
    @Override
    public boolean delete(String fileKey) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(fileKey)
                    .build()
            );
            log.info("File deleted from MinIO: {}", fileKey);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete file from MinIO: {}", fileKey, e);
            return false;
        }
    }
    
    @Override
    public int batchDelete(List<String> fileKeys) {
        int successCount = 0;
        for (String fileKey : fileKeys) {
            if (delete(fileKey)) {
                successCount++;
            }
        }
        return successCount;
    }
    
    @Override
    public String getPresignedUrl(String fileKey, long expireSeconds) {
        try {
            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(minioProperties.getBucketName())
                    .object(fileKey)
                    .expiry(expireSeconds, TimeUnit.SECONDS)
                    .build()
            );
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for: {}", fileKey, e);
            throw new RuntimeException("Failed to generate presigned URL", e);
        }
    }
    
    @Override
    public boolean exists(String fileKey) {
        try {
            minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(fileKey)
                    .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public StorageType getStorageType() {
        return StorageType.MINIO;
    }
}
```

### 8.3 文件业务服务实现

#### FileService.java

```java
package com.cloud.file.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.file.dto.request.FileUploadRequest;
import com.cloud.file.dto.response.FileResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    
    /**
     * 上传单个文件
     */
    FileResponse uploadFile(MultipartFile file, String businessType, Long businessId);
    
    /**
     * 根据 ID 获取文件信息
     */
    FileResponse getFileById(Long id);
    
    /**
     * 根据 fileKey 获取文件信息
     */
    FileResponse getFileByKey(String fileKey);
    
    /**
     * 下载文件
     */
    byte[] downloadFile(Long id);
    
    /**
     * 获取文件预览 URL
     */
    String getPresignedUrl(Long id, long expireSeconds);
    
    /**
     * 删除文件
     */
    boolean deleteFile(Long id);
    
    /**
     * 批量删除文件
     */
    int batchDeleteFiles(List<Long> fileIds);
    
    /**
     * 分页查询文件
     */
    Page<FileResponse> pageFiles(Integer current, Integer size, 
                                  String businessType, Long uploadedBy);
}
```

#### FileServiceImpl.java（核心逻辑）

```java
package com.cloud.file.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.common.entity.BasePage;
import com.cloud.common.exception.BusinessException;
import com.cloud.common.exception.ErrorCode;
import com.cloud.common.web.UserContext;
import com.cloud.file.converter.FileConverter;
import com.cloud.file.entity.FileInfo;
import com.cloud.file.mapper.FileInfoMapper;
import com.cloud.file.config.FileStorageProperties;
import com.cloud.file.service.FileService;
import com.cloud.file.storage.FileStorageService;
import com.cloud.file.utils.FileKeyGenerator;
import com.cloud.file.dto.response.FileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    
    private final FileStorageService fileStorageService;
    private final FileInfoMapper fileInfoMapper;
    private final FileConverter fileConverter;
    private final FileStorageProperties storageProperties;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileResponse uploadFile(MultipartFile file, String businessType, Long businessId) {
        // 1. 文件校验
        validateFile(file);
        
        // 2. 计算 MD5（用于去重）
        String md5;
        try (InputStream inputStream = file.getInputStream()) {
            md5 = DigestUtil.md5Hex(inputStream);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, 
                "Failed to calculate file MD5");
        }
        
        // 3. 检查文件是否已存在（去重）
        FileInfo existingFile = fileInfoMapper.selectOne(
            new LambdaQueryWrapper<FileInfo>()
                .eq(FileInfo::getFileMd5, md5)
                .eq(FileInfo::getDeleted, false)
        );
        if (existingFile != null) {
            log.info("File already exists, returning existing record: {}", existingFile.getId());
            return fileConverter.toResponse(existingFile);
        }
        
        // 4. 生成 fileKey
        String fileKey = FileKeyGenerator.generateKey(
            businessType != null ? businessType : "general",
            file.getOriginalFilename()
        );
        
        // 5. 上传到存储服务
        try (InputStream inputStream = file.getInputStream()) {
            fileStorageService.upload(
                inputStream,
                fileKey,
                file.getContentType(),
                file.getSize()
            );
        } catch (Exception e) {
            log.error("Failed to upload file to storage: {}", fileKey, e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, 
                "Failed to upload file to storage");
        }
        
        // 6. 保存文件信息到数据库
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileKey(fileKey);
        fileInfo.setOriginalFileName(file.getOriginalFilename());
        fileInfo.setFileExtension(getFileExtension(file.getOriginalFilename()));
        fileInfo.setFileSize(file.getSize());
        fileInfo.setContentType(file.getContentType());
        fileInfo.setStorageType(fileStorageService.getStorageType().getCode());
        fileInfo.setFileUrl(buildFileUrl(fileKey));
        fileInfo.setFileMd5(md5);
        fileInfo.setUploadedBy(UserContext.getUserId());
        fileInfo.setBusinessType(businessType);
        fileInfo.setBusinessId(businessId);
        
        fileInfoMapper.insert(fileInfo);
        
        log.info("File uploaded successfully: id={}, key={}", fileInfo.getId(), fileKey);
        return fileConverter.toResponse(fileInfo);
    }
    
    @Override
    public FileResponse getFileById(Long id) {
        FileInfo fileInfo = fileInfoMapper.selectById(id);
        if (fileInfo == null || fileInfo.getDeleted()) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
        return fileConverter.toResponse(fileInfo);
    }
    
    @Override
    public byte[] downloadFile(Long id) {
        FileInfo fileInfo = getFileById(id);
        try (InputStream inputStream = fileStorageService.download(fileInfo.getFileKey())) {
            return inputStream.readAllBytes();
        } catch (Exception e) {
            log.error("Failed to download file: {}", fileInfo.getFileKey(), e);
            throw new BusinessException(ErrorCode.FILE_DOWNLOAD_FAILED);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFile(Long id) {
        FileInfo fileInfo = fileInfoMapper.selectById(id);
        if (fileInfo == null || fileInfo.getDeleted()) {
            return false;
        }
        
        // 1. 删除存储中的文件
        boolean deleted = fileStorageService.delete(fileInfo.getFileKey());
        if (!deleted) {
            log.warn("Failed to delete file from storage: {}", fileInfo.getFileKey());
        }
        
        // 2. 逻辑删除数据库记录
        fileInfo.setDeleted(true);
        fileInfoMapper.updateById(fileInfo);
        
        return true;
    }
    
    // ... 其他方法实现
    
    private void validateFile(MultipartFile file) {
        // 文件大小校验
        if (file.getSize() > storageProperties.getMaxSize()) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
        }
        
        // 文件扩展名校验
        String extension = getFileExtension(file.getOriginalFilename());
        if (!storageProperties.getAllowedExtensions().contains(extension)) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
    
    private String buildFileUrl(String fileKey) {
        return storageProperties.getBaseUrl() + "/file/download?key=" + fileKey;
    }
}
```

---

## 9. 部署方案

### 9.1 环境要求

| 组件 | 版本要求 | 说明 |
|------|---------|------|
| JDK | 17+ | Java 运行环境 |
| MySQL | 8.0+ | 元数据存储 |
| Nacos | 2.x | 配置中心和服务发现 |
| MinIO | Latest | 对象存储（MinIO 模式） |
| Redis | 6.0+ | 缓存（可选） |

### 9.2 MinIO 部署（Docker）

```bash
# 启动 MinIO 服务
docker run -d \
  --name minio \
  -p 9000:9000 \
  -p 9001:9001 \
  -e "MINIO_ROOT_USER=minioadmin" \
  -e "MINIO_ROOT_PASSWORD=minioadmin" \
  -v /data/minio:/data \
  minio/minio server /data --console-address ":9001"

# 访问 MinIO 控制台
open http://localhost:9001
# 用户名: minioadmin
# 密码: minioadmin
```

### 9.3 Nacos 配置部署

在 Nacos 配置中心创建 `cloud-file.yaml` 配置：

```yaml
# 开发环境配置
file:
  storage:
    type: minio
    max-size: 104857600  # 100MB
    base-url: http://file-service.example.com
    minio:
      endpoint: http://minio.example.com:9000
      access-key: ${MINIO_ACCESS_KEY}
      secret-key: ${MINIO_SECRET_KEY}
      bucket-name: cloud-files-dev
```

```yaml
# 生产环境配置
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

### 9.4 网关路由配置

在 `cloud-gateway` 中添加 cloud-file 路由：

```yaml
# Nacos 配置：cloud-gateway.yaml
spring:
  cloud:
    gateway:
      routes:
        - id: cloud-file
          uri: lb://cloud-file
          predicates:
            - Path=/file/**
          filters:
            - StripPrefix=0

# 白名单配置（如需匿名访问预览）
gateway:
  white-list:
    - /file/anonymous/**
```

### 9.5 启动顺序

```bash
# 1. 启动基础设施
docker-compose up -d mysql nacos redis minio

# 2. 编译项目
mvn clean install -DskipTests

# 3. 启动网关
java -jar cloud-gateway/target/cloud-gateway.jar

# 4. 启动认证服务
java -jar cloud-auth/target/cloud-auth.jar

# 5. 启动文件服务
java -jar cloud-file/target/cloud-file.jar

# 6. 启动其他业务服务（可选）
java -jar cloud-product/target/cloud-product.jar
java -jar cloud-order/target/cloud-order.jar
```

### 9.6 Docker Compose 完整部署

创建 `docker-compose.yml`：

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: cloud-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
    networks:
      - cloud-network

  nacos:
    image: nacos/nacos-server:v2.2.3
    container_name: cloud-nacos
    environment:
      MODE: standalone
      MYSQL_SERVICE_HOST: mysql
      MYSQL_SERVICE_DB_NAME: nacos_config
      MYSQL_SERVICE_USER: root
      MYSQL_SERVICE_PASSWORD: root
    ports:
      - "8848:8848"
      - "9848:9848"
    depends_on:
      - mysql
    networks:
      - cloud-network

  redis:
    image: redis:7-alpine
    container_name: cloud-redis
    ports:
      - "6379:6379"
    networks:
      - cloud-network

  minio:
    image: minio/minio:latest
    container_name: cloud-minio
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin123
    ports:
      - "9000:9000"
      - "9001:9001"
    volumes:
      - minio-data:/data
    command: server /data --console-address ":9001"
    networks:
      - cloud-network

volumes:
  mysql-data:
  minio-data:

networks:
  cloud-network:
    driver: bridge
```

启动：

```bash
docker-compose up -d
```

---

## 10. 测试方案

### 10.1 单元测试

#### 测试文件上传服务

```java
@SpringBootTest
@AutoConfigureMockMvc
class FileServiceTest {
    
    @Autowired
    private FileService fileService;
    
    @MockBean
    private FileStorageService fileStorageService;
    
    @Test
    void testUploadFile_Success() {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            "test content".getBytes()
        );
        
        when(fileStorageService.upload(any(), any(), any(), anyLong()))
            .thenReturn("avatar/2026-06-30/uuid.jpg");
        
        // When
        FileResponse response = fileService.uploadFile(file, "avatar", null);
        
        // Then
        assertNotNull(response);
        assertEquals("test.jpg", response.getOriginalFileName());
        assertEquals("avatar/2026-06-30/uuid.jpg", response.getFileKey());
    }
    
    @Test
    void testUploadFile_FileTooLarge() {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "large.jpg",
            "image/jpeg",
            new byte[20 * 1024 * 1024] // 20MB
        );
        
        // When & Then
        assertThrows(BusinessException.class, () -> {
            fileService.uploadFile(file, "avatar", null);
        });
    }
}
```

### 10.2 集成测试

#### 测试 MinIO 存储实现

```java
@SpringBootTest
@TestClassOrder(OrderAnnotation.class)
class MinioFileIntegrationTest {
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Test
    @Order(1)
    void testUploadAndDownload() {
        String content = "Hello, MinIO!";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());
        
        // Upload
        String fileKey = fileStorageService.upload(
            inputStream, 
            "test.txt", 
            "text/plain", 
            content.length()
        );
        assertNotNull(fileKey);
        
        // Download
        InputStream downloadedStream = fileStorageService.download(fileKey);
        String downloadedContent = new String(downloadedStream.readAllBytes());
        assertEquals(content, downloadedContent);
    }
    
    @Test
    @Order(2)
    void testDelete() {
        // Create file first
        String fileKey = "delete-test.txt";
        fileStorageService.upload(
            new ByteArrayInputStream("test".getBytes()),
            fileKey,
            "text/plain",
            4
        );
        
        // Delete
        boolean deleted = fileStorageService.delete(fileKey);
        assertTrue(deleted);
        
        // Verify
        assertFalse(fileStorageService.exists(fileKey));
    }
}
```

### 10.3 API 测试

#### 使用 cURL 测试

```bash
# 1. 登录获取 Token
TOKEN=$(curl -X POST http://localhost:9527/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  | jq -r '.data.token')

# 2. 上传文件
curl -X POST http://localhost:9527/file/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/path/to/test.jpg" \
  -F "businessType=avatar"

# 3. 下载文件
curl -X GET http://localhost:9527/file/download/1001 \
  -H "Authorization: Bearer $TOKEN" \
  -o downloaded.jpg

# 4. 删除文件
curl -X DELETE http://localhost:9527/file/1001 \
  -H "Authorization: Bearer $TOKEN"

# 5. 分页查询
curl -X GET "http://localhost:9527/file/page?current=1&size=10" \
  -H "Authorization: Bearer $TOKEN"
```

#### 使用 Postman 测试

1. **导入 API 集合**
   - 创建 Postman Collection
   - 添加环境变量：`BASE_URL`, `TOKEN`

2. **上传文件测试**
   - Method: POST
   - URL: `{{BASE_URL}}/file/upload`
   - Headers: `Authorization: Bearer {{TOKEN}}`
   - Body: form-data
     - `file`: [选择文件]
     - `businessType`: avatar
   - Tests: 
     ```javascript
     pm.test("Status code is 200", function () {
         pm.response.to.have.status(200);
     });
     pm.test("File uploaded successfully", function () {
         var json = pm.response.json();
         pm.expect(json.code).to.eql(200);
         pm.expect(json.data.fileKey).to.exist;
     });
     ```

### 10.4 压力测试

#### 使用 JMeter 测试上传性能

```xml
<!-- JMeter Test Plan -->
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2">
  <hashTree>
    <TestPlan>
      <stringProp name="TestPlan.comments">File Upload Load Test</stringProp>
    </TestPlan>
    <hashTree>
      <ThreadGroup>
        <stringProp name="ThreadGroup.num_threads">100</stringProp>
        <stringProp name="ThreadGroup.ramp_time">10</stringProp>
        <stringProp name="ThreadGroup.duration">60</stringProp>
      </ThreadGroup>
    </hashTree>
  </hashTree>
</jmeterTestPlan>
```

**性能指标：**

| 指标 | 目标值 |
|------|--------|
| 并发用户数 | 100 |
| 响应时间（P95） | < 2s |
| 吞吐量 | > 50 请求/秒 |
| 错误率 | < 1% |

---

## 11. 监控与运维

### 11.1 日志监控

```yaml
# logback-spring.xml
<configuration>
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/cloud-file.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/cloud-file.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <logger name="com.cloud.file" level="INFO"/>
    <logger name="com.cloud.file.storage" level="DEBUG"/>
    
    <root level="INFO">
        <appender-ref ref="FILE"/>
        <appender-ref ref="CONSOLE"/>
    </root>
</configuration>
```

### 11.2 健康检查

```java
@Component
public class FileStorageHealthIndicator implements HealthIndicator {
    
    private final FileStorageService fileStorageService;
    
    @Override
    public Health health() {
        try {
            // 检查存储服务连通性
            boolean healthy = fileStorageService.exists("health-check-test");
            return Health.up()
                .withDetail("storage", fileStorageService.getStorageType().getCode())
                .withDetail("status", "connected")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

### 11.3 Prometheus 指标监控

```java
@Configuration
public class MetricsConfig {
    
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags(
            "application", "cloud-file",
            "region", "cn-hangzhou"
        );
    }
}

@Service
public class FileServiceImpl implements FileService {
    
    private final Counter uploadCounter = Metrics.counter("file.upload.count");
    private final Counter uploadFailureCounter = Metrics.counter("file.upload.failure");
    private final Timer uploadTimer = Metrics.timer("file.upload.duration");
    
    public FileResponse uploadFile(...) {
        return uploadTimer.record(() -> {
            try {
                FileResponse response = doUpload(...);
                uploadCounter.increment();
                return response;
            } catch (Exception e) {
                uploadFailureCounter.increment();
                throw e;
            }
        });
    }
}
```

**关键指标：**

| 指标名称 | 类型 | 说明 |
|---------|------|------|
| file_upload_count | Counter | 上传成功总数 |
| file_upload_failure | Counter | 上传失败总数 |
| file_upload_duration | Timer | 上传耗时分布 |
| file_storage_size | Gauge | 存储空间使用量 |
| file_storage_health | Gauge | 存储服务健康状态 |

---

## 12. 扩展与优化

### 12.1 支持更多存储方案

扩展新的存储方案只需两步：

**步骤 1：实现 FileStorageService 接口**

```java
public class TencentCosFileServiceImpl implements FileStorageService {
    // 实现所有接口方法
}
```

**步骤 2：添加条件装配**

```java
@Configuration
public class FileStorageConfig {
    @Bean
    @ConditionalOnProperty(name = "file.storage.type", havingValue = "cos")
    public FileStorageService cosFileStorage(...) {
        return new TencentCosFileServiceImpl(...);
    }
}
```

**步骤 3：添加配置**

```yaml
file:
  storage:
    type: cos
    cos:
      secret-id: ${COS_SECRET_ID}
      secret-key: ${COS_SECRET_KEY}
      region: ap-guangzhou
      bucket-name: cloud-files
```

### 12.2 文件缩略图功能

```java
@Service
public class ThumbnailServiceImpl {
    
    @Async
    public void generateThumbnail(FileInfo fileInfo) {
        try {
            // 下载原文件
            InputStream originalStream = fileStorageService.download(fileInfo.getFileKey());
            
            // 使用 Thumbnailator 生成缩略图
            ByteArrayOutputStream thumbnailStream = new ByteArrayOutputStream();
            Thumbnails.of(originalStream)
                .size(200, 200)
                .toOutputStream(thumbnailStream);
            
            // 上传缩略图
            String thumbnailKey = "thumbnail/" + fileInfo.getFileKey();
            fileStorageService.upload(
                new ByteArrayInputStream(thumbnailStream.toByteArray()),
                thumbnailKey,
                "image/jpeg",
                thumbnailStream.size()
            );
            
            // 保存缩略图信息
            fileInfo.setThumbnailKey(thumbnailKey);
            fileInfoMapper.updateById(fileInfo);
            
        } catch (Exception e) {
            log.error("Failed to generate thumbnail for file: {}", fileInfo.getId(), e);
        }
    }
}
```

### 12.3 大文件分片上传

```java
@RestController
@RequestMapping("/file/chunk")
public class FileChunkController {
    
    @PostMapping("/init")
    public Result<String> initChunkUpload(
        @RequestParam String fileName,
        @RequestParam Long fileSize,
        @RequestParam String fileMd5
    ) {
        // 生成上传 ID
        String uploadId = UUID.randomUUID().toString();
        // 保存上传任务信息到 Redis
        redisTemplate.opsForHash().put("upload:" + uploadId, "fileName", fileName);
        redisTemplate.opsForHash().put("upload:" + uploadId, "fileSize", fileSize);
        redisTemplate.opsForHash().put("upload:" + uploadId, "fileMd5", fileMd5);
        redisTemplate.opsForHash().put("upload:" + uploadId, "uploadedChunks", "0");
        redisTemplate.expire("upload:" + uploadId, 1, TimeUnit.DAYS);
        
        return Result.success(uploadId);
    }
    
    @PostMapping("/upload")
    public Result<Void> uploadChunk(
        @RequestParam String uploadId,
        @RequestParam Integer chunkNumber,
        @RequestParam MultipartFile chunk
    ) {
        // 上传分片到存储（临时路径）
        String tempKey = "temp/" + uploadId + "/chunk_" + chunkNumber;
        fileStorageService.upload(
            chunk.getInputStream(),
            tempKey,
            chunk.getContentType(),
            chunk.getSize()
        );
        
        // 更新已上传分片数
        redisTemplate.opsForHash().increment("upload:" + uploadId, "uploadedChunks", 1);
        
        return Result.success();
    }
    
    @PostMapping("/complete")
    public Result<FileResponse> completeChunkUpload(
        @RequestParam String uploadId
    ) {
        // 合并分片
        // 1. 获取所有分片 Key
        // 2. 按顺序合并
        // 3. 上传完整文件
        // 4. 删除临时分片
        // 5. 保存文件信息
        
        return Result.success(fileResponse);
    }
}
```

### 12.4 文件过期清理

```java
@Component
public class FileCleanupTask {
    
    @Scheduled(cron = "0 0 2 * * ?")  // 每天凌晨 2 点执行
    public void cleanupExpiredFiles() {
        log.info("Starting file cleanup task...");
        
        // 查询过期文件
        List<FileInfo> expiredFiles = fileInfoMapper.selectList(
            new LambdaQueryWrapper<FileInfo>()
                .eq(FileInfo::getDeleted, false)
                .lt(FileInfo::getExpireTime, new Date())
        );
        
        int deletedCount = 0;
        for (FileInfo file : expiredFiles) {
            try {
                // 删除存储中的文件
                fileStorageService.delete(file.getFileKey());
                
                // 逻辑删除数据库记录
                file.setDeleted(true);
                fileInfoMapper.updateById(file);
                
                deletedCount++;
            } catch (Exception e) {
                log.error("Failed to delete expired file: {}", file.getId(), e);
            }
        }
        
        log.info("File cleanup completed. Deleted {} files.", deletedCount);
    }
}
```

---

## 附录

### A. 错误码定义

| 错误码 | 说明 |
|--------|------|
| 2001 | 文件不存在 |
| 2002 | 文件上传失败 |
| 2003 | 文件下载失败 |
| 2004 | 文件删除失败 |
| 2005 | 文件过大 |
| 2006 | 不支持的文件类型 |
| 2007 | 文件内容类型不匹配 |
| 2008 | 存储服务错误 |
| 2009 | 无效的文件 Key |
| 2010 | 文件已存在 |

### B. 业务类型定义

| 业务类型 | 说明 | 示例用途 |
|---------|------|---------|
| avatar | 用户头像 | 用户头像上传 |
| product | 产品图片 | 商品图片 |
| order | 订单附件 | 订单相关文件 |
| document | 文档资料 | 公文、合同等 |
| certificate | 证书文件 | 资质证书 |
| other | 其他 | 未分类文件 |

### C. 参考资源

- MinIO 官方文档：https://min.io/docs/minio/linux/index.html
- 阿里云 OSS 文档：https://help.aliyun.com/product/31815.html
- Spring Boot 文档：https://spring.io/projects/spring-boot
- MyBatis-Plus 文档：https://baomidou.com/

---

**文档版本历史：**

| 版本 | 日期 | 变更说明 | 作者 |
|------|------|---------|------|
| v1.0 | 2026-06-30 | 初始版本 | Cloud Project Team |
