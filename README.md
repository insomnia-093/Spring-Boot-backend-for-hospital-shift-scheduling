# Hospital Scheduling Backend

## Overview

基于 Spring Boot 构建的医院排班管理后端服务，集成 JWT 身份认证、PostgreSQL 持久化存储，且提供医院代理任务相关 API 接口

## Prerequisites

- Java 17+
- Maven 3.9+
- PostgreSQL 14+

## Configuration

调整环境变量，或更新配置文件 `src/main/resources/application.yml`

```
DB_HOST=localhost
DB_PORT=5432
DB_NAME=my_pg_base
DB_USER=postgres
DB_PASSWORD=123456
JWT_SECRET=change-me-to-a-256-bit-secret-string
JWT_EXPIRATION_MINUTES=60
```

## Running

```
mvn spring-boot:run
```

默认管理员登录凭据（首次登录后请修改）：

- email: admin@hospital.local
- password: Admin123!

## API Highlights

- `POST /api/auth/register` – 注册带角色权限的新用户
- `POST /api/auth/login` – 获取 JWT 令牌
- `GET /api/departments` – 查询医院科室列表
- `POST /api/shifts` – 创建排班（仅管理员 / 排班协调员可操作）
- `PUT /api/shifts/{id}` – 为指定排班分配工作人员（路径参数`id`为排班 ID）
- `POST /api/agent/tasks` – 将代理任务加入任务队列
- `GET /api/agent/tasks/pending` – 代理拉取待处理任务

所有受保护的请求，均需在请求头中添加`Authorization: Bearer <token>`字段。
