@echo off
REM ============================================
REM   医院排班系统 - 问题诊断工具
REM   Diagnostic Tool for CORS Issues
REM ============================================

setlocal enabledelayedexpansion
chcp 65001 >nul 2>&1
color 0B

cls
echo.
echo ============================================
echo    医院排班系统 - 诊断工具
echo    Diagnostic Tool
echo ============================================
echo.

REM 检查编译状态
echo [1] 检查编译状态...
if exist "target\hospital-0.0.1-SNAPSHOT.jar" (
    echo ✅ JAR 文件存在
    for /f "tokens=*" %%A in ('powershell -Command "'{0:N0}' -f (Get-Item 'target\hospital-0.0.1-SNAPSHOT.jar').Length"') do set JAR_SIZE=%%A
    echo   大小: !JAR_SIZE! 字节
) else (
    echo ❌ JAR 文件不存在，需要编译
    echo   解决: mvn clean package -DskipTests
)

echo.

REM 检查 Java 版本
echo [2] 检查 Java 版本...
java -version 2>&1 | findstr /c:"version" >nul
if errorlevel 1 (
    echo ❌ Java 未安装
) else (
    for /f "tokens=3" %%i in ('java -version 2^>^&1 ^| find "version"') do set JAVA_VER=%%i
    echo ✅ Java 版本: !JAVA_VER!
)

echo.

REM 检查 Maven 版本
echo [3] 检查 Maven 版本...
mvn -version 2>&1 | findstr /c:"Apache Maven" >nul
if errorlevel 1 (
    echo ❌ Maven 未安装
) else (
    for /f "tokens=3" %%i in ('mvn -version 2^>^&1 ^| find "Apache Maven"') do set MVN_VER=%%i
    echo ✅ Maven 已安装: 版本 !MVN_VER!
)

echo.

REM 检查后端端口
echo [4] 检查后端服务状态 (端口 9090)...
netstat -ano 2>nul | find ":9090" >nul
if errorlevel 1 (
    echo ⏸️  后端未运行
    echo   启动命令: java -jar target\hospital-0.0.1-SNAPSHOT.jar
) else (
    echo ✅ 后端正在运行
    REM 检查健康
    powershell -Command "$h = try { Invoke-WebRequest -Uri 'http://localhost:9090/api/health' -TimeoutSec 2 -ErrorAction SilentlyContinue } catch { $null }; if ($h) { Write-Host '✅ 健康检查通过'; exit 0 } else { Write-Host '❌ 健康检查失败'; exit 1 }"
)

echo.

REM 检查前端端口
echo [5] 检查前端服务状态 (端口 5173)...
netstat -ano 2>nul | find ":5173" >nul
if errorlevel 1 (
    echo ⏸️  前端未运行
    echo   启动命令: cd frontend ^&^& npm run dev
) else (
    echo ✅ 前端正在运行
)

echo.

REM 检查 CORS 配置
echo [6] 检查 CORS 配置...
if exist "src\main\java\org\example\hospital\security\SecurityConfig.java" (
    findstr /c:"setAllowedOrigins" "src\main\java\org\example\hospital\security\SecurityConfig.java" >nul
    if errorlevel 1 (
        echo ❌ SecurityConfig 未找到 setAllowedOrigins
    ) else (
        echo ✅ SecurityConfig 包含 CORS 配置

        REM 检查是否使用了通配符
        findstr /c:"""*""" "src\main\java\org\example\hospital\security\SecurityConfig.java" >nul
        if errorlevel 1 (
            echo ✅ 未使用不安全的通配符 "*"
        ) else (
            echo ⚠️  警告: 检测到通配符 "*"，可能导致 CORS 错误
        )
    )
)

echo.

REM 检查 .env 文件
echo [7] 检查环境配置 (.env)...
if exist ".env" (
    echo ✅ .env 文件存在

    findstr /c:"CORS_ALLOWED_ORIGINS" .env >nul
    if errorlevel 1 (
        echo ⚠️  未配置 CORS_ALLOWED_ORIGINS
    ) else (
        echo ✅ CORS_ALLOWED_ORIGINS 已配置
    )

    findstr /c:"DB_HOST" .env >nul
    if errorlevel 1 (
        echo ⚠️  未配置数据库连接
    ) else (
        echo ✅ 数据库配置已设置
    )
) else (
    echo ⚠️  .env 文件不存在
)

echo.

REM 检查数据库连接
echo [8] 检查 PostgreSQL 连接...
findstr /c:"DB_HOST" .env >nul
if errorlevel 0 (
    echo ℹ️  无法远程检查数据库，请手动验证：
    echo    - PostgreSQL 是否运行
    echo    - 连接信息是否正确
    echo    - 端口 5432 是否开放
) else (
    echo ℹ️  请设置 .env 中的数据库配置
)

echo.

REM 建议
echo ============================================
echo    诊断建议
echo ============================================
echo.
echo 常见问题排查:
echo.
echo [CORS 错误]
echo   ❌ 错误: "allowedOrigins cannot contain *"
echo   ✅ 解决: 查看 WHY_CORS_ERROR.md
echo.
echo [编译失败]
echo   ❌ 错误: "不支持发行版本 21"
echo   ✅ 解决: 使用 Java 17-21
echo            mvn clean package -DskipTests
echo.
echo [端口被占用]
echo   ❌ 错误: "Port 9090 already in use"
echo   ✅ 解决: taskkill /F /IM java.exe
echo            或更改 application.yml 中的 server.port
echo.
echo [数据库连接]
echo   ❌ 错误: "Cannot get a connection"
echo   ✅ 解决: 检查 .env 中的数据库配置
echo            确保 PostgreSQL 运行
echo.

echo.
echo ============================================
echo    一键启动脚本
echo ============================================
echo.
echo 执行: 启动系统.bat
echo 或手动:
echo   1. 编译: mvn clean package -DskipTests
echo   2. 后端: java -jar target\hospital-0.0.1-SNAPSHOT.jar
echo   3. 前端: cd frontend ^& npm run dev
echo.

pause

