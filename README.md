# 校园选课系统（微服务版）- course-cloud

## 项目信息

- **项目名称**: course-cloud
- **版本号**: v1.1.0
- **基于版本**: course:v1.1.0 (hw04b)
- **项目阶段**: 微服务架构（服务注册与发现）
- **Git 分支**: main

## 项目简介

本项目是将单体选课系统拆分为微服务架构的实践项目。通过将原有的单体应用拆分为两个独立的微服务，实现了服务间的解耦，提高了系统的可扩展性和可维护性。

### 拆分策略

原单体应用结构：
```
course-system（单体应用）
├── Course（课程）
├── Student（学生）
└── Enrollment（选课）
```

拆分后的微服务架构：
```
catalog-service（课程目录服务）:8081
└── Course（课程管理）

enrollment-service（选课服务）:8082
├── Student（学生管理）
└── Enrollment（选课管理，通过 HTTP 调用 catalog-service 验证课程）
```

## 架构图

```
客户端
 ↓
 ├─→ catalog-service (8081) → catalog_db (3307)
 │   └── 课程管理
 │
 └─→ enrollment-service (8082) → enrollment_db (3308)
     ├── 学生管理
     ├── 选课管理
     └── HTTP 调用 → catalog-service（验证课程）
```

## 技术栈

- **Spring Boot**: 3.5.7
- **Java**: 17
- **MySQL**: 8.4
- **Docker**: 20.10+
- **Docker Compose**: 2.0+
- **Nacos**: 2.3.0 (服务注册与发现)
- **Spring Cloud Alibaba**: 2023.0.1.0
- **RestTemplate + DiscoveryClient**: 服务间通信

## 环境要求

- JDK 25+
- Maven 3.8+
- Docker 20.10+
- Docker Compose 2.0+

## 项目结构

```
course-cloud/
├── README.md                   # 项目文档
├── docker-compose.yml          # Docker 编排文件
├── test-services.sh            # 测试脚本
├── VERSION                     # 版本号文件（1.0.0）
│
├── catalog-service/            # 课程目录服务
│   ├── src/
│   │   └── main/
│   │       ├── java/com/zjgsu/wzy/catalog/
│   │       │   ├── model/
│   │       │   │   ├── Course.java
│   │       │   │   ├── Instructor.java
│   │       │   │   └── ScheduleSlot.java
│   │       │   ├── repository/
│   │       │   │   └── CourseRepository.java
│   │       │   ├── service/
│   │       │   │   └── CourseService.java
│   │       │   ├── controller/
│   │       │   │   └── CourseController.java
│   │       │   ├── common/
│   │       │   │   └── ApiResponse.java
│   │       │   └── CatalogServiceApplication.java
│   │       └── resources/
│   │           ├── application.yml
│   │           └── application-prod.yml
│   ├── Dockerfile
│   └── pom.xml
│
└── enrollment-service/         # 选课服务
    ├── src/
    │   └── main/
    │       ├── java/com/zjgsu/wzy/enrollment/
    │       │   ├── model/
    │       │   │   ├── Student.java
    │       │   │   ├── Enrollment.java
    │       │   │   └── EnrollmentStatus.java
    │       │   ├── repository/
    │       │   │   ├── StudentRepository.java
    │       │   │   └── EnrollmentRepository.java
    │       │   ├── service/
    │       │   │   ├── StudentService.java
    │       │   │   └── EnrollmentService.java
    │       │   ├── controller/
    │       │   │   ├── StudentController.java
    │       │   │   └── EnrollmentController.java
    │       │   ├── common/
    │       │   │   └── ApiResponse.java
    │       │   ├── exception/
    │       │   │   ├── ResourceNotFoundException.java
    │       │   │   └── BusinessException.java
    │       │   └── EnrollmentServiceApplication.java
    │       └── resources/
    │           ├── application.yml
    │           └── application-prod.yml
    ├── Dockerfile
    └── pom.xml
```

## 构建和运行

### 方法 1: 使用 Docker Compose（推荐）

1. **进入项目目录**
   ```bash
   cd course-cloud
   ```

2. **构建 JAR 包**
   ```bash
   cd catalog-service && mvn clean package -DskipTests && cd ..
   cd enrollment-service && mvn clean package -DskipTests && cd ..
   ```

3. **启动所有服务**
   ```bash
   docker-compose up -d --build
   ```

4. **查看服务状态**
   ```bash
   docker-compose ps
   ```

5. **查看日志**
   ```bash
   docker-compose logs -f catalog-service
   docker-compose logs -f enrollment-service
   ```

6. **停止所有服务**
   ```bash
   docker-compose down
   ```

7. **清理数据卷（重新开始）**
   ```bash
   docker-compose down -v
   ```

### 方法 2: 本地运行

1. **启动 catalog-service**
   ```bash
   cd catalog-service
   mvn spring-boot:run
   ```

2. **启动 enrollment-service**（在新终端）
   ```bash
   cd enrollment-service
   mvn spring-boot:run
   ```

## API 文档

### Swagger/OpenAPI 文档

两个服务都集成了 Swagger UI，可以通过浏览器访问交互式 API 文档：

- **课程目录服务**: http://localhost:8081/swagger-ui.html
- **选课服务**: http://localhost:8082/swagger-ui.html

OpenAPI 规范 JSON：
- 课程目录服务: http://localhost:8081/v3/api-docs
- 选课服务: http://localhost:8082/v3/api-docs

### 课程目录服务 (catalog-service) - http://localhost:8081

#### 课程管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/courses | 获取所有课程 |
| GET | /api/courses/{id} | 获取单个课程 |
| GET | /api/courses/code/{code} | 按课程代码查询 |
| POST | /api/courses | 创建课程 |
| PUT | /api/courses/{id} | 更新课程 |
| DELETE | /api/courses/{id} | 删除课程 |

### 选课服务 (enrollment-service) - http://localhost:8082

#### 学生管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/students | 获取所有学生 |
| GET | /api/students/{id} | 获取单个学生 |
| POST | /api/students | 创建学生 |
| PUT | /api/students/{id} | 更新学生 |
| DELETE | /api/students/{id} | 删除学生 |

#### 选课管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/enrollments | 获取所有选课记录 |
| POST | /api/enrollments | 学生选课 |
| GET | /api/enrollments/course/{courseId} | 按课程查询选课 |
| GET | /api/enrollments/student/{studentId} | 按学生查询选课 |
| DELETE | /api/enrollments/{id} | 学生退课 |

## 测试说明

运行测试脚本：
```bash
./test-services.sh
```

测试脚本会自动执行以下测试：
1. 创建课程
2. 获取所有课程
3. 创建学生
4. 获取所有学生
5. 学生选课（验证服务间通信）
6. 查询选课记录
7. 测试选课失败（课程不存在）

### 手动测试示例

1. **创建课程**
   ```bash
   curl -X POST http://localhost:8081/api/courses \
     -H "Content-Type: application/json" \
     -d '{
     "code": "CS101",
     "title": "计算机科学导论",
     "instructor": {
       "id": "T001",
       "name": "张教授",
       "email": "zhang@example.edu.cn"
     },
     "scheduleSlot": {
       "dayOfWeek": "MONDAY",
       "startTime": "08:00",
       "endTime": "10:00",
       "expectedAttendance": 50
     },
     "capacity": 60,
     "enrolledCount": 0
   }'
   ```

2. **创建学生**
   ```bash
   curl -X POST http://localhost:8082/api/students \
     -H "Content-Type: application/json" \
     -d '{
     "studentId": "2024001",
     "name": "张三",
     "major": "计算机科学与技术",
     "grade": 2024,
     "email": "zhangsan@example.edu.cn"
   }'
   ```

3. **学生选课**
   ```bash
   curl -X POST http://localhost:8082/api/enrollments \
     -H "Content-Type: application/json" \
     -d '{
     "courseId": "<课程ID>",
     "studentId": "2024001"
   }'
   ```

## 数据库配置

### catalog-db (课程目录数据库)
- 主机: localhost
- 端口: 3307
- 数据库名: catalog_db
- 用户名: catalog_user
- 密码: catalog_pass

### enrollment-db (选课数据库)
- 主机: localhost
- 端口: 3308
- 数据库名: enrollment_db
- 用户名: enrollment_user
- 密码: enrollment_pass

## 服务注册与发现 (Nacos)

### Nacos 控制台

- **访问地址**: http://localhost:8848/nacos
- **默认账号**: nacos / nacos

### 服务注册

两个微服务都会自动注册到 Nacos：
- `catalog-service` - 课程目录服务
- `enrollment-service` - 选课服务

### 服务发现

enrollment-service 通过 DiscoveryClient 从 Nacos 获取 catalog-service 的地址：

```java
@Autowired
private DiscoveryClient discoveryClient;

private String getCatalogServiceUrl() {
    List<ServiceInstance> instances = discoveryClient.getInstances("catalog-service");
    if (!instances.isEmpty()) {
        return instances.get(0).getUri().toString();
    }
    return catalogServiceUrl; // 回退到配置的 URL
}
```

### 健康检查端点

- catalog-service: http://localhost:8081/actuator/health
- enrollment-service: http://localhost:8082/actuator/health

### Nacos 测试脚本

```bash
./scripts/nacos-test.sh
```

测试内容包括：
1. Nacos 控制台可用性
2. 服务注册状态
3. 服务实例详情
4. 健康检查端点
5. 通过服务发现进行服务间调用

## 服务间通信

enrollment-service 通过 RestTemplate + DiscoveryClient 调用 catalog-service 的 API：

```java
// 从 Nacos 获取服务地址
String url = getCatalogServiceUrl() + "/api/courses/" + courseId;

// 验证课程存在性
Map<String, Object> courseResponse = restTemplate.getForObject(url, Map.class);

// 更新课程已选人数
restTemplate.put(url, updateData);
```

## 遇到的问题和解决方案

### 1. 服务间通信失败
**问题**: enrollment-service 无法调用 catalog-service

**解决方案**:
- 确保 catalog-service 已启动
- 检查网络连接：`docker network inspect course-cloud_course-network`
- 验证环境变量配置是否正确

### 2. 数据库连接失败
**问题**: 服务无法连接到数据库

**解决方案**:
- 等待数据库健康检查完成
- 检查数据库日志：`docker-compose logs catalog-db`
- 确保数据库容器已完全启动

### 3. 端口冲突
**问题**: 端口已被占用

**解决方案**:
- 检查端口占用：`netstat -tulpn | grep 8081`
- 修改 docker-compose.yml 中的端口映射

## 微服务架构的优势

1. **服务解耦**: 课程管理和选课管理独立部署和扩展
2. **技术栈灵活**: 每个服务可以使用不同的技术栈
3. **独立部署**: 更新一个服务不影响其他服务
4. **容错性**: 一个服务故障不会导致整个系统崩溃
5. **可扩展性**: 可以根据负载独立扩展每个服务

## 与单体应用的对比

| 特性 | 单体应用 | 微服务架构 |
|------|---------|-----------|
| 部署 | 整体部署 | 独立部署 |
| 扩展 | 整体扩展 | 按需扩展 |
| 技术栈 | 统一 | 灵活 |
| 复杂度 | 较低 | 较高 |
| 服务通信 | 本地调用 | HTTP/RPC |
| 数据库 | 共享 | 独立 |

## 贡献者

- 王振宇 (wzy) - Initial work

## 许可证

本项目为教学项目，仅供学习使用。

## 联系方式

如有问题，请联系：wzy@zjgsu.edu.cn

## 思考题

1. **如果 catalog-service 宕机，enrollment-service 会如何？如何提高可用性？**
   - 当前实现：enrollment-service 会抛出异常，选课失败
   - 改进方案：
     - 实现服务熔断器（Circuit Breaker）
     - 添加缓存机制
     - 实现重试策略
     - 使用服务注册与发现

2. **如何实现课程人数的实时更新？**
   - 当前实现：选课时同步更新
   - 改进方案：
     - 使用消息队列（如 RabbitMQ、Kafka）
     - 实现事件驱动架构
     - 使用 WebSocket 推送实时更新

3. **如果需要添加教师服务（teacher-service），应该如何拆分？**
   - 拆分方案：
     - 创建独立的 teacher-service
     - 将 Instructor 从 Course 中拆分出来
     - catalog-service 调用 teacher-service 验证教师
     - 建立教师-课程关联关系

4. **如何避免选课时的并发问题（超选）？**
   - 当前实现：简单的数量检查
   - 改进方案：
     - 使用分布式锁（Redis）
     - 数据库乐观锁（版本号）
     - 数据库悲观锁（SELECT FOR UPDATE）
     - 使用消息队列实现顺序处理

## 更新日志

### v1.1.0 (2025-xx-xx)
- 集成 Nacos 服务注册与发现
- 添加 Spring Cloud Alibaba 依赖
- 使用 DiscoveryClient 实现动态服务发现
- 添加 Spring Boot Actuator 健康检查
- 添加 Nacos 测试脚本
- 服务间调用支持从 Nacos 获取服务地址

### v1.0.0 (2025-xx-xx)
- 初始版本
- 从单体应用拆分为微服务架构
- 实现服务间 HTTP 通信
- 使用 Docker Compose 部署
