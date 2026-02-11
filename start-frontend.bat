@echo off
setlocal enabledelayedexpansion

REM Usage: start-frontend.bat [--dry-run]
set ROOT=%~dp0

for /f "delims=" %%A in ('echo prompt $E^| cmd') do set "ESC=%%A"
set "GREEN=%ESC%[32m"
set "RESET=%ESC%[0m"

if "%1"=="--dry-run" (
  echo [DRY] Frontend: cd /d "%ROOT%frontend" ^&^& if not exist "node_modules" npm install ^&^& npm run dev
  echo [DRY] Browser: start "" "http://localhost:5173"
  echo [DRY] Status: %GREEN%o%RESET% connected
  exit /b 0
)

echo [INFO] Starting frontend...
echo [INFO] Frontend: http://localhost:5173
echo [INFO] Status: %GREEN%o%RESET% connected

cd /d "%ROOT%frontend"
if not exist "node_modules" npm install
start "" "http://localhost:5173"
npm run dev

exit /b 0
