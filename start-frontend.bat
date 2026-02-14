@echo off
setlocal enabledelayedexpansion

REM ==========================================
REM 医院排班系统 - 前端启动脚本
REM Usage: start-frontend.bat [--dry-run] [--port 5173]
REM ==========================================

set ROOT=%~dp0
set FRONTEND_PORT=5173
set SKIP_PORT_CHECK=0

REM 定义颜色和符号
for /f "delims=" %%A in ('echo prompt $E^| cmd') do set "ESC=%%A"
set "GREEN=%ESC%[32m"
set "YELLOW=%ESC%[33m"
set "RED=%ESC%[31m"
set "BLUE=%ESC%[34m"
set "RESET=%ESC%[0m"

REM 解析命令行参数
if "%1"=="--dry-run" goto dryrun
if "%1"=="--skip-port-check" set SKIP_PORT_CHECK=1
if not "%1"=="" if not "%1"=="--dry-run" set FRONTEND_PORT=%1

REM 检查前端目录是否存在
if not exist "%ROOT%frontend" (
  echo %RED%[ERROR] 前端目录不存在: %ROOT%frontend%RESET%
  exit /b 1
)

echo.
echo %BLUE%╔════════════════════════════════════════╗%RESET%
echo %BLUE%║   🏥 医院排班系统 - 前端启动          ║%RESET%
echo %BLUE%╚═══���════════════════════════════════════╝%RESET%
echo.

REM 检查端口是否被占用（如果不跳过）
if %SKIP_PORT_CHECK%==0 (
  echo %YELLOW%[INFO]%RESET% 检查端口 %FRONTEND_PORT% 状态...
  netstat -ano | findstr :%FRONTEND_PORT% >nul 2>&1
  if !errorlevel! equ 0 (
    echo %RED%[WARNING] 端口 %FRONTEND_PORT% 已被占用！%RESET%
    echo.
    echo %YELLOW%[INFO]%RESET% 尝试自动清理占用的进程...

    REM 获取占用端口的进程ID
    for /f "tokens=5" %%i in ('netstat -ano ^| findstr :%FRONTEND_PORT%') do (
      echo %YELLOW%[INFO]%RESET% 杀死进程: %%i
      taskkill /PID %%i /F >nul 2>&1
      if !errorlevel! equ 0 (
        echo %GREEN%[✓]%RESET% 进程已清理
      ) else (
        echo %RED%[WARNING] 无法清理进程 %%i，可能需要手动处理%RESET%
      )
    )

    timeout /t 2 /nobreak >nul
    echo.
  ) else (
    echo %GREEN%[✓]%RESET% 端口 %FRONTEND_PORT% 可用
  )
)

REM 进入前端目录
cd /d "%ROOT%frontend"
if errorlevel 1 (
  echo %RED%[ERROR] 无法进入前端目录%RESET%
  exit /b 1
)

echo %YELLOW%[INFO]%RESET% 检查依赖...
if not exist "node_modules" (
  echo %YELLOW%[INFO]%RESET% 首次启动，正在安装依赖...
  echo %YELLOW%[INFO]%RESET% 这可能需要 2-3 分钟...
  call npm install
  if errorlevel 1 (
    echo %RED%[ERROR] npm install 失败%RESET%
    echo %YELLOW%[TIPS]%RESET% 尝试以下方案:
    echo        npm cache clean --force
    echo        npm install
    exit /b 1
  )
  echo %GREEN%[✓]%RESET% 依赖安装成功
) else (
  echo %GREEN%[✓]%RESET% 依赖已安装
)

echo.
echo %GREEN%════════════════════════════════════════%RESET%
echo %YELLOW%[INFO]%RESET% 启动信息:
echo %GREEN%  • 前端地址: http://localhost:%FRONTEND_PORT%%RESET%
echo %GREEN%  • 后端 API: http://localhost:9090/api%RESET%
echo %GREEN%  • WebSocket: http://localhost:9090/ws%RESET%
echo %GREEN%  • 工作目录: %cd%%RESET%
echo %GREEN%════════════════════════════════════════%RESET%
echo.
echo %YELLOW%[INFO]%RESET% 正在启动开发服务器...
echo %YELLOW%[INFO]%RESET% 浏览器将在 3 秒后自动打开...
echo.

REM 延迟 3 秒后打开浏览器（给开发服务器启动时间）
timeout /t 3 /nobreak >nul

REM 检查服务器是否已启动
for /l %%i in (1,1,10) do (
  netstat -ano | findstr :%FRONTEND_PORT% >nul 2>&1
  if !errorlevel! equ 0 (
    echo %GREEN%[✓]%RESET% 开发服务器已启动，打开浏览器...
    start "" "http://localhost:%FRONTEND_PORT%"
    goto dev_server
  )
  timeout /t 1 /nobreak >nul
)

echo %YELLOW%[WARNING] 开发服务器启动超时，尝试打开浏览器...%RESET%
start "" "http://localhost:%FRONTEND_PORT%"

:dev_server
REM 启动开发服务器（前台运行）
echo.
echo %GREEN%════════════════════════════════════════%RESET%
echo %GREEN%  开发服务器运行中... 按 Ctrl+C 停止%RESET%
echo %GREEN%════════════════════════════════════════%RESET%
echo.

call npm run dev

if errorlevel 1 (
  echo.
  echo %RED%[ERROR] 前端启动失败%RESET%
  echo %YELLOW%[TIPS]%RESET% 检查:
  echo        - 是否有 vite.config.js 配置文件
  echo        - package.json 中 dev 脚本是否存在
  echo        - 依赖是否完整
  exit /b 1
)

exit /b 0

REM ==================== 干运行模式 ====================
:dryrun
echo.
echo %GREEN%=== 前端启动 (DRY RUN) ===%RESET%
echo.
echo %YELLOW%[DRY] 工作目录:%RESET% %ROOT%frontend
echo %YELLOW%[DRY] 前端端口:%RESET% %FRONTEND_PORT%
echo.
echo %YELLOW%[DRY] 检查步骤:%RESET%
echo   1. 验证前端目录存在
echo   2. 检查端口 %FRONTEND_PORT% 是否被占用
echo   3. 如果被占用，自动杀死占用进程
echo   4. 检查 node_modules 是否存在
echo   5. 如果不存在，运行 npm install
echo   6. 启动浏览器访问 http://localhost:%FRONTEND_PORT%
echo   7. 运行 npm run dev 启动开发服务器
echo.
echo %YELLOW%[DRY] 完整命令:%RESET%
echo   cd "%ROOT%frontend"
echo   npm install (if not exist node_modules)
echo   start "" "http://localhost:%FRONTEND_PORT%"
echo   npm run dev
echo.
echo %GREEN%[DRY] 预期结果:%RESET%
echo   ✓ 浏览器自动打开
echo   ✓ 开发服务器运行在 http://localhost:%FRONTEND_PORT%
echo   ✓ 可连接到后端 http://localhost:9090/api
echo.

exit /b 0
