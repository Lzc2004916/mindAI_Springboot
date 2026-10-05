# ============================================================
# 多阶段构建：构建阶段用 Maven，运行阶段用 JRE
# ============================================================
# ---- 构建阶段 ----
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml .
# 先下载依赖（利用 Docker 层缓存）
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

# ---- 运行阶段 ----
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# 安装 curl 用于健康检查
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# 创建非 root 用户运行
RUN groupadd -r appuser && useradd -r -g appuser appuser

# 复制 jar
COPY --from=builder /build/target/*.jar app.jar

# 创建上传目录
RUN mkdir -p /app/files /app/files_private /app/logs \
    && chown -R appuser:appuser /app

USER appuser

EXPOSE 1236

# JVM 参数：容器内自动感知内存限制
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# 健康检查：每 30s 检查一次，启动后 60s 开始
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:1236/actuator/health/readiness || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.profiles.active=prod"]
