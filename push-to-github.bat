@echo off
REM Git Push to GitHub - Batch Script
REM Usage: push-to-github.bat [GitHubUsername] [RepositoryName]

setlocal enabledelayedexpansion

REM Colors (using ANSI codes if terminal supports it)
set "GREEN=[92m"
set "RED=[91m"
set "YELLOW=[93m"
set "BLUE=[94m"
set "RESET=[0m"

REM Parse arguments
set "GITHUB_USERNAME=%~1"
set "REPO_NAME=%~2"
set "BRANCH_NAME=main"

if "!GITHUB_USERNAME!"=="" (
    set "GITHUB_USERNAME=sanchay"
)

if "!REPO_NAME!"=="" (
    set "REPO_NAME=recordbook"
)

echo.
echo ========================================
echo   GitHub Push Setup Script
echo ========================================
echo.

REM Check if Git is installed
git --version >nul 2>&1
if errorlevel 1 (
    echo Error: Git is not installed. Please install from https://git-scm.com
    exit /b 1
)

echo [OK] Git is installed

REM Change to project directory
cd /d C:\sanchay\recordbook
if errorlevel 1 (
    echo Error: Cannot change to project directory
    exit /b 1
)

echo [OK] Changed to project directory

REM Initialize Git if not already done
if not exist ".git" (
    echo [*] Initializing Git repository...
    git init
    echo [OK] Git repository initialized
) else (
    echo [*] Git repository already initialized
)

REM Configure Git user
echo [*] Configuring Git user...
git config user.name "Sanchay"
git config user.email "sanchay@urviclean.com"
echo [OK] Git user configured

REM Add all files
echo [*] Adding all files to Git...
git add .
echo [OK] All files staged

REM Create commit
echo [*] Creating initial commit...
git commit -m "feat: implement daily expense record feature with API"
echo [OK] Commit created

REM Rename branch to main
echo [*] Setting up main branch...
for /f "tokens=*" %%i in ('git rev-parse --abbrev-ref HEAD') do set "CURRENT_BRANCH=%%i"
if not "!CURRENT_BRANCH!"=="!BRANCH_NAME!" (
    git branch -M !BRANCH_NAME!
    echo [OK] Branch renamed to !BRANCH_NAME!
) else (
    echo [OK] Already on !BRANCH_NAME! branch
)

REM Add remote
echo [*] Adding remote repository...
set "REMOTE_URL=https://github.com/!GITHUB_USERNAME!/!REPO_NAME!.git"

git remote get-url origin >nul 2>&1
if errorlevel 1 (
    git remote add origin !REMOTE_URL!
    echo [OK] Remote repository added: !REMOTE_URL!
) else (
    echo [*] Remote 'origin' already exists
    echo [*] To change it, run: git remote set-url origin !REMOTE_URL!
)

REM Display status
echo.
echo ========================================
echo   Setup Complete!
echo ========================================
echo.
echo Repository Configuration:
echo   Username: !GITHUB_USERNAME!
echo   Repository: !REPO_NAME!
echo   Branch: !BRANCH_NAME!
echo   Remote URL: !REMOTE_URL!
echo.
echo Git Status:
git status
echo.
echo IMPORTANT - Next Steps:
echo.
echo 1. Create a repository on GitHub:
echo    https://github.com/new
echo.
echo 2. Push your code to GitHub:
echo    git push -u origin main
echo.
echo 3. You may be prompted for credentials:
echo    - Use your GitHub username
echo    - Use a Personal Access Token as password
echo    (Create at: https://github.com/settings/tokens)
echo.
echo 4. Verify the push:
echo    git log --oneline -5
echo.
echo See GITHUB_PUSH_GUIDE.md for detailed instructions
echo.

endlocal
pause

