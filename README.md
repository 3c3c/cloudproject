# SpringCloud 登录认证微服务后端（RBAC）

基于 **JWT + Redis + Spring Security** 的微服务认证授权工程，实现统一网关鉴权、RBAC 权限模型、账号密码与手机验证码双登录、Feign 服务间调用。技术方案对齐 `aaa.md`。

## 一、技术栈

| 组件 | 版本 |
| :-- | :-- |
| JDK | 17 |
| Spring Boot | 3.2.5 |
| Spring Cloud | 2023.0.1 |
| Spring Cloud Alibaba | 2023.0.1.0 |
| Spring Security | 6（lambda DSL） |
| MyBatis-Plus（Spring Boot 3） | 3.5.5 |
| jjwt | 0.11.5 |
| MySQL | 8.x |
| Redis | 任意稳定版 |
| Nacos | 2.x |

## 二、架构

```
                       ┌───────────────────────────┐
  前端 ──Bearer JWT──► │  cloud-gateway  (9527)    │  全局过滤器验签+黑名单
                       │  白名单:/auth/**          │  透传 X-User-Id/Name/Auths
                       └──────┬──────────┬─────────┘
                              │          │
                ┌─────────────┘          └──────────────┐
                ▼                                       ▼
   ┌────────────────────────┐            ┌──────────────────────┐
   │ cloud-auth    (7001)   │            │ cloud-product (8001) │
   │ 登录/注册/发码/退出     │            │ 产品 CRUD + RBAC     │
   │ Security+JWT+Redis     │            │ UserContextFilter    │
   │ RBAC 查询权限          │            └──────────▲───────────┘
   └────────────────────────┘                       │ Feign(透传Header)
                                          ┌─────────┴────────────┐
                                          │ cloud-order  (8002)  │
                                          │ 订单 CRUD + RBAC     │
                                          │ 下单调用 product     │
                                          └──────────────────────┘
```

**职责划分（对齐 aaa.md）**
- **JWT**：无状态身份凭证，携带 userId / username / authorities
- **Redis**：`login:token:{username}`（单点登录/强制下线）、`blacklist:{token}`（退出失效）、`sms:code:{mobile}`（验证码）
- **Gateway**：统一验签 + 透传用户信息（aaa.md 网关方案）
- **业务服务**：解析 `X-User-*` 头构建认证上下文，方法级 `@PreAuthorize`
- **Feign**：服务间调用透传凭证（aaa.md 无网关 Feign 方案）

## 三、模块

| 模块 | 端口 | 说明 |
| :-- | :-- | :-- |
| `cloud-common` | - | 公共：Result/异常/JwtUtils/LoginUser/UserContextFilter/常量 |
| `cloud-gateway` | 9527 | 统一入口、JWT 鉴权、用户信息透传（WebFlux） |
| `cloud-auth` | 7001 | 登录/注册/验证码/退出 + RBAC 数据源 |
| `cloud-product` | 8001 | 产品 CRUD，`@PreAuthorize` 控制 `product:*` |
| `cloud-order` | 8002 | 订单 CRUD，`order:*`，Feign 调 product |

## 四、RBAC 数据模型（`cloud_auth` 库）

```
sys_user ──< sys_user_role >── sys_role ──< sys_role_permission >── sys_permission
```

| 账号 | 密码 | 角色 | 权限 |
| :-- | :-- | :-- | :-- |
| admin | 123456 | ROLE_ADMIN | product:* / order:* |
| user | 123456 | ROLE_USER | product:query / order:query（**无 order:create**，演示 403） |
| 13800000000 | 验证码 | ROLE_USER | 验证码登录自动注册 |

> 初始密码以明文写入 `data.sql`，应用启动后由 `PasswordDataInitializer` 自动转 BCrypt，幂等。

## 五、准备运行环境

1. **JDK 17**、**Maven 3.8+**
2. **MySQL 8**：连接串含 `createDatabaseIfNotExist=true`，启动时自动建库（`cloud_auth` / `cloud_product` / `cloud_order`）并执行 `schema.sql` / `data.sql`。默认账号 `root/root`，可用环境变量 `MYSQL_USER` / `MYSQL_PASSWORD` 覆盖。
3. **Redis**：默认 `127.0.0.1:6379`，可用 `REDIS_HOST` / `REDIS_PORT` 覆盖。
4. **Nacos 2.x**：默认 `127.0.0.1:8848`，可用 `NACOS_ADDR` 覆盖。
   - （可选）在 Nacos 新建 `cloud-common.yaml`（Group `DEFAULT_GROUP`）放置共享配置，例如：
     ```yaml
     jwt:
       secret: your-very-strong-secret-at-least-32-bytes-long
     spring:
       data:
         redis:
           host: 127.0.0.1
     ```
   - 即使不建该配置，各服务本地 `application.yml` 也有同名兜底值，仍可启动。

## 六、启动顺序

```bash
# 在项目根目录编译
mvn clean install -DskipTests

# 依次启动（顺序不强制，但建议先网关）
java -jar cloud-gateway/target/cloud-gateway.jar
java -jar cloud-auth/target/cloud-auth.jar
java -jar cloud-product/target/cloud-product.jar
java -jar cloud-order/target/cloud-order.jar
```

或在 IDE 中分别运行各模块的 `*Application` 主类。

## 七、接口测试（经网关 9527）

### 1. 账号密码登录（admin）
```bash
TOKEN=$(curl -s -X POST http://localhost:9527/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}' | jq -r .data.token)
echo $TOKEN
```

### 2. 访问产品（需要 product:query，admin 有）
```bash
curl -s http://localhost:9527/products -H "Authorization: Bearer $TOKEN"
```

### 3. RBAC 拒绝（user 无 order:create → 403）
```bash
UTOKEN=$(curl -s -X POST http://localhost:9527/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"123456"}' | jq -r .data.token)

# user 下单 → 403
curl -s -X POST http://localhost:9527/orders \
  -H "Authorization: Bearer $UTOKEN" \
  -H "Content-Type: application/json" \
  -d '{"productId":1,"quantity":2}'
```

### 4. Feign 联调（admin 下单 → order 内部 Feign 调 product 成功）
```bash
curl -s -X POST http://localhost:9527/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"productId":1,"quantity":2}'
```

### 5. 手机验证码登录（首次自动注册）
```bash
# 发送验证码（验证码会打印在 auth 服务日志中）
curl -s -X POST http://localhost:9527/auth/sms/send \
  -H "Content-Type: application/json" -d '{"mobile":"13800000000"}'

# 用日志中的验证码登录（注意是表单参数）
curl -s -X POST "http://localhost:9527/auth/sms/login" \
  -d "mobile=13800000000&code=123456"
```

### 6. 当前用户信息 / 退出 / 刷新
```bash
curl -s http://localhost:9527/auth/user/info   -H "Authorization: Bearer $TOKEN"
curl -s -X POST http://localhost:9527/auth/refresh -H "Authorization: Bearer $TOKEN"
curl -s -X POST http://localhost:9527/auth/logout -H "Authorization: Bearer $TOKEN"
```

### 7. 单点登录验证
同一 `admin` 再次登录会覆盖 Redis 中的 token，旧 token 立即失效（401）。

## 八、关键设计说明

- **网关 vs 业务服务鉴权**：网关验签 + 透传；业务服务只解析 `X-User-*` 头，**不持有 JWT 密钥**。
- **authorities 组装**：`UserDetailsServiceImpl` 同时放入 `ROLE_xxx`（`hasRole` 匹配去掉 `ROLE_` 前缀）与 `perm_code`（`hasAuthority` 直接匹配）。
- **Feign 异步**：示例为同步调用；若用 `@Async`，`RequestContextHolder` 会丢失，需配 `TaskDecorator` 透传（aaa.md 已述）。
- **状态管理**：JWT 无状态身份 + Redis 有状态令牌，二者结合实现单点登录、强制下线、退出黑名单。

## 九、目录结构

```
cloudproject/
├── pom.xml                  父 POM（锁定版本、聚合模块）
├── cloud-common/            公共模块
├── cloud-gateway/           网关
├── cloud-auth/              认证服务（RBAC 数据源）
├── cloud-product/           产品服务
└── cloud-order/             订单服务（Feign）
```
