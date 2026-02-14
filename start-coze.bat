@echo off
REM Hospital Scheduling System - Coze Complete Startup Script
REM Includes: Configuration verification, Coze API, Backend, and Frontend

setlocal enabledelayedexpansion
chcp 65001 >nul

echo.
echo =========================================
echo   Hospital Scheduling System
echo   Complete Startup with Coze
echo =========================================
echo.

set PROJECT_ROOT=%~dp0

REM Step 1: Verify Coze configuration
echo [1] Verifying Coze configuration...
python "%PROJECT_ROOT%verify_coze.py"
if errorlevel 1 (
    echo.
    echo Warning: Coze configuration verification encountered issues, please check .env file
    echo.
    pause
)

REM Step 2: Start Coze API Server
echo.
echo [2] Starting Coze AI Workflow API Server (port 8000)...
start "Coze API Server" python "%PROJECT_ROOT%coze_api_server.py"
timeout /t 3 /nobreak

REM Step 3: Start Backend Service
echo.
echo [3] Starting Backend Service (port 9090)...
start "Hospital Backend" cmd /k "cd /d %PROJECT_ROOT% && mvn spring-boot:run"
timeout /t 5 /nobreak

REM Step 4: Start Frontend Development Server
echo.
echo [4] Starting Frontend Development Server (port 5174)...
start "Hospital Frontend" cmd /k "cd /d %PROJECT_ROOT%frontend && npm run dev"

echo.
echo =========================================
echo   All services started successfully!
echo =========================================
echo.
echo Access URLs:
echo   - Frontend:   http://localhost:5174
echo   - Backend:    http://localhost:9090
echo   - Coze API:   http://localhost:8000/health
echo.
echo Login credentials:
echo   - Email: admin@hospital.local
echo   - Password: Admin123!
echo.
echo Quick test:
echo   1. Login and go to "Agent Chat"
echo   2. Type a message in the chat box and click send
echo   3. Wait for Coze AI response
echo.
pause
pause
