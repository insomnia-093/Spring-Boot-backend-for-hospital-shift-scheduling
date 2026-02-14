@echo off
REM ============================================
REM   医院排班系统 - 一键启动脚本
REM   Hospital Scheduling System - Start Script
REM ============================================

setlocal enabledelayedexpansion
chcp 65001 >nul 2>&1
color 0A

cls
echo.
echo ============================================
echo    医院智能排班系统
echo    Hospital Scheduling System
echo ============================================
echo.
echo 正在启动系统...
echo.

REM 检查必要文件
echo [检查] 验证项目文件...
if not exist "pom.xml" (
    color 0C
    echo ❌ 错误: 未找到 pom.xml，请在项目根目录运行此脚本
    pause
    exit /b 1
)
echo ✅ 项目文件验证通过

REM 检查 Java
echo [检查] 验证 Java 环境...
java -version >nul 2>&1
if errorlevel 1 (
    color 0C
    echo ❌ 错误: Java 未安装或未在 PATH 中
    echo 请从 https://www.oracle.com/java/technologies/downloads/ 安装 Java 17+
    pause
    exit /b 1
)
echo ✅ Java 环境正常

REM 检查 Maven
echo [检查] 验证 Maven 环境...
mvn -version >nul 2>&1
if errorlevel 1 (
    color 0C
    echo ❌ 错误: Maven 未安装或未在 PATH 中
    echo 请从 https://maven.apache.org/download.cgi 安装 Maven 3.8+
    pause
    exit /b 1
)
echo ✅ Maven 环境正常

REM 编译后端
echo.
echo [编译] 编译后端项目...
if not exist "target\hospital-0.0.1-SNAPSHOT.jar" (
    echo 首次编译可能需要 2-3 分钟，请耐心等待...
    echo.
    call mvn clean package -DskipTests -q
    if errorlevel 1 (
        color 0C
        echo ❌ 编译失败！
        echo 请检查 Java 版本是否为 17+
        pause
        exit /b 1
    )
    echo ✅ 编译成功
) else (
    echo ✅ 已有编译文件，跳过编译
)

REM 检查 Node.js
echo [检查] 验证 Node.js 环境（前端）...
node -v >nul 2>&1
if errorlevel 1 (
    color 0E
    echo ⚠️  警告: Node.js 未安装，仅启动后端
    echo 前端需手动启动: cd frontend ^&^& npm run dev
    set SKIP_FRONTEND=1
) else (
    echo ✅ Node.js 环境正常
    set SKIP_FRONTEND=0
)

REM 启动服务
cls
echo.
echo ============================================
echo    启动中...
echo ============================================
echo.

echo [后端] 启动 Spring Boot 服务 (端口 9090)...
echo.
start "Hospital Backend - Spring Boot" cmd /k ^
    java -jar target\hospital-0.0.1-SNAPSHOT.jar

REM 等待后端启动
timeout /t 5 /nobreak >nul

REM 检查后端是否启动成功
echo [检查] 验证后端是否启动...
for /l %%i in (1,1,30) do (
    powershell -Command "try { $null = Invoke-WebRequest -Uri 'http://localhost:9090/api/health' -ErrorAction SilentlyContinue; exit 0 } catch { exit 1 }"
    if errorlevel 0 (
        echo ✅ 后端启动成功
        goto BACKEND_OK
    )
    echo 等待中... %%i/30
    timeout /t 1 /nobreak >nul
)

echo ❌ 后端启动失败
pause
exit /b 1

:BACKEND_OK

REM 启动前端
if %SKIP_FRONTEND% equ 0 (
    echo [前端] 安装依赖并启动 Vue 服务 (端口 5173)...
    echo.

    if not exist "frontend\node_modules" (
        echo [npm] 安装依赖中...
        start "Hospital Frontend - npm install" cmd /k ^
            cd /d D:\hospital\hospital\frontend ^& npm install ^& pause
        timeout /t 10 /nobreak >nul
    )

    echo [Vite] 启动开发服务器...
    start "Hospital Frontend - Vite" cmd /k ^
        cd /d D:\hospital\hospital\frontend ^& npm run dev
) else (
    color 0E
    echo ⚠️  跳过前端启动
)

REM 启动完成信息
cls
echo.
echo ============================================
echo    ✅ 系统启动完成！
echo ============================================
echo.
echo 📍 前端地址:   http://localhost:5173
echo 📍 后端地址:   http://localhost:9090
echo 📍 健康检查:   http://localhost:9090/api/health
echo.
echo 🔐 默认账户:
echo    Email:    admin@hospital.local
echo    Password: Admin123!
echo.
echo 📚 相关文档:
echo    - QUICK_START_CN.md       (快速开始)
echo    - WHY_CORS_ERROR.md       (CORS 问题说明)
echo    - CORS_HOTFIX.md          (CORS 详细配置)
echo    - README.md               (完整文档)
echo.
echo 🛑 要停止服务:
echo    1. 关闭所有弹出的命令窗口
echo    2. 或按 Ctrl+C 停止
echo.
echo ============================================
echo.
pause

