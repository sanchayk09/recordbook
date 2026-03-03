@echo off
REM ============================================================
REM Fix SOLD_BY_SALESMAN Enum Error
REM ============================================================

echo ========================================
echo Fixing warehouse_ledger enum issue
echo ========================================
echo.

REM Try common MySQL paths
SET MYSQL_PATHS[0]="C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
SET MYSQL_PATHS[1]="C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe"
SET MYSQL_PATHS[2]="C:\xampp\mysql\bin\mysql.exe"
SET MYSQL_PATHS[3]="C:\wamp64\bin\mysql\mysql8.0.31\bin\mysql.exe"
SET MYSQL_PATHS[4]="mysql.exe"

SET MYSQL_CMD=
FOR %%P IN (%MYSQL_PATHS[0]% %MYSQL_PATHS[1]% %MYSQL_PATHS[2]% %MYSQL_PATHS[3]% %MYSQL_PATHS[4]%) DO (
    IF EXIST %%~P (
        SET MYSQL_CMD=%%~P
        GOTO :FOUND
    )
)

:FOUND
IF "%MYSQL_CMD%"=="" (
    echo ERROR: MySQL not found!
    echo.
    echo Please install MySQL or set the path manually:
    echo   SET PATH=%PATH%;C:\Program Files\MySQL\MySQL Server 8.0\bin
    echo.
    pause
    exit /b 1
)

echo Found MySQL at: %MYSQL_CMD%
echo.

REM Run the SQL fix
echo Running SQL migration...
%MYSQL_CMD% -u root -pAsdqwe123. < "%~dp0V009_AddSoldBySalesmanToEnum.sql"

IF %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo SUCCESS! Enum updated.
    echo ========================================
    echo.
    echo Next steps:
    echo   1. Restart your Spring Boot application
    echo   2. Test daily sales entry
    echo   3. Check that stock updates correctly
    echo.
) ELSE (
    echo.
    echo ========================================
    echo ERROR: Failed to run SQL migration
    echo ========================================
    echo.
    echo Try running manually in MySQL Workbench:
    echo   1. Open MySQL Workbench
    echo   2. Connect to localhost
    echo   3. Open file: V009_AddSoldBySalesmanToEnum.sql
    echo   4. Execute the script
    echo.
)

pause

