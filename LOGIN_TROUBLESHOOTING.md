# 🔍 登录失败诊断和解决方案

## 🚨 常见登录失败原因

### 1️⃣ 账户不存在
**症状**：输入任何邮箱都无法登录
**原因**：初始管理员账户未创建，或数据库为空
**解决方案**：
```bash
# 检查后端日志
docker-compose logs backend
# 或本地运行查看
tail -f logs/hospital.log
```

**解决步骤**：
1. 确保 `.env` 中的配置正确：
```bash
INIT_ADMIN_ENABLED=true
INIT_ADMIN_EMAIL=admin@hospital.local
INIT_ADMIN_PASSWORD=Admin123!
```

2. 重启后端：
```bash
docker-compose restart backend
# 或
mvn spring-boot:run
```

3. 等待启动完成（看到 "Database initialization completed" 日志）

4. 尝试用以下默认账户登录：
```
邮箱: admin@hospital.local
密码: Admin123!
```

---

### 2️⃣ 密码错误
**症状**：账户存在但密码不对
**原因**：输入的密码与 `.env` 中配置不一致
**解决方案**：
1. 检查 `.env` 中的 `INIT_ADMIN_PASSWORD`
2. 确保密码区分大小写
3. 如果忘记密码，需要重新创建账户或修改数据库

---

### 3️⃣ CORS 错误
**症状**：浏览器控制台显示 CORS 错误
**原因**：前后端跨域配置不匹配
**解决方案**：
1. 检查 `.env` 中的 CORS 配置：
```bash
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:5174,http://localhost:80
```

2. 如果在 Docker 中运行，前端访问地址应该是 `http://localhost`
3. 如果本地运行，前端访问地址应该是 `http://localhost:5173`

---

### 4️⃣ 后端未运行或连接失败
**症状**：登录时无任何响应，长时间加载
**原因**：后端服务未启动或网络连接问题
**解决方案**：

检查后端是否运行：
```bash
# 检查端口是否监听
curl http://localhost:9090/api/health

# 如果成功，应该返回：
# {"status":"UP","timestamp":"2026-02-14T...", ...}
```

如果不成功，重启后端：
```bash
# Docker 方式
docker-compose restart backend
docker-compose logs -f backend

# 本地方式
mvn spring-boot:run
```

---

### 5️⃣ 数据库连接问题
**症状**：后端启动时报数据库错误，登录失败
**原因**：数据库未启动或连接参数错误
**解决方案**：

检查数据库：
```bash
# Docker 方式
docker-compose ps postgres
docker-compose logs postgres

# 本地方式，连接 PostgreSQL
psql -U postgres -d my_pg_db
```

如果数据库不存在，创建它：
```bash
psql -U postgres
CREATE DATABASE my_pg_db;
\q
```

---

## 🔧 完整诊断步骤

### 步骤 1：检查后端是否运行

```bash
# 测试后端健康
curl http://localhost:9090/api/health

# 如果返回 JSON，后端正常
# 如果连接拒绝，后端未运行
```

### 步骤 2：查看后端日志

```bash
# Docker 方式
docker-compose logs -f backend | grep -E "(ERROR|INFO.*login|initialization)"

# 本地方式
tail -f logs/hospital.log | grep -E "(ERROR|login)"
```

### 步骤 3：查看浏览器控制台错误

```bash
# 打开浏览器开发者工具 (F12)
# 切换到 Console 选项卡
# 查看红色错误信息，通常会显示：
# - CORS 错误 → 检查 CORS 配置
# - Network Error → 检查后端是否运行
# - 其他错误 → 复制错误信息查询
```

### 步骤 4：检查数据库

```bash
# 进入数据库
docker-compose exec postgres psql -U postgres -d my_pg_db
# 或本地
psql -U postgres -d my_pg_db

# 查看用户表
SELECT id, email, full_name FROM user_account LIMIT 10;

# 查看角色表
SELECT id, name FROM role LIMIT 10;

# 如果表为空，说明初始化失败
\q
```

---

## ✅ 快速解决方案

### 方案 A：Docker 部署方式

```bash
# 1. 修改 .env 文件
cd D:\hospital\hospital
# 编辑 .env，确保以下配置正确
# DB_PASSWORD=postgres
# INIT_ADMIN_ENABLED=true
# INIT_ADMIN_EMAIL=admin@hospital.local
# INIT_ADMIN_PASSWORD=Admin123!

# 2. 清除旧数据
docker-compose down -v

# 3. 重新启动
docker-compose up -d

# 4. 查看日志，等待初始化完成
docker-compose logs -f backend

# 5. 尝试登录
# 前端: http://localhost
# 邮箱: admin@hospital.local
# 密码: Admin123!
```

### 方案 B：本地运行方式

```bash
# 1. 确保 PostgreSQL 运行
# 可视化管理工具或命令行：
psql -U postgres

# 2. 创建数据库
CREATE DATABASE my_pg_db;

# 3. 编辑 src/main/resources/application.yml
# 确保数据库连接参数正确

# 4. 启动后端
mvn spring-boot:run

# 5. 查看日志，确认初始化成功
# 应该看到：[main] INFO ... DataInitializer: Database initialization completed

# 6. 启动前端（新窗口）
cd frontend
npm run dev

# 7. 尝试登录
# 前端: http://localhost:5173
# 邮箱: admin@hospital.local
# 密码: Admin123!
```

---

## 🐛 常见错误信息及解决

| 错误信息 | 原因 | 解决方案 |
|---------|------|---------|
| **Bad credentials** | 密码错误 | 检查密码，确认大小写 |
| **User not found** | 账户不存在 | 检查 INIT_ADMIN_EMAIL 配置 |
| **CORS error** | 跨域错误 | 修改 CORS_ALLOWED_ORIGINS |
| **Connection refused** | 后端未运行 | 启动后端服务 |
| **Cannot get a connection** | 数据库错误 | 检查数据库配置和连接 |
| **No qualifying bean** | 依赖注入失败 | 检查异常处理器配置 |

---

## 🎯 重置账户（如果忘记密码）

### 方法 1：通过数据库修改

```bash
# 进入数据库
docker-compose exec postgres psql -U postgres -d my_pg_db
# 或本地
psql -U postgres -d my_pg_db

# 查看当前用户
SELECT id, email FROM user_account;

# 删除旧账户（小心！）
DELETE FROM user_account WHERE email = 'admin@hospital.local';

# 退出
\q
```

然后重启后端，会自动重新创建：
```bash
docker-compose restart backend
```

### 方法 2：注册新账户

1. 在登录页面点击"注册"
2. 填写新邮箱和密码
3. 在数据库中将新用户的角色改为 ADMIN：

```bash
docker-compose exec postgres psql -U postgres -d my_pg_db

-- 查看角色ID（ADMIN 的 ID）
SELECT id, name FROM role WHERE name = 'ADMIN';

-- 假设 ADMIN 角色 ID 为 1，用户 ID 为 2
-- 执行以下 SQL（具体 ID 根据实际修改）
INSERT INTO user_account_roles (user_account_id, roles_id) VALUES (2, 1);

\q
```

---

## 📋 完整检查清单

启动前：
- [ ] .env 文件已修改正确的数据库密码
- [ ] INIT_ADMIN_PASSWORD 已设置
- [ ] PostgreSQL 已安装并运行

启动后：
- [ ] `curl http://localhost:9090/api/health` 返回正常
- [ ] 后端日志显示初始化完成
- [ ] 数据库中存在 user_account 表和数据
- [ ] 可以用默认账户登录

---

## 💬 获取更多帮助

如果以上步骤都无法解决，请：

1. 收集以下信息：
   - 后端日志（`docker-compose logs backend` 或 `logs/hospital.log`）
   - 浏览器控制台错误（F12 → Console）
   - `.env` 文件配置（隐去密码）
   - 数据库表内容（`SELECT * FROM user_account;`）

2. 检查文档：
   - `DOCKER_COMPLETE_GUIDE.md` - Docker 部署故障排查
   - `README.md` - 配置说明

3. 查看日志获取详细错误信息

