@echo off
REM Docker 部署启动脚本 (Windows)

setlocal enabledelayedexpansion

echo ================================
echo 医院排班系统 - Docker 部署
echo ================================
echo.

REM 检查 Docker
echo 检查 Docker...
docker --version >nul 2>&1
if errorlevel 1 (
    echo Docker 未安装，请先安装 Docker Desktop
    exit /b 1
)
for /f "tokens=*" %%a in ('docker --version') do (
    echo   %%a
)

REM 检查 Docker Compose
echo 检查 Docker Compose...
docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo Docker Compose 未安装
    exit /b 1
)
for /f "tokens=*" %%a in ('docker-compose --version') do (
    echo   %%a
)

REM 检查 .env 文件
echo 检查环境配置...
if not exist ".env" (
    echo .env 文件不存在
    exit /b 1
)
echo   .env 文件已找到

REM 启动服务
echo.
echo 启动服务...
docker-compose up -d
if errorlevel 1 (
    echo 启动失败，请检查 Docker Desktop 是否运行
    exit /b 1
)

REM 等待服务启动
echo.
echo 等待服务启动...
timeout /t 10 /nobreak

REM 检查服务状态
echo.
echo 服务状态检查...
docker-compose ps

REM 检查后端健康
echo.
echo 检查后端健康状态...
setlocal enabledelayedexpansion
for /l %%i in (1,1,10) do (
    curl -s http://localhost:9090/api/health >nul 2>&1
    if errorlevel 1 (
        echo   等待后端启动... (%%i/10^)
        timeout /t 3 /nobreak
    ) else (
        echo 后端服务已就绪
        goto :frontend_check
    )
)

:frontend_check
REM 检查前端
echo 检查前端...
for /l %%i in (1,1,10) do (
    curl -s http://localhost/ >nul 2>&1
    if errorlevel 1 (
        echo   等待前端启动... (%%i/10^)
        timeout /t 3 /nobreak
    ) else (
        echo 前端服务已就绪
        goto :db_check
    )
)

:db_check
REM 检查数据库
echo 检查数据库...
docker-compose exec -T postgres pg_isready -U postgres >nul 2>&1
if errorlevel 1 (
    echo 数据库可能未完全启动
) else (
    echo 数据库服务已就绪
)

echo.
echo ================================
echo 部署完成！
echo ================================
echo.
echo 访问地址:
echo   前端:       http://localhost
echo   后端 API:   http://localhost:9090/api
echo   健康检查:   http://localhost:9090/api/health
echo   监控指标:   http://localhost:9090/actuator/prometheus
echo.
echo 常用命令:
echo   查看日志:   docker-compose logs -f backend
echo   停止服务:   docker-compose down
echo   重启服务:   docker-compose restart
echo.
echo 更多信息请查看 DOCKER_DEPLOYMENT.md
echo.

pause
