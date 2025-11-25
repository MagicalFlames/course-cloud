# 第九周作业：API 网关与统一认证

## 一、实验目标

1. 使用 Spring Cloud Gateway 构建 API 网关
2. 实现基于 JWT 的统一身份认证
3. 配置服务路由和跨域支持
4. 在网关层统一处理认证和鉴权逻辑

## 二、系统架构

### 服务列表

- **gateway-service** (端口 8090): API 网关，负责路由转发和 JWT 认证
- **user-service** (端口 8083): 用户服务，提供用户注册、登录和信息查询
- **catalog-service** (端口 8081): 课程目录服务
- **enrollment-service** (端口 8082): 选课服务
- **nacos** (端口 8848): 服务注册与发现中心

### 架构图

```
客户端
  ↓
API Gateway (8090)
  ├─ JWT 认证过滤器
  ├─ 路由转发
  └─ CORS 配置
  ↓
├─→ user-service (8083)
├─→ catalog-service (8081)
└─→ enrollment-service (8082)
```

## 三、核心实现

### 1. 用户服务 (user-service)

#### 用户实体
- 使用 H2 内存数据库
- 用户属性：id (UUID), username, password, email, realName, role
- 支持的角色：STUDENT, TEACHER, ADMIN

#### JWT 工具类 (JwtUtil)

```java
- 算法：HS512
- 密钥长度：256 位
- Token 有效期：24 小时
- Token 包含信息：userId (subject), username, role
```

主要方法：
- `generateToken()`: 生成 JWT token
- `parseToken()`: 解析 token 获取用户信息
- `validateToken()`: 验证 token 是否有效

#### 认证接口

**注册接口** POST `/auth/register`
- 请求体：username, password, email, realName, role
- 返回：JWT token 和用户信息

**登录接口** POST `/auth/login`
- 请求体：username, password
- 返回：JWT token 和用户信息

**用户信息接口** GET `/users/me`
- 需要认证
- 从请求头获取：X-User-Id, X-Username, X-User-Role
- 返回：用户信息

### 2. 网关服务 (gateway-service)

#### JWT 认证过滤器 (JwtAuthenticationFilter)

实现 `GlobalFilter` 和 `Ordered` 接口，执行顺序为 -100（优先级高）

**认证流程：**
1. 检查请求路径是否在白名单中
   - 白名单：`/api/auth/login`, `/api/auth/register`, `/actuator`
   - 白名单路径直接放行
2. 从 Authorization header 提取 Bearer token
3. 验证 token 有效性
4. 解析 token 获取用户信息
5. 将用户信息添加到请求头：
   - `X-User-Id`: 用户 ID
   - `X-Username`: 用户名
   - `X-User-Role`: 用户角色
6. 转发请求到后端服务

**错误处理：**
- 缺少 token：返回 401 Unauthorized，错误信息"缺少认证令牌"
- token 无效/过期：返回 401 Unauthorized，错误信息"令牌无效或已过期"

#### 路由配置

```yaml
spring:
  cloud:
    gateway:
      routes:
        # 认证服务路由
        - id: auth-service
          uri: http://user-service:8083
          predicates:
            - Path=/api/auth/**
          filters:
            - StripPrefix=1

        # 用户服务路由
        - id: user-service
          uri: http://user-service:8083
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=1

        # 课程目录服务路由
        - id: catalog-service
          uri: http://catalog-service-1:8081
          predicates:
            - Path=/api/catalog/**
          filters:
            - StripPrefix=1

        # 选课服务路由
        - id: enrollment-service
          uri: http://enrollment-service:8082
          predicates:
            - Path=/api/enrollment/**
          filters:
            - StripPrefix=1
```

#### CORS 配置

```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowed-origins: "*"
            allowed-methods: [GET, POST, PUT, DELETE, OPTIONS]
            allowed-headers: "*"
            allow-credentials: false
            max-age: 3600
```

## 四、测试验证

### 1. 登录测试

**请求：**
```bash
curl -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"student1","password":"pass123"}'
```

**响应：**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "user": {
    "id": "6e3aab5f-23b1-4e11-81da-399c7b0016c4",
    "username": "student1",
    "role": "STUDENT",
    "email": "student1@example.com",
    "realName": "张三"
  }
}
```

### 2. 未认证访问测试

**请求：**
```bash
curl -X GET http://localhost:8090/api/catalog/courses
```

**响应：**
```
HTTP/1.1 401 Unauthorized
{"error": "缺少认证令牌"}
```

### 3. 认证访问测试

**请求：**
```bash
curl -X GET http://localhost:8090/api/users/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

**响应：**
```
HTTP/1.1 200 OK
{
  "success": true,
  "message": "Authentication successful",
  "userId": "6e3aab5f-23b1-4e11-81da-399c7b0016c4",
  "username": "student1",
  "role": "STUDENT"
}
```

### 4. 后端服务日志

```
user-service  | Received user info from gateway:
                userId=6e3aab5f-23b1-4e11-81da-399c7b0016c4,
                username=student1,
                role=STUDENT
```

## 五、技术要点

### 1. Spring Boot 版本兼容性

- Spring Boot 3.3.5（而非 3.5.7）
- Spring Cloud 2023.0.3
- Spring Cloud Alibaba 2023.0.1.0
- 解决方案：修改 pom.xml 中的 Spring Boot 版本

### 2. 服务发现配置

由于 Nacos gRPC 端口（9848）连接问题，采用直接 URI 配置：
- 优点：启动快速，无需等待服务注册
- 缺点：不支持负载均衡（但本作业中可接受）

### 3. JWT 配置

- 密钥必须至少 256 位（32 字节）以满足 HS512 算法要求
- Token 过期时间：86400000 毫秒（24 小时）
- 用户信息存储在 claims 中

### 4. 请求头传递

网关通过请求头将用户信息传递给后端服务：
- 后端服务无需再次解析 JWT
- 简化后端服务的认证逻辑
- 提高性能

## 六、遇到的问题与解决方案

### 问题 1：Spring Boot 版本不兼容

**现象：** 服务启动失败，提示 Spring Boot 3.5.7 与 Spring Cloud 不兼容

**解决：** 将 Spring Boot 版本降级到 3.3.5

### 问题 2：Nacos gRPC 连接超时

**现象：** 服务无法连接到 Nacos 的 gRPC 端口 9848

**解决：**
- 禁用 Nacos 服务发现：`spring.cloud.nacos.discovery.enabled=false`
- 使用直接 URI 配置路由

### 问题 3：网关无法访问后端服务

**现象：** ping user-service 失败，100% 丢包

**解决：** 确保所有服务在同一个 Docker 网络中（course-network）

### 问题 4：Authorization header 值丢失

**现象：** 日志显示"缺少Token或格式错误: Bearer"，token 部分丢失

**解决：** 在 curl 命令中使用单引号包裹 header 值

## 七、总结

本次作业成功实现了：

1. ✅ API 网关的搭建和配置
2. ✅ JWT 认证机制的实现
3. ✅ 统一认证过滤器
4. ✅ 服务路由配置
5. ✅ CORS 跨域支持
6. ✅ 用户信息请求头传递
7. ✅ Docker Compose 多服务编排

通过本次实验，深入理解了微服务架构中 API 网关的作用，以及如何实现统一的身份认证和鉴权机制。
