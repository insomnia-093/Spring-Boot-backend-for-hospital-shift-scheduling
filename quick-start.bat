@echo off
REM Hospital Scheduling System - Quick Start Script
REM One-click startup for Coze API, Backend, and Frontend

setlocal enabledelayedexpansion
chcp 65001 >nul

echo.
echo ===============================================
echo   Hospital Scheduling System - Quick Start
echo ===============================================
echo.

cd /d "%~dp0"

REM Start Coze API Server
echo [1/3] Starting Coze API Server...
start "Coze API" python coze_api_server.py
timeout /t 2 /nobreak

REM Start Backend
echo.
echo [2/3] Starting Backend Service...
start "Backend" cmd /k mvn spring-boot:run
timeout /t 5 /nobreak

REM Start Frontend
echo.
echo [3/3] Starting Frontend Service...
start "Frontend" cmd /k "cd frontend && npm run dev"

echo.
echo ===============================================
echo   All services started successfully!
echo ===============================================
echo.
echo Frontend:   http://localhost:5174
echo Backend:    http://localhost:9090
echo Coze API:   http://localhost:8000
echo.
echo Login:
echo   Email: admin@hospital.local
echo   Password: Admin123!
echo.
echo Press any key to continue...
echo.
pause
