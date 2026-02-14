# 🏥 医院排班系统（Hospital Scheduling Agent）

<div align="center">

**一个基于 Spring Boot + Vue 3 的智能医院排班管理系统**

![Java 17](https://img.shields.io/badge/Java-17+-green?style=flat)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-green?style=flat)
![Vue 3](https://img.shields.io/badge/Vue-3-brightgreen?style=flat)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14+-blue?style=flat)

[快速开始](#快速开始) • [功能特性](#功能特性) • [技术栈](#技术栈) • [Docker 部署](#docker-部署)

</div>

---

## 📋 项目简介

**医院排班系统** 是一个为医院管理部门设计的智能排班管理平台，支持班次管理、员工调度、实时沟通和智能体协作。系统采用前后端分离架构，提供完整的权限控制、数据持久化和实时通信能力。

### ✨ 核心特性

- ✅ **完整的账号管理** - 支持登录、注册、JWT 无状态认证
- ✅ **灵活的权限控制** - 5 级角色系统（ADMIN/COORDINATOR/DOCTOR/NURSE/AGENT）
- ✅ **智能排班系统** - 班次创建、员工指派、状态跟踪、冲突检测
- ✅ **实时通信** - 基于 WebSocket 的多频道消息系统
- ✅ **智能体协作** - 任务创建、状态跟踪、聊天历史持久化
- ✅ **管理后台** - 用户密码重置、班次详情修改、数据统计
- ✅ **生产就绪** - 完整的异常处理、日志记录、监控告警
- ✅ **Docker 部署** - 一键启动、多服务容器编排、云部署支持

---

## 🚀 快速开始

### 前置条件

| 要求 | 版本 | 说明 |
|------|------|------|
| **JDK** | 17+ | 后端运行时 |
| **PostgreSQL** | 14+ | 数据库 |
| **Node.js** | 18+ | 前端构建 |
| **npm** | 8+ | 包管理器 |

### 方式一：Docker 快速启动（推荐 ⭐）

```bash
cd D:\hospital\hospital
deploy.bat              # Windows
# 或
./deploy.sh            # Linux/Mac
```

启动完成后访问：
- **前端**：http://localhost
- **后端 API**：http://localhost:9090/api
- **健康检查**：http://localhost:9090/api/health

### 方式二：本地开发环境

1.直接运行start-dev

2.命令行

```bash
# 启动后端
mvn spring-boot:run
# 访问 http://localhost:9090

# 启动前端（新窗口）
cd frontend
npm install
npm run dev
# 访问 http://localhost:5173
```

---

## 📚 功能说明

### 账号与权限
- 邮箱注册、JWT 登录
- 5 级权限控制
- 管理员密码重置

### 排班管理
- 班次创建、指派、删除
- 状态管理（OPEN → ASSIGNED → COMPLETED）
- 冲突检测、数据统计

### 实时通信
- WebSocket 多频道：`/topic/shifts`、`/topic/agent-chat` 等
- 聊天历史持久化
- 实时任务推送

### 智能体协作
- 任务创建与跟踪
- 实时聊天
- 多端同步

---

## 🛠 技术栈

```
后端: Spring Boot 3.2.1 + PostgreSQL 14+
前端: Vue 3 + Vite + Fetch + WebSocket
容器: Docker + Docker Compose
监控: Prometheus + Grafana（可选）
```

---

## ⚙️ 配置

### 环境变量 (.env)

```bash
# 数据库
DB_HOST=postgres
DB_PASSWORD=postgres               # ⚠️ 生产必改

# 认证
JWT_SECRET=change-me-...           # ⚠️ 生产必改
JWT_EXPIRATION_MINUTES=60

# 初始管理员
INIT_ADMIN_EMAIL=admin@hospital.local
INIT_ADMIN_PASSWORD=Admin123!      # ⚠️ 生产必改

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:80
WS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:80

# 端口
BACKEND_PORT=9090
FRONTEND_PORT=80
```

### 默认账号

| 用户 | 邮箱 | 密码 |
|------|------|------|
| 管理员 | admin@hospital.local | Admin123! |
| 数据库 | postgres | postgres |

⚠️ **生产环境务必修改所有密码！**

---

## 🐳 Docker 部署

```bash
# 快速启动
deploy.bat              # Windows
./deploy.sh            # Linux/Mac
docker-compose up -d   # 手动

# 查看日志
docker-compose logs -f backend

# 常用服务地址
前端:     http://localhost
后端:     http://localhost:9090
健康检查: http://localhost:9090/api/health
监控:     http://localhost:9090/actuator/prometheus
```

详见 `DOCKER_COMPLETE_GUIDE.md`

---

## 📡 API 概览

```
认证:    POST /api/auth/login, /api/auth/register
排班:    GET|POST|PUT|DELETE /api/shifts
科室:    GET|POST /api/departments
智能体:  GET|POST /api/agent/tasks, GET /api/agent/chat
管理:    GET /api/admin/users, PUT /api/admin/users/{userId}/password
```

完整文档：启动后访问 `http://localhost:9090/swagger-ui.html`

---

## 🔐 安全建议

✅ JWT 无状态认证
✅ Spring Security 权限控制
✅ BCrypt 密码加密
✅ CORS 配置
✅ SQL 注入防护

生产环境步骤：
```bash
# 1. 修改所有密码（.env）
# 2. 生成 JWT 密钥：openssl rand -base64 32
# 3. 启用 HTTPS（在 Nginx 配置 SSL）
# 4. 定期备份数据库
```

---

## 🔧 故障排查

| 问题 | 解决方案 |
|------|---------|
| 端口被占用 | 修改 `.env` 中的端口号 |
| 数据库连接失败 | 检查 PostgreSQL 运行状态和连接参数 |
| 前端无法访问 API | 检查 CORS 配置，确保后端在 9090 运行 |
| Docker 启动失败 | 运行 `docker-compose logs` 查看错误 |
| Java 版本不兼容 | 确认 Java 17+（`java -version`） |

查看日志：
```bash
docker-compose logs -f backend
tail -f logs/hospital.log
```

---

## 📈 项目改进

✨ **11 项核心改进**
- 全局异常处理、结构化日志、数据库优化、监控告警等

📚 **完整 Docker 支持**
- 一键启动、生产配置、云部署

详见：
- `IMPROVEMENT_SUMMARY.md` - 改进总结
- `DOCKER_COMPLETE_GUIDE.md` - Docker 指南

---

## 💡 项目结构

```
hospital/
├── src/main/java/org/example/hospital/
│   ├── config/          配置
│   ├── controller/      REST API
│   ├── domain/          数据模型
│   ├── dto/             数据传输对象
│   ├── repository/      数据访问
│   ├── security/        安全配置
│   └── service/         业务逻辑
├── frontend/            Vue 应用
├── Dockerfile           后端镜像
├── docker-compose.yml   容器编排
├── .env                 环境配置
└── README.md            本文件
```

---

## 📄 许可证

MIT License

---

<div align="center">

**⭐ 如果这个项目对你有帮助，请给个 Star！**

Made with ❤️ for Hospital Management

</div>
