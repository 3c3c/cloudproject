# Nacos 配置中心 / 注册中心（开发环境）

配套 Spring Cloud Alibaba **2023.0.1.0**（自带 Nacos Client 2.3.2），故 Server 选用 **v2.3.2**。

## 目录结构

```
docker/nacos/
├── docker-compose.yml        # Nacos + MySQL + Redis 一键启动
├── init-configs.sh           # 把 configs/ 下配置一键导入 Nacos
└── configs/                  # 各项目在 Nacos 上的配置（参考/导入源）
    ├── cloud-common.yaml     # 共享：Redis / JWT / MyBatis-Plus / 日志
    ├── cloud-auth.yaml       # auth 私有：数据源 + 建表
    ├── cloud-product.yaml    # product 私有：数据源
    ├── cloud-order.yaml      # order 私有：数据源 + Feign 日志
    └── cloud-gateway.yaml    # gateway 私有：路由 + 白名单
```

## 三步启动

### 1. 启动中间件（Nacos + MySQL + Redis）

```bash
docker compose -f docker/nacos/docker-compose.yml up -d
```

启动后：

| 服务 | 地址 | 账号 |
|---|---|---|
| Nacos 控制台 | http://localhost:8848/nacos | nacos / nacos |
| MySQL | localhost:3306 | root / root |
| Redis | localhost:6379 | 无密码 |

> 等待 Nacos 就绪（看到日志 `Nacos started successfully`），约 20~30 秒。

### 2. 把配置导入 Nacos

```bash
bash docker/nacos/init-configs.sh
```

脚本会登录 Nacos、把 `configs/` 下 5 个 yaml 发布到 **public** 命名空间、`DEFAULT_GROUP`。
完成后可在控制台「配置管理 → 配置列表」看到它们。

> 该脚本是为了免去手动逐条粘贴；你也可以在控制台手动新建（dataId/group/内容与 `configs/` 文件一一对应）。

### 3. 启动各服务

正常用 IDE 或 `mvn spring-boot:run` 启动 gateway / auth / product / order。
每个服务的本地 `application.yml` 现在只剩**端口 + 应用名 + Nacos 连接**，其余全部从 Nacos 拉取。

## 环境变量（可选覆盖）

| 变量 | 默认值 | 说明 |
|---|---|---|
| `NACOS_ADDR` | `127.0.0.1:8848` | Nacos 地址 |
| `NACOS_NS` | 空（= public） | 命名空间 ID；留空即 public |
| `NACOS_USER` / `NACOS_PASSWORD` | `nacos` / `nacos` | Nacos 鉴权账号 |
| `MYSQL_HOST` / `MYSQL_PORT` / `MYSQL_USER` / `MYSQL_PASSWORD` | `127.0.0.1` / `3306` / `root` / `root` | 业务库连接 |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | `127.0.0.1` / `6379` / 空 | Redis 连接 |
| `JWT_SECRET` | 内置默认值 | JWT 签名密钥（生产务必覆盖） |

## 常见坑

1. **必须放行 9848 端口**：Nacos 2.x 客户端用 gRPC 通信（端口 = 主端口 + 1000）。
   `docker-compose.yml` 已映射 `9848`，若自行部署漏了，服务会连不上 Nacos（报 connection refused）。
2. **命名空间用空串表示 public**：Nacos 的 `public` 命名空间 ID 是**空串**，不是字符串 `"public"`。
   本项目本地配置写 `namespace: ${NACOS_NS:}`，默认空 = public，与控制台默认视图一致。
3. **鉴权默认开启**：Nacos 2.2+ 启用鉴权，三个 token（`NACOS_AUTH_TOKEN` / `IDENTITY_KEY` / `VALUE`）必填，
   否则控制台登录或 Open API 会 403。`docker-compose.yml` 已配好开发占位值，**生产请替换**。
4. **生产建议接 MySQL**：本 compose 用 Nacos 内嵌 Derby（单机、数据不迁移）。
   生产请改用外部 MySQL，在 Nacos 环境变量里配置 `SPRING_DATASOURCE_PLATFORM=mysql`、
   `MYSQL_SERVICE_HOST/PORT/DB_NAME/USER/PASSWORD` 并初始化 `nacos/conf/mysql-schema.sql`。

## 配置在哪改

- 想改某服务的数据库/路由/白名单 → 改对应 `configs/<服务名>.yaml`，导入到 Nacos（或在控制台直接编辑，Nacos 支持热更新）。
- 想改 Redis/JWT/MyBatis 等公共项 → 改 `configs/cloud-common.yaml`。
- 改完 `configs/` 文件后重新跑 `init-configs.sh` 即可覆盖更新。
