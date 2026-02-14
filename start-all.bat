@echo off
setlocal enabledelayedexpansion

REM ==========================================
REM 医院排班系统 - 完整启动脚本
REM Usage: start-all.bat [--dry-run] [--backend-only] [--frontend-only] [--skip-port-check]
REM ==========================================

set ROOT=%~dp0
set BACKEND_PORT=9090
set FRONTEND_PORT=5173

REM 定义颜色和符号
for /f "delims=" %%A in ('echo prompt $E^| cmd') do set "ESC=%%A"
set "GREEN=%ESC%[32m"
set "YELLOW=%ESC%[33m"
set "RED=%ESC%[31m"
set "BLUE=%ESC%[34m"
set "RESET=%ESC%[0m"

REM 解析命令行参数
set DRY_RUN=0
set BACKEND_ONLY=0
set FRONTEND_ONLY=0
set SKIP_PORT_CHECK=0

if "%1"=="--dry-run" set DRY_RUN=1
if "%1"=="--backend-only" set BACKEND_ONLY=1
if "%1"=="--frontend-only" set FRONTEND_ONLY=1
if "%1"=="--skip-port-check" set SKIP_PORT_CHECK=1

if "%2"=="--dry-run" set DRY_RUN=1
if "%2"=="--backend-only" set BACKEND_ONLY=1
if "%2"=="--frontend-only" set FRONTEND_ONLY=1
if "%2"=="--skip-port-check" set SKIP_PORT_CHECK=1

if "%3"=="--skip-port-check" set SKIP_PORT_CHECK=1

cls

REM 干运行模式
if %DRY_RUN%==1 goto dryrun

echo.
echo %BLUE%╔════════════════════════════════════════╗%RESET%
echo %BLUE%║  🏥 医院排班系统 - 前后端完整启动    ║%RESET%
echo %BLUE%╚════════════════════════════════════════╝%RESET%
echo.

REM 检查项目目录
if not exist "%ROOT%pom.xml" (
  echo %RED%[ERROR] 后端项目文件缺失（pom.xml）%RESET%
  exit /b 1
)

if not exist "%ROOT%frontend" (
  echo %RED%[ERROR] 前端项目目录不存在%RESET%
  exit /b 1
)

echo %YELLOW%[INFO]%RESET% 准备启动服务...
echo.

REM 检查端口（如果不跳过）
if %SKIP_PORT_CHECK%==0 (
  echo %YELLOW%[INFO]%RESET% 检查端口占用...

  REM 检查后端端口
  netstat -ano | findstr :%BACKEND_PORT% >nul 2>&1
  if !errorlevel! equ 0 (
    echo %RED%[WARNING] 后端端口 %BACKEND_PORT% 已被占用，尝试清理...%RESET%
    for /f "tokens=5" %%i in ('netstat -ano ^| findstr :%BACKEND_PORT%') do (
      taskkill /PID %%i /F >nul 2>&1
      if !errorlevel! equ 0 echo %GREEN%[✓]%RESET% 进程已清理
    )
  ) else (
    echo %GREEN%[✓]%RESET% 后端端口 %BACKEND_PORT% 可用
  )

  REM 检查前端端口
  netstat -ano | findstr :%FRONTEND_PORT% >nul 2>&1
  if !errorlevel! equ 0 (
    echo %RED%[WARNING] 前端端口 %FRONTEND_PORT% 已被占用，尝试清理...%RESET%
    for /f "tokens=5" %%i in ('netstat -ano ^| findstr :%FRONTEND_PORT%') do (
      taskkill /PID %%i /F >nul 2>&1
      if !errorlevel! equ 0 echo %GREEN%[✓]%RESET% 进程已清理
    )
  ) else (
    echo %GREEN%[✓]%RESET% 前端端口 %FRONTEND_PORT% 可用
  )

  echo.
)

echo %GREEN%════════════════════════════════════════%RESET%
echo %YELLOW%[INFO]%RESET% 启动配置:
echo %GREEN%  • 后端地址: http://localhost:%BACKEND_PORT%%RESET%
echo %GREEN%  • 后端 API: http://localhost:%BACKEND_PORT%/api%RESET%
echo %GREEN%  • 前端地址: http://localhost:%FRONTEND_PORT%%RESET%
echo %GREEN%  • 工作目录: %ROOT%%RESET%
echo %GREEN%════════════════════════════════════════%RESET%
echo.

REM 启动后端
if %FRONTEND_ONLY%==0 (
  echo %YELLOW%[INFO]%RESET% 启动后端服务...
  if %SKIP_PORT_CHECK%==0 (
    start "🏥 医院排班系统 - 后端 (端口:%BACKEND_PORT%)" cmd /k "%ROOT%start-backend.bat --skip-port-check"
  ) else (
    start "🏥 医院排班系统 - 后端 (端口:%BACKEND_PORT%)" cmd /k "%ROOT%start-backend.bat"
  )
  timeout /t 5 /nobreak >nul
)

REM 启动前端
if %BACKEND_ONLY%==0 (
  echo %YELLOW%[INFO]%RESET% 启动前端服务...
  if %SKIP_PORT_CHECK%==0 (
    start "🏥 医院排班系统 - 前端 (端口:%FRONTEND_PORT%)" cmd /k "%ROOT%start-frontend.bat --skip-port-check"
  ) else (
    start "🏥 医院排班系统 - 前端 (端口:%FRONTEND_PORT%)" cmd /k "%ROOT%start-frontend.bat"
  )
)

echo.
echo %GREEN%════════════════════════════════════════%RESET%
echo %GREEN%  ✓ 启动完成！%RESET%
echo %GREEN%════════════════════════════════════════%RESET%
echo.
echo %YELLOW%[访问地址]%RESET%
echo %GREEN%  • 前端应用: http://localhost:%FRONTEND_PORT%%RESET%
echo %GREEN%  • 后端 API: http://localhost:%BACKEND_PORT%/api%RESET%
echo %GREEN%  • 健康检查: http://localhost:%BACKEND_PORT%/api/health%RESET%
echo %GREEN%  • 默认账户: admin@hospital.local / Admin123!%RESET%
echo.
echo %YELLOW%[快速命令]%RESET%
echo %GREEN%  • 仅后端:  start-backend.bat%RESET%
echo %GREEN%  • 仅前端:  start-frontend.bat%RESET%
echo %GREEN%  • 完整启动: start-all.bat%RESET%
echo %GREEN%  • 预览步骤: start-all.bat --dry-run%RESET%
echo.
echo %YELLOW%[说明]%RESET%
echo   两个新窗口已打开分别运行后端和前端
echo   请勿关闭任何窗口，除非需要停止服务
echo   按 Ctrl+C 可在各窗口中停止对应服务
echo.
pause

exit /b 0

REM ==================== 干运行模式 ====================
:dryrun
echo.
echo %GREEN%=== 医院排班系统启动 (DRY RUN 模式) ===%RESET%
echo.
echo %YELLOW%[配置信息]%RESET%
echo %GREEN%  • 后端端口: %BACKEND_PORT%%RESET%
echo %GREEN%  • 前端端口: %FRONTEND_PORT%%RESET%
echo %GREEN%  • 项目路径: %ROOT%%RESET%
echo.
echo %YELLOW%[启动步骤]%RESET%
if %BACKEND_ONLY%==0 if %FRONTEND_ONLY%==0 (
  echo   1️⃣  启动后端服务 (新窗口)
  echo       命令: start-backend.bat --skip-port-check
  echo       地址: http://localhost:%BACKEND_PORT%
  echo       等待: 5 秒
  echo.
  echo   2️⃣  启动前端服务 (新窗口)
  echo       命令: start-frontend.bat --skip-port-check
  echo       地址: http://localhost:%FRONTEND_PORT%
) else if %BACKEND_ONLY%==1 (
  echo   启动后端服务
  echo   命令: start-backend.bat
  echo   地址: http://localhost:%BACKEND_PORT%
) else if %FRONTEND_ONLY%==1 (
  echo   启动前端服务
  echo   命令: start-frontend.bat
  echo   地址: http://localhost:%FRONTEND_PORT%
)
echo.
echo %YELLOW%[预期结果]%RESET%
if %BACKEND_ONLY%==0 if %FRONTEND_ONLY%==0 (
  echo   ✓ 两个新窗口分别运行后端和前端
  echo   ✓ 浏览器自动打开前端页面
  echo   ✓ 后端 API 在 http://localhost:%BACKEND_PORT%/api 可访问
  echo   ✓ 前端在 http://localhost:%FRONTEND_PORT% 可访问
) else if %BACKEND_ONLY%==1 (
  echo   ✓ 后端服务启动
  echo   ✓ 健康检查: http://localhost:%BACKEND_PORT%/api/health
) else (
  echo   ✓ 前端服务启动
  echo   ✓ 浏览器自动打开: http://localhost:%FRONTEND_PORT%
)
echo.
echo %YELLOW%[可用命令]%RESET%
echo   start-all.bat                 同时启动前后端
echo   start-all.bat --backend-only  仅启动后端
echo   start-all.bat --frontend-only 仅启动前端
echo   start-all.bat --skip-port-check 跳过端口检查
echo   start-all.bat --dry-run       显示此预览
echo.

pause
exit /b 0
