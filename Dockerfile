# 后端 Dockerfile - 多阶段构建

# 第一阶段：构建
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /build

# 复制 Maven 配置
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# 下载依赖（可选，加快构建）
RUN chmod +x mvnw && ./mvnw dependency:go-offline -DskipTests || true

# 复制源代码
COPY src src

# 构建项目
RUN ./mvnw clean package -DskipTests

# 第二阶段：运行
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 复制构建好的 JAR 文件
COPY --from=builder /build/target/hospital-*.jar app.jar

# 创建日志目录
RUN mkdir -p /app/logs

# 暴露端口
EXPOSE 9090

# 健康检查
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:9090/api/health || exit 1

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]
