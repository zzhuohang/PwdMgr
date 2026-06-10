@echo off
setlocal enabledelayedexpansion

echo ============================================
echo   PwdMgr - Build EXE Package
echo ============================================
echo.

:: Config
set "PROJECT_DIR=%~dp0backend"
set "JDK_HOME=D:\software\Programming\JDK21"
set "APP_NAME=PwdMgr"
set "APP_VERSION=1.0.0"
set "MAIN_CLASS=com.pwdmgr.PasswordManagerApplication"
set "MAIN_JAR=password-manager-%APP_VERSION%.jar"

:: Check JDK
if not exist "%JDK_HOME%\bin\java.exe" (
    echo [ERROR] JDK not found: %JDK_HOME%
    echo Please edit JDK_HOME in this script
    pause
    exit /b 1
)

set "PATH=%JDK_HOME%\bin;%PATH%"
echo [OK] JDK: %JDK_HOME%
echo.

:: ===== Step 1: Build Frontend =====
echo [1/5] Building frontend...
cd /d "%PROJECT_DIR%\frontend"
call npm run build
if %errorlevel% neq 0 (
    echo [FAIL] Frontend build failed
    pause
    exit /b 1
)
echo [OK] Frontend built
echo.

:: ===== Step 2: Build Backend JAR =====
echo [2/5] Building backend JAR...

:: Kill any running Java process to avoid file lock
taskkill /f /im java.exe >nul 2>&1
echo     (Stopped existing Java processes)
timeout /t 2 /nobreak >nul

cd /d "%PROJECT_DIR%"
call mvn clean package -DskipTests -Dp3c.skip=true -q
if %errorlevel% neq 0 (
    echo [FAIL] Backend build failed
    pause
    exit /b 1
)
echo [OK] Backend built
echo.

:: ===== Step 3: Analyze JDK Modules =====
echo [3/5] Analyzing JDK module dependencies...
set "FAT_JAR=%PROJECT_DIR%\target\%MAIN_JAR%"
if not exist "%FAT_JAR%" (
    echo [ERROR] JAR not found: %FAT_JAR%
    pause
    exit /b 1
)

set "JDK_MODULES=java.base,java.desktop,java.management,java.naming,java.net.http,java.security.jgss,java.sql,java.transaction.xa,java.xml,jdk.unsupported,java.instrument,jdk.management"

"%JDK_HOME%\bin\jdeps" --print-module-deps --ignore-missing-deps "%FAT_JAR%" > "%TEMP%\jdeps_modules.txt" 2>nul
if %errorlevel% equ 0 (
    set /p JDK_MODULES=<"%TEMP%\jdeps_modules.txt"
    echo [OK] jdeps analysis done
) else (
    echo [WARN] jdeps failed, using default module list
)
echo Modules: %JDK_MODULES%
echo.

:: ===== Step 4: Create Minimal JRE =====
echo [4/5] Creating minimal JRE with jlink...
set "JRE_DIR=%PROJECT_DIR%\target\runtime"
if exist "%JRE_DIR%" rmdir /s /q "%JRE_DIR%"
"%JDK_HOME%\bin\jlink" --module-path "%JDK_HOME%\jmods" --add-modules %JDK_MODULES% --output "%JRE_DIR%" --strip-debug --no-header-files --no-man-pages --compress zip-6
if %errorlevel% neq 0 (
    echo [FAIL] jlink failed
    pause
    exit /b 1
)
echo [OK] Minimal JRE created
echo.

:: ===== Step 5: Package EXE =====
echo [5/5] Packaging EXE with jpackage...
set "DIST_DIR=%PROJECT_DIR%\target\dist"
if exist "%DIST_DIR%" rmdir /s /q "%DIST_DIR%"

"%JDK_HOME%\bin\jpackage" --type exe --name "%APP_NAME%" --app-version "%APP_VERSION%" --description "Password Manager" --vendor "PwdMgr" --input "%PROJECT_DIR%\target" --main-jar "%MAIN_JAR%" --main-class "%MAIN_CLASS%" --runtime-image "%JRE_DIR%" --dest "%DIST_DIR%" --win-console --win-shortcut --win-menu --win-menu-group "PwdMgr"

if %errorlevel% neq 0 (
    echo.
    echo [WARN] jpackage failed (maybe missing WiX Toolset)
    echo Switching to portable package...
    goto :fallback
)

echo [OK] EXE packaged
echo Output: %DIST_DIR%\%APP_NAME%-%APP_VERSION%.exe
goto :done

:fallback
:: ===== Fallback: Portable bat + JRE package =====
echo [FALLBACK] Creating portable package...
set "PORTABLE_DIR=%PROJECT_DIR%\target\portable\%APP_NAME%"
if exist "%PORTABLE_DIR%" rmdir /s /q "%PORTABLE_DIR%"
mkdir "%PORTABLE_DIR%\jre"
mkdir "%PORTABLE_DIR%\data"

:: Copy JRE
xcopy /e /i /q "%JRE_DIR%\*" "%PORTABLE_DIR%\jre\" >nul

:: Copy JAR
copy /y "%FAT_JAR%" "%PORTABLE_DIR%\%APP_NAME%.jar" >nul

:: Create launcher bat (ASCII only, no Chinese)
>"%PORTABLE_DIR%\start.bat" (
    echo @echo off
    echo title PwdMgr - Password Manager
    echo echo Starting Password Manager...
    echo echo Open browser: http://localhost:8880
    echo echo Default account: admin / admin123
    echo start http://localhost:8880
    echo jre\bin\java.exe -jar %APP_NAME%.jar
    echo pause
)

echo [OK] Portable package created
echo Output: %PORTABLE_DIR%
echo Usage:  Run "start.bat" to launch

:done
echo.
echo ============================================
echo   Build complete!
echo ============================================
pause
