# 构建阶段
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build

# 先复制 pom.xml 并下载依赖（利用 Docker 层缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 再复制源代码并构建
COPY src ./src
RUN mvn clean package -DskipTests -B

# 运行阶段
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 创建非 root 用户
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# 复制构建产物
COPY --from=builder /build/target/enrollment-service.jar app.jar

# 配置 JVM 参数
ENV JAVA_OPTS="-Xmx512m -Xms256m"

EXPOSE 8082

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
