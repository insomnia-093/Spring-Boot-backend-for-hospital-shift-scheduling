# 🚀 快速开始指南

## 概述

本项目是一个**医院智能排班系统**，集成了 AI 智能体 (Coze) 进行智能排班建议。

- **后端**: Spring Boot 3.2.1 + PostgreSQL
- **前端**: Vue 3 + Vite
- **AI 智能体**: Coze 工作流

---

## ✅ 环境要求

### 必需
- **Java 17+** (推荐 Java 17 或 21)
- **Maven 3.8+**
- **Node.js 16+** 和 npm
- **PostgreSQL 12+**

### 可选
- Python 3.8+ (用于 Coze API 服务器)

---

## 📋 快速配置 (5 分钟)

### 1️⃣ 克隆和配置

```bash
cd D:\hospital\hospital

# 创建 .env 文件（如果不存在）
# 已有默认配置，可直接使用
```

### 2️⃣ 编译后端

```bash
mvn clean package -DskipTests
```

**预期**: ✅ `BUILD SUCCESS`

### 3️⃣ 启动服务

**方式 A: 分别启动（推荐）**

```bash
# 终端 1 - 后端
cd D:\hospital\hospital
java -jar target/hospital-0.0.1-SNAPSHOT.jar

# 终端 2 - 前端  
cd D:\hospital\hospital\frontend
npm install  # 第一次运行
npm run dev

# 终端 3 - Coze API (可选)
python coze_api_server.py
```

**方式 B: 自动启动脚本**

```bash
start-all-complete.bat  # Windows 批处理脚本
```

---

## 🌐 访问应用

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端应用 | http://localhost:5173 | Vue 前端页面 |
| 后端 API | http://localhost:9090 | REST API 端点 |
| 健康检查 | http://localhost:9090/api/health | 后端状态 |
| Coze API | http://localhost:8000 | AI 智能体 API |

---

## 🔐 默认账户

| 账号 | 密码 | 角色 |
|------|------|------|
| admin@hospital.local | Admin123! | 管理员 |

---

## 🔧 常见问题

### Q: 编译失败 "不支持发行版本 21"
**解决**：
```bash
# 检查 Java 版本
java -version

# 如果不是 17-21，请使用合适的版本
# 或在 pom.xml 中改为 <java.version>17</java.version>
```

### Q: 前端显示 CORS 错误
**解决**：
1. 确保后端已启动
2. 清除浏览器缓存 (Ctrl+Shift+Del)
3. 检查 `.env` 中的 CORS 配置
4. 查看修复说明: [CORS_HOTFIX.md](./CORS_HOTFIX.md)

### Q: 数据库连接失败
**解决**：
```bash
# 检查 PostgreSQL 是否运行
# 确认 .env 中的数据库配置：
DB_HOST=localhost
DB_PORT=5432
DB_NAME=my_pg_db
DB_USER=postgres
DB_PASSWORD=123456
```

### Q: 前端 npm 依赖错误
**解决**：
```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
npm run dev
```

---

## 📚 项目结构

```
hospital/
├── src/
│   └── main/
│       ├── java/org/example/hospital/    # 后端代码
│       │   ├── config/        # 配置类
│       │   ├── controller/     # API 端点
│       │   ├── service/        # 业务逻辑
│       │   ├── repository/     # 数据库访问
│       │   ├── security/       # 认证授权
│       │   └── domain/         # 数据模型
│       └── resources/          # 配置文件
├── frontend/                    # Vue 前端项目
│   ├── src/
│   │   ├── main.js            # 入口文件
│   │   ├── style.css          # 样式
│   │   └── components/        # Vue 组件
│   └── package.json
├── .env                        # 环境变量
├── pom.xml                     # Maven 配置
├── docker-compose.yml          # Docker 编排
└── README.md                   # 详细文档
```

---

## 🔑 主要功能

### ✅ 已实现
- [x] 用户注册和登录
- [x] JWT 认证
- [x] 基于角色的访问控制 (RBAC)
- [x] 科室管理
- [x] 排班管理
- [x] WebSocket 实时通讯
- [x] Coze AI 智能体集成
- [x] CORS 跨域支持

### 🚀 待实现
- [ ] 前端 UI 优化
- [ ] 智能排班算法
- [ ] 移动应用适配
- [ ] 部署到生产环境

---

## 📖 详细文档

- **[FIX_SUMMARY.md](./FIX_SUMMARY.md)** - CORS 修复说明
- **[CORS_HOTFIX.md](./CORS_HOTFIX.md)** - CORS 配置详解
- **[README.md](./README.md)** - 完整项目文档
- **[LOGIN_TROUBLESHOOTING.md](./LOGIN_TROUBLESHOOTING.md)** - 登录问题排查

---

## 🚢 部署

### Docker 部署 (推荐)

```bash
docker-compose up
```

这将启动：
- PostgreSQL 数据库
- Spring Boot 后端
- Nginx (可选前端)

### 手动部署到服务器

参考 [README.md - 部署章节](./README.md#部署)

---

## 🆘 获取帮助

如遇到问题，请按照以下步骤：

1. **查看日志**
   ```bash
   # 后端日志
   tail -f logs/hospital.log
   
   # 浏览器控制台
   F12 → Console 标签
   ```

2. **检查诊断工具**
   ```javascript
   // 在浏览器控制台运行
   window.hospital.diagnostics.checkBackend()
   window.hospital.diagnostics.checkCozeAPI()
   ```

3. **参考文档**
   - [CORS_HOTFIX.md](./CORS_HOTFIX.md) - CORS 错误
   - [LOGIN_TROUBLESHOOTING.md](./LOGIN_TROUBLESHOOTING.md) - 登录错误
   - [README.md](./README.md) - 完整文档

---

## 📞 技术栈总结

```
Frontend: Vue 3 + Vite + Axios
Backend: Spring Boot 3.2.1 + Spring Security + JPA
Database: PostgreSQL 12+
Message Queue: STOMP/WebSocket
Cache: 无 (可选集成 Redis)
API Auth: JWT (JSON Web Token)
AI Service: Coze Workflow API
Deployment: Docker + Docker Compose
```

---

## ⚡ 快速命令参考

```bash
# 编译
mvn clean package -DskipTests

# 运行后端
java -jar target/hospital-0.0.1-SNAPSHOT.jar

# 运行前端
cd frontend && npm run dev

# 构建前端
cd frontend && npm run build

# 运行 Coze API
python coze_api_server.py

# Docker 部署
docker-compose up

# 停止所有容器
docker-compose down

# 查看日志
docker-compose logs -f

# 重建镜像
docker-compose up --build
```

---

**祝您使用愉快！** 🎉

如有问题，请参考各文档或提出 Issue。

