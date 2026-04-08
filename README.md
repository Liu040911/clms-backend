# clms-backend

校园讲座管理系统后端服务，基于 Spring Boot 3 构建，提供用户认证、权限管理、讲座管理、教室管理、报名管理与动态配置等能力。

## 项目简介

`clms-backend` 是 CLMS（Campus Lecture Management System）的核心后端项目，主要职责如下：

- 提供统一 REST API，供小程序/H5 前端调用
- 负责用户登录鉴权（Sa-Token）与权限控制
- 管理讲座全生命周期（创建、修改、删除、状态流转）
- 管理教室资源并处理可用性校验
- 提供动态页面配置（如 `my` 页面菜单）

## 技术栈

- Java 21
- Spring Boot 3.4.0
- MyBatis-Plus 3.5.9
- MySQL 8+
- Redis
- RabbitMQ
- Sa-Token 1.39.0
- SpringDoc OpenAPI + Knife4j
- Hutool

## 核心目录

```text
src/main/java/com/clms/
├── config/          # 配置类（MyBatis、Redis、Swagger、Sa-Token等）
├── controller/      # 接口层（admin/user/lecture/class等）
├── entity/          # 数据对象（po/dto/bo/base）
├── enums/           # 枚举定义（返回码、讲座状态等）
├── exception/       # 异常与全局错误处理
├── mapper/          # MyBatis Mapper
├── service/         # 业务接口与实现（impl/data）
└── utils/           # 通用工具

src/main/resources/
├── application.yaml          # 通用配置
├── application-dev.yaml      # 开发环境配置
├── application-test.yaml     # 测试环境配置
├── mapper/                   # 自定义 Mapper XML
└── sql/
  ├── database_init.sql      # 初始化脚本
  └── update/                # 增量脚本
```

## 分层说明

- Controller：参数接收、鉴权注解、结果返回
- Service：业务编排与规则校验
- Data Service（`service/data`）：基于 MyBatis-Plus 的数据访问封装
- PO/DTO/BO：
  - PO：数据库映射对象
  - DTO：入参对象
  - BO：出参对象

## 运行环境要求

- JDK 21
- Maven 3.9+
- MySQL 8+
- Redis 6+
- RabbitMQ 3+

## 快速启动

### 1. 初始化数据库

执行：

- `src/main/resources/sql/database_init.sql`
- 如需增量能力，再执行 `src/main/resources/sql/update/` 下对应脚本

### 2. 配置环境

推荐通过 `.env` 或环境变量注入敏感信息，避免明文写入配置文件。

可参考：

- `.env.example`
- `application.yaml`
- `application-dev.yaml`

### 3. 启动依赖服务（可选）

项目提供了 Docker Compose 文件：

- `deploy/docker-compose-dev.yaml`
- `deploy/docker-compose-test.yml`

### 4. 启动项目

```bash
mvn clean compile
mvn spring-boot:run
```

或先打包再运行：

```bash
mvn clean package -DskipTests
java -jar target/clms-backend-0.0.1-SNAPSHOT.jar
```

## 常用命令

```bash
# 编译
mvn clean compile

# 运行测试
mvn test

# 打包
mvn clean package
```

## API 文档

启动后可访问：

- Swagger UI：`http://localhost:8080/swagger-ui.html`
- OpenAPI JSON：`http://localhost:8080/v3/api-docs`

## 主要业务模块

- 用户与认证：登录、注册、用户信息、头像更新
- 权限系统：角色、权限、角色权限绑定、管理员能力
- 讲座管理：讲座 CRUD、讲座详情、讲座列表
- 教室管理：教室 CRUD、可用教室查询
- 报名管理：用户报名与状态追踪（pending/cancelled/checked_in/not_signed_in）
- 应用配置：动态页面配置读取

## 开发约定

- 主键使用 UUID v7（32位无连字符）
- 统一响应使用 `ResponseEntity<T>`
- 统一业务异常使用 `BusinessException`
- 数据库逻辑删除字段为 `deleted`（MyBatis-Plus 全局配置）
- 接口权限通过 Sa-Token 注解控制（如 `@SaCheckLogin`）

## 注意事项

- 当前仓库中的 `application*.yaml` 可能包含示例连接信息，部署前请替换为安全配置。
- 如果新增表结构，请同步更新：
  - `database_init.sql`
  - 对应增量脚本（`sql/update`）
  - PO/DTO/BO、Mapper、Service、Controller

## CI/CD

GitHub Actions 工作流位于：

- `.github/workflows/master-cicd.yml`

可按团队策略扩展构建、测试、镜像发布与部署流程。
