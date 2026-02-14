@echo off
REM 医院排班系统 - 完整启动脚本
REM This script starts both backend and frontend services

setlocal enabledelayedexpansion

color 0A
echo.
echo ====================================================
echo    医院排班系统 (Hospital Scheduling System)
echo    完整启动脚本
echo ====================================================
echo.

REM 检查 Java 版本
echo [1/4] 检查 Java 环境...
java -version >nul 2>&1
if errorlevel 1 (
    color 0C
    echo ❌ Java 未安装或未在 PATH 中
    pause
    exit /b 1
)
echo ✅ Java 环境检查通过

REM 检查 Node.js
echo [2/4] 检查 Node.js 环境...
node -v >nul 2>&1
if errorlevel 1 (
    color 0C
    echo ⚠️  Node.js 未安装，仅启动后端
    echo 请手动启动前端: cd frontend && npm run dev
    set FRONTEND_AVAILABLE=0
) else (
    echo ✅ Node.js 环境检查通过
    set FRONTEND_AVAILABLE=1
)

REM 编译后端
echo [3/4] 编译后端项目...
if not exist "target\hospital-0.0.1-SNAPSHOT.jar" (
    call mvn clean package -DskipTests
    if errorlevel 1 (
        color 0C
        echo ❌ 后端编译失败
        pause
        exit /b 1
    )
)
echo ✅ 后端编译成功

REM 启动后端
echo [4/4] 启动服务...
echo.
echo ====================================================
echo    启动后端服务 (Backend: http://localhost:9090)
echo ====================================================
echo.

start "Hospital Backend" cmd /k ^
    "java -jar target\hospital-0.0.1-SNAPSHOT.jar && pause"

timeout /t 3 /nobreak

REM 启动前端
if %FRONTEND_AVAILABLE% equ 1 (
    echo.
    echo ====================================================
    echo    启动前端服务 (Frontend: http://localhost:5173)
    echo ====================================================
    echo.
    start "Hospital Frontend" cmd /k ^
        "cd frontend && npm run dev"
)

echo.
echo ====================================================
echo    所有服务已启动！
echo ====================================================
echo.
echo 📍 后端地址: http://localhost:9090
echo 📍 API 文档: http://localhost:9090/swagger-ui.html
if %FRONTEND_AVAILABLE% equ 1 (
    echo 📍 前端地址: http://localhost:5173
)
echo.
echo 📝 按 Ctrl+C 停止服务
echo.
pause

