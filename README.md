# Hospital Scheduling Agent

## 项目概览
医院排班系统后端（Spring Boot 3 + PostgreSQL）与独立 Vue 3 前端。支持登录注册、权限控制、排班管理、智能体任务，并提供 WebSocket 实时通知与聊天记录。

## 功能清单
- 账号体系: 登录、注册、JWT 鉴权
- 权限控制: 角色分级（ADMIN/COORDINATOR/DOCTOR/NURSE/AGENT）
- 排班管理: 班次创建、指派、查询、删除
- 智能体任务: 任务创建与状态跟踪
- 实时通信: WebSocket 通知与智能体聊天
- 聊天留存: 智能体沟通记录持久化
- 管理后台: 用户密码重置、班次详情修改

## 技术栈
| 层级 | 技术 | 备注 |
| --- | --- | --- |
| 语言/运行时 | Java 17, JavaScript (ES2020+), Node.js 18+ | 后端/前端运行环境 |
| 后端框架 | Spring Boot 3.2.1, Spring Web | REST API |
| 安全 | Spring Security, JWT | 无状态鉴权 |
| 数据访问 | Spring Data JPA, Hibernate | ORM 持久化 |
| 实时通信 | Spring WebSocket, STOMP, SockJS | WebSocket 与消息订阅 |
| 数据库 | PostgreSQL 14+ | 关系型数据库 |
| 前端框架 | Vue 3, Vite | SPA + 构建 |
| 构建工具 | Maven, npm | 后端/前端构建 |
| 序列化 | Jackson | JSON 序列化 |

## 语言规范
- 说明文档与 UI 文案使用中文，技术名词保留英文原文。
- API 命名使用小写字母与短横线（kebab-case），如 `/api/agent/chat`。
- Java 类与方法命名遵循驼峰（PascalCase / camelCase）。
- 数据库表与字段使用小写下划线（snake_case）。

## 运行前准备
1. 安装 JDK 17+（确认 `java -version` 为 17 或更高）。
2. 安装 PostgreSQL 并启动服务。
3. 创建数据库（默认名 `my_pg_db`）。
4. 确认后端配置文件 `src/main/resources/application.yml` 中数据库账号密码。

## 配置说明
后端默认配置（可用环境变量覆盖）：
- `DB_HOST` 默认 `localhost`
- `DB_PORT` 默认 `5432`
- `DB_NAME` 默认 `my_pg_db`
- `DB_USER` 默认 `postgres`
- `DB_PASSWORD` 默认 `123456`
- `JWT_SECRET` 默认 `change-me-to-a-256-bit-secret-string`
- `JWT_EXPIRATION_MINUTES` 默认 `60`
- `server.port` 默认 `9090`

初始化管理员账号（启动时自动创建）：
- `INIT_ADMIN_ENABLED` 默认 `true`
- `INIT_ADMIN_EMAIL` 默认 `admin@hospital.local`
- `INIT_ADMIN_PASSWORD` 默认 `Admin123!`
- `INIT_ADMIN_FULL_NAME` 默认 `System Admin`

前端可用环境变量配置：
- `VITE_API_BASE` 默认 `http://localhost:9090/api`
- `VITE_WS_BASE` 默认 `http://localhost:9090/ws`

## 本地运行
### 启动后端
```powershell
mvn spring-boot:run
```
后端默认地址: `http://localhost:9090`

### 启动前端
```powershell
cd frontend
npm install
npm run dev
```
前端默认地址: `http://localhost:5173`

如需调整前端连接后端地址：
```powershell
$env:VITE_API_BASE="http://localhost:9090/api"
$env:VITE_WS_BASE="http://localhost:9090/ws"
```

## 管理员功能
- 默认管理员可使用 `INIT_ADMIN_EMAIL` / `INIT_ADMIN_PASSWORD` 登录。
- 管理员界面位置：登录后进入“个人中心”。
- 功能包含：
  - 重置指定用户密码
  - 修改班次详情（时间、角色、科室、指派、状态、备注）

## WebSocket 实时功能
- 连接地址: `http://localhost:9090/ws`
- 订阅频道:
  - `/topic/agent-chat` 智能体聊天室
  - `/topic/shifts` 排班更新事件
  - `/topic/agent-tasks` 任务更新事件
  - `/topic/notifications` 通用通知
- 发送地址:
  - `/app/agent-chat` 发送聊天消息

## API 概览
- Auth: `/api/auth/login`, `/api/auth/register`
- Dept: `/api/departments`
- Shift: `/api/shifts`
- Agent: `/api/agent/tasks`
- Admin: `/api/admin/users`, `/api/admin/users/{userId}/password`, `/api/admin/shifts/{shiftId}`
- Chat: `/api/agent/chat?limit=50`

## 常见问题
- 端口冲突: 修改 `src/main/resources/application.yml` 的 `server.port`。
- 数据库连接失败: 检查 `application.yml` 中数据库连接信息。
- 编译报错: 若提示版本不兼容，请确认 IDE 的 Java 版本为 17。

## 生产环境部署
### 后端部署
1. 打包后端:
```powershell
mvn -DskipTests package
```
2. 启动（建议使用环境变量配置数据库与密钥）:
```powershell
$env:DB_HOST="<db-host>"
$env:DB_PORT="5432"
$env:DB_NAME="hospital"
$env:DB_USER="<db-user>"
$env:DB_PASSWORD="<db-password>"
$env:JWT_SECRET="<256-bit-secret>"
$env:JWT_EXPIRATION_MINUTES="120"
$env:SERVER_PORT="9090"
$env:INIT_ADMIN_EMAIL="admin@hospital.local"
$env:INIT_ADMIN_PASSWORD="<change-me>"
java -jar target\hospital-0.0.1-SNAPSHOT.jar
```

### 前端部署
1. 打包前端:
```powershell
cd frontend
npm install
npm run build
```
2. 将 `frontend/dist` 部署到静态服务器（如 Nginx 或 IIS）。

### 反向代理（可选）
如需统一域名，可将 `/api` 与 `/ws` 代理到后端端口，并将根路径指向前端静态资源。

### 生产建议
- 请修改默认数据库账号密码与 `JWT_SECRET`。
- 建议在生产环境关闭 `ddl-auto: update`，改用迁移工具管理表结构。
- 确保防火墙放行 `server.port` 端口（默认 9090）。
