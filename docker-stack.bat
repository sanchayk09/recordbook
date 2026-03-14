@echo off
setlocal

cd /d "%~dp0"

if "%~1"=="" goto :help
if /I "%~1"=="up" goto :up
if /I "%~1"=="down" goto :down
if /I "%~1"=="reset" goto :reset
goto :help

:up
echo Starting Docker Compose services...
docker compose -f docker-compose.yml up -d
if %errorlevel% neq 0 (
  echo.
  echo Failed to start services.
  exit /b %errorlevel%
)
echo.
echo Services started.
docker compose -f docker-compose.yml ps
exit /b 0

:down
echo Stopping Docker Compose services...
docker compose -f docker-compose.yml down
if %errorlevel% neq 0 (
  echo.
  echo Failed to stop services.
  exit /b %errorlevel%
)
echo.
echo Services stopped.
exit /b 0

:reset
echo Stopping services and removing volumes...
docker compose -f docker-compose.yml down -v
if %errorlevel% neq 0 (
  echo.
  echo Failed to reset services.
  exit /b %errorlevel%
)
echo.
echo Services reset (containers + volumes removed).
exit /b 0

:help
echo Usage:
echo   docker-stack.bat up
echo   docker-stack.bat down
echo   docker-stack.bat reset
exit /b 1
