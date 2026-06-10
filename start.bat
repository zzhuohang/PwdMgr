@echo off
setlocal enabledelayedexpansion

echo ========================================
echo   PwdMgr - Password Manager
echo ========================================
echo.

cd /d "%~dp0backend"

:: Check Java
echo [1/2] Checking Java...
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java not found. Please install JDK 21+
    pause
    exit /b 1
)
echo [OK] Java found

:: Check JAR
echo [2/2] Checking JAR...
set "JAR_FILE=target\password-manager-1.0.0.jar"
if not exist "%JAR_FILE%" (
    echo [WARN] JAR not found, building...
    mvn package -DskipTests -Dp3c.skip=true -q
    if errorlevel 1 (
        echo [ERROR] Build failed
        pause
        exit /b 1
    )
)
echo [OK] JAR ready

:: Build frontend if static dir missing
if not exist "src\main\resources\static\index.html" (
    echo [WARN] Frontend not built, building...
    cd /d "%~dp0backend\frontend"
    call npm run build
    cd /d "%~dp0backend"
)

echo.
echo ========================================
echo   Starting Password Manager
echo ========================================
echo.
echo URL: http://localhost:8880
echo Default: admin / admin123
echo H2 Console: http://localhost:8880/h2-console
echo.

:: Kill existing instance to avoid port conflict
taskkill /f /im java.exe >nul 2>&1
echo (Stopped existing instances)
timeout /t 2 /nobreak >nul

start http://localhost:8880
java -jar %JAR_FILE%

pause
