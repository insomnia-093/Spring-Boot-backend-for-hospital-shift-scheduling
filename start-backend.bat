@echo off
setlocal enabledelayedexpansion

REM Usage: start-backend.bat [--dry-run]
set ROOT=%~dp0

if "%1"=="--dry-run" (
  echo [DRY] Backend: cd /d "%ROOT%" ^&^& mvn -q -DskipTests spring-boot:run
  exit /b 0
)

echo [INFO] Starting backend...
echo [INFO] Backend: http://localhost:9090

echo [INFO] If Maven fails, ensure JAVA_HOME and Maven are configured.
cd /d "%ROOT%"
set MAVEN_OPTS=-Dfile.encoding=UTF-8
mvn -q -DskipTests spring-boot:run

exit /b 0
