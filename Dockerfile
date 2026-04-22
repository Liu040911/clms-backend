# 使用多阶段构建
# 第一阶段：构建应用
FROM maven:3.9.11-eclipse-temurin-21 AS builder

# 指定维护者信息
LABEL maintainer="1606294640@qq.com"

# 将工作目录设置为 /build
WORKDIR /build

# 先复制 Maven 配置，利用 Docker 缓存优化依赖下载
COPY pom.xml ./

# 复制源码
COPY src ./src

# 构建项目（跳过测试以加快构建速度）
RUN mvn -B clean package -DskipTests

# 第二阶段：运行时镜像
FROM eclipse-temurin:21-jre

# 安装健康检查所需的 curl
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 创建非 root 用户
RUN groupadd -r clms && useradd --no-log-init -r -g clms clms

# 将工作目录设置为 /app
WORKDIR /app

# 从构建阶段复制 JAR 文件
COPY --from=builder /build/target/*.jar app.jar

# 更改文件所有者
RUN chown -R clms:clms /app

# 切换到非 root 用户
USER clms

# 暴露 8080 端口
EXPOSE 8080

# 设置 JVM 参数以适应容器环境
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC -XX:+UseContainerSupport"

# 运行 jar 文件（环境变量在运行时传入）
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]