# 🆘 问题排查指南 (故障排除)

## 快速导航

点击跳转到您遇到的问题：

1. [CORS 错误](#cors-错误)
2. [后端无法启动](#后端无法启动)
3. [前端无法访问](#前端无法访问)
4. [登录失败](#登录失败)
5. [数据库连接错误](#数据库连接错误)
6. [编译错误](#编译错误)

---

## CORS 错误

### ❌ 错误现象

```
Access to fetch at 'http://localhost:9090/api/auth/login' from origin 
'http://localhost:5174' has been blocked by CORS policy: Response to 
preflight request doesn't pass access control check: No 
'Access-Control-Allow-Origin' header is present on the requested resource.
```

或

```
智能体呼吁失败: When allowCredentials is true, 
allowedOrigins cannot contain the special value "*"
```

### 🔍 原因分析

| 原因 | 症状 | 解决方案 |
|------|------|--------|
| CORS 配置错误 | 浏览器提示跨域错误 | 查看 WHY_CORS_ERROR.md |
| 通配符 "*" | "allowedOrigins cannot contain *" | 改为明确列表 |
| 后端未启动 | 连接被拒绝 | `java -jar target/hospital-0.0.1-SNAPSHOT.jar` |
| 端口号错误 | 访问错误的后端地址 | 检查 localhost:9090 |

### ✅ 解决步骤

**步骤 1: 验证后端启动**

```bash
# 检查 9090 端口
netstat -ano | findstr "9090"

# 如果没有输出，表示后端未启动
java -jar target\hospital-0.0.1-SNAPSHOT.jar
```

**步骤 2: 清除浏览器缓存**

```
按键: Ctrl + Shift + Delete
清除: 所有时间 → 继续清除
```

**步骤 3: 检查 CORS 配置**

打开浏览器 F12 → Network 标签，任意请求右键 → Response Headers

**正确的响应头**：
```
Access-Control-Allow-Origin: http://localhost:5173
Access-Control-Allow-Credentials: true
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
```

**错误的响应头**：
```
Access-Control-Allow-Origin: *
Access-Control-Allow-Credentials: true  ← ❌ 矛盾
```

**步骤 4: 重新编译和启动**

```bash
mvn clean package -DskipTests
java -jar target\hospital-0.0.1-SNAPSHOT.jar
```

---

## 后端无法启动

### ❌ 错误现象

```
Port 9090 already in use
```

或

```
Exception in thread "main" java.lang.UnsupportedClassVersionError
```

或

```
ClassFormatException: ASM ClassReader failed to parse class file
```

### 🔍 原因分析

| 错误信息 | 原因 | 解决方案 |
|---------|------|--------|
| Port 9090 already in use | 端口被占用 | 停止占用进程或改端口 |
| UnsupportedClassVersionError | Java 版本不匹配 | 检查 Java 版本 |
| ClassFormatException | 编译器版本问题 | 重新编译 |
| Connection refused | 数据库未启动 | 启动 PostgreSQL |

### ✅ 解决步骤

**情况 1: Port 9090 already in use**

```bash
# Windows: 查找占用进程
netstat -ano | findstr "9090"

# 停止进程 (替换 PID)
taskkill /PID 1234 /F

# 或停止所有 Java 进程
taskkill /F /IM java.exe

# 然后重新启动
java -jar target\hospital-0.0.1-SNAPSHOT.jar
```

**情况 2: UnsupportedClassVersionError**

```bash
# 检查 Java 版本
java -version

# 应该是 Java 17 或更高
# 如果不是，在 pom.xml 中改为:
# <java.version>17</java.version>

# 重新编译
mvn clean package -DskipTests
```

**情况 3: ClassFormatException**

```bash
# 删除旧的编译文件
mvn clean

# 重新编译
mvn package -DskipTests

# 启动
java -jar target\hospital-0.0.1-SNAPSHOT.jar
```

**情况 4: Connection refused (数据库)**

```bash
# 检查 PostgreSQL 是否运行
# Windows: 检查服务
services.msc

# 确保 PostgreSQL 服务已启动
# 默认端口: 5432
```

---

## 前端无法访问

### ❌ 错误现象

```
Cannot get /
Cannot GET /index.html
```

或访问 http://localhost:5173 显示白屏

### 🔍 原因分析

| 原因 | 症状 | 解决方案 |
|------|------|--------|
| 前端未启动 | 无法连接 | `npm run dev` |
| npm 依赖缺失 | 模块加载失败 | `npm install` |
| 端口被占用 | Port 5173 in use | 改端口或停止进程 |
| Vite 配置错误 | 白屏或 404 | 检查 vite.config.js |

### ✅ 解决步骤

**步骤 1: 启动前端**

```bash
cd frontend
npm install  # 如果是第一次
npm run dev
```

**预期输出**：
```
  VITE v5.0.0  ready in 123 ms

  ➜  Local:   http://localhost:5173/
  ➜  press h to show help
```

**步骤 2: 如果 npm install 失败**

```bash
# 清除缓存
npm cache clean --force

# 删除旧文件
rm -rf node_modules package-lock.json

# 重新安装
npm install
```

**步骤 3: 如果端口 5173 被占用**

```bash
# 方式 1: 使用不同的端口
npm run dev -- --port 5174

# 方式 2: 停止占用进程
netstat -ano | findstr "5173"
taskkill /PID <PID> /F
```

**步骤 4: 检查浏览器控制台**

按 F12 打开开发者工具，查看：
- Console: 是否有红色错误
- Network: 是否有 404 错误
- Sources: 是否能加载 main.js

---

## 登录失败

### ❌ 错误现象

在登录页面输入账密后：

```
401 Unauthorized
Bad credentials
User not found
```

或提交后没有反应

### 🔍 原因分析

| 原因 | 症状 | 解决方案 |
|------|------|--------|
| 账号/密码错误 | 401 Unauthorized | 确认默认账号信息 |
| 后端未启动 | Connection refused | 启动后端 |
| CORS 错误 | 跨域被阻止 | 检查 CORS 配置 |
| 数据库未初始化 | 500 Internal Error | 检查数据库连接 |

### ✅ 解决步骤

**步骤 1: 确认默认账号**

```
Email:    admin@hospital.local
Password: Admin123!
```

**步骤 2: 检查后端是否启动**

```bash
# 健康检查
curl http://localhost:9090/api/health

# 或在浏览器访问
http://localhost:9090/api/health

# 应该返回 {"status":"UP"}
```

**步骤 3: 检查浏览器控制台**

按 F12，看 Network 标签：

```
请求 URL: http://localhost:9090/api/auth/login
方法: POST
状态: 200 (成功) 或 401 (失败)
```

**步骤 4: 手动测试登录**

在浏览器控制台执行：

```javascript
fetch('http://localhost:9090/api/auth/login', {
  method: 'POST',
  headers: {'Content-Type': 'application/json'},
  body: JSON.stringify({
    email: 'admin@hospital.local',
    password: 'Admin123!'
  })
})
.then(r => r.json())
.then(d => console.log(d))
.catch(e => console.error(e.message))
```

**预期响应**：
```json
{
  "accessToken": "eyJhbGc...",
  "user": {
    "id": 1,
    "email": "admin@hospital.local",
    "fullName": "System Admin"
  }
}
```

**错误响应**：
```json
{"error": "Bad credentials"}
```

---

## 数据库连接错误

### ❌ 错误现象

```
Could not get a connection, pool error Timeout waiting for idle object
```

或

```
Connection to localhost:5432 refused
```

### 🔍 原因分析

| 原因 | 症状 | 解决方案 |
|------|------|--------|
| PostgreSQL 未启动 | 连接被拒绝 | 启动 PostgreSQL |
| 连接信息错误 | Timeout 或 refused | 检查 .env 配置 |
| 数据库不存在 | 404 或错误 | 创建数据库 |
| 密码错误 | 认证失败 | 确认密码 |

### ✅ 解决步骤

**步骤 1: 检查 PostgreSQL 运行状态**

```bash
# Windows 检查服务
services.msc

# 或检查端口
netstat -ano | findstr "5432"

# 如果没有输出，表示 PostgreSQL 未运行
```

**步骤 2: 检查 .env 数据库配置**

```bash
# 打开 .env 文件，检查：
DB_HOST=localhost        # 主机
DB_PORT=5432            # 端口
DB_NAME=my_pg_db        # 数据库名
DB_USER=postgres        # 用户
DB_PASSWORD=123456      # 密码
```

**步骤 3: 手动测试连接**

```bash
# Windows 用户可用 pgAdmin 测试
# 或使用 psql 命令行:

psql -h localhost -U postgres -d my_pg_db

# 提示输入密码，输入 123456
# 如果连接成功，会显示 psql prompt
```

**步骤 4: 创建数据库（如果不存在）**

```bash
# 使用 psql 连接
psql -U postgres

# 创建数据库
CREATE DATABASE my_pg_db;

# 退出
\q
```

**步骤 5: 重启后端**

```bash
# 先停止
taskkill /F /IM java.exe

# 重新启动
java -jar target\hospital-0.0.1-SNAPSHOT.jar
```

---

## 编译错误

### ❌ 错误现象

```
ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.11.0:compile
```

或

```
不支持发行版本 21
```

### 🔍 原因分析

| 错误 | 原因 | 解决方案 |
|------|------|--------|
| "不支持发行版本 21" | Java 版本过高 | 使用 Java 17-21 |
| "找不到符号" | 依赖缺失 | `mvn clean install` |
| "编译失败" | 代码错误 | 检查日志 |

### ✅ 解决步骤

**步骤 1: 检查 Java 版本**

```bash
java -version

# 应该显示 Java 17 或更高
# 如果不对，下载安装正确版本
```

**步骤 2: 更新 pom.xml**

```xml
<!-- 改为你的 Java 版本，比如 17 -->
<properties>
    <java.version>17</java.version>
</properties>
```

**步骤 3: 清除并重新编译**

```bash
# 完全清除
mvn clean

# 重新编译
mvn package -DskipTests

# 预期: BUILD SUCCESS
```

---

## 🚀 快速修复流程

如果遇到多个问题，按以下顺序排查：

```
1. 停止所有 Java 进程
   taskkill /F /IM java.exe

2. 清除编译文件
   mvn clean

3. 重新编译
   mvn package -DskipTests

4. 检查数据库
   确保 PostgreSQL 运行，数据库存在

5. 启动后端
   java -jar target\hospital-0.0.1-SNAPSHOT.jar

6. 检查前端依赖
   cd frontend && npm install

7. 启动前端
   npm run dev

8. 打开浏览器
   http://localhost:5173

9. 使用默认账号登录
   Email: admin@hospital.local
   Password: Admin123!
```

---

## 🔧 有用的诊断命令

```bash
# 检查 Java
java -version

# 检查 Maven
mvn -version

# 检查 Node.js
node -v
npm -v

# 检查端口占用
netstat -ano | findstr "9090"   # 后端
netstat -ano | findstr "5173"   # 前端
netstat -ano | findstr "5432"   # 数据库

# 停止进程
taskkill /PID <PID> /F

# 停止所有 Java
taskkill /F /IM java.exe

# 查看实时日志
powershell -Command "Get-Content logs\hospital.log -Tail 100 -Wait"
```

---

## 📞 获取更多帮助

- **WHY_CORS_ERROR.md** - CORS 错误详解
- **CORS_HOTFIX.md** - CORS 配置方案
- **QUICK_START_CN.md** - 快速开始
- **README.md** - 完整文档

---

## 💡 最后的技巧

### 重启大法

如果不知道什么原因导致的问题，试试：

```bash
# 1. 关闭所有窗口
taskkill /F /IM java.exe

# 2. 清除编译
mvn clean

# 3. 重新编译
mvn package -DskipTests

# 4. 启动
java -jar target\hospital-0.0.1-SNAPSHOT.jar
```

### 查看完整日志

后端启动时会打印详细日志，遇到错误时：
1. 复制完整的错误信息
2. 查看本文档的相关章节
3. 查看 WHY_CORS_ERROR.md

---

**仍有问题？** 请确保已查看：
1. 本文档的所有章节
2. WHY_CORS_ERROR.md
3. README.md 中的完整指南

祝您使用顺利！ 🎉

