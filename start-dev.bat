@echo off
setlocal

set "ROOT=%~dp0"
set "DRY=0"
set "MODE=both"

if "%~1"=="" goto end_parse

for %%A in (%*) do (
  if /I "%%~A"=="--dry-run" (set "DRY=1") else (
    if /I "%%~A"=="--backend-only" (set "MODE=backend") else (
      if /I "%%~A"=="--frontend-only" (set "MODE=frontend") else (
        echo [ERROR] Unknown argument: %%~A
        goto usage
      )
    )
  )
)

:end_parse

set "BACKEND_BAT=%ROOT%start-backend.bat"
set "FRONTEND_BAT=%ROOT%start-frontend.bat"

if /I "%MODE%"=="backend" if not exist "%BACKEND_BAT%" (
  echo [ERROR] Missing: %BACKEND_BAT%
  exit /b 1
)
if /I "%MODE%"=="frontend" if not exist "%FRONTEND_BAT%" (
  echo [ERROR] Missing: %FRONTEND_BAT%
  exit /b 1
)
if /I "%MODE%"=="both" if not exist "%BACKEND_BAT%" (
  echo [ERROR] Missing: %BACKEND_BAT%
  exit /b 1
)
if /I "%MODE%"=="both" if not exist "%FRONTEND_BAT%" (
  echo [ERROR] Missing: %FRONTEND_BAT%
  exit /b 1
)

if "%DRY%"=="1" (
  if /I "%MODE%"=="backend" echo [DRY] Backend: call "%BACKEND_BAT%" --dry-run
  if /I "%MODE%"=="frontend" echo [DRY] Frontend: call "%FRONTEND_BAT%" --dry-run
  if /I "%MODE%"=="both" echo [DRY] Backend: call "%BACKEND_BAT%" --dry-run
  if /I "%MODE%"=="both" echo [DRY] Frontend: call "%FRONTEND_BAT%" --dry-run
  exit /b 0
)

echo [INFO] Starting in separate terminals...
if /I "%MODE%"=="backend" echo [INFO] Backend: http://localhost:9090
if /I "%MODE%"=="frontend" echo [INFO] Frontend: http://localhost:5173
if /I "%MODE%"=="both" echo [INFO] Backend: http://localhost:9090
if /I "%MODE%"=="both" echo [INFO] Frontend: http://localhost:5173

if /I "%MODE%"=="backend" start "backend" cmd /k ""call "%BACKEND_BAT%"""
if /I "%MODE%"=="frontend" start "frontend" cmd /k ""call "%FRONTEND_BAT%"""
if /I "%MODE%"=="both" start "backend" cmd /k ""call "%BACKEND_BAT%"""
if /I "%MODE%"=="both" start "frontend" cmd /k ""call "%FRONTEND_BAT%"""

exit /b 0

:usage
echo Usage: start-dev.bat [--dry-run] [--backend-only^|--frontend-only]
exit /b 1
