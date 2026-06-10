@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ============================================
echo   密码管理系统 - 一键打包 EXE
echo ============================================
echo.

:: 配置
set "PROJECT_DIR=%~dp0backend"
set "JDK_HOME=D:\software\Programming\JDK21"
set "APP_NAME=PwdMgr"
set "APP_VERSION=1.0.0"
set "MAIN_CLASS=com.pwdmgr.PasswordManagerApplication"
set "MAIN_JAR=password-manager-%APP_VERSION%.jar"

:: 检查 JDK
if not exist "%JDK_HOME%\bin\java.exe" (
    echo [错误] JDK 未找到: %JDK_HOME%
    echo 请修改脚本中的 JDK_HOME 变量
    pause
    exit /b 1
)

set "PATH=%JDK_HOME%\bin;%PATH%"
echo [✓] JDK: %JDK_HOME%
echo.

:: ===== Step 1: 构建前端 =====
echo [1/5] 构建前端...
cd /d "%PROJECT_DIR%\frontend"
call npm run build
if %errorlevel% neq 0 (
    echo [✗] 前端构建失败
    pause
    exit /b 1
)
echo [✓] 前端构建完成
echo.

:: ===== Step 2: 构建后端 =====
echo [2/5] 构建后端 JAR...
cd /d "%PROJECT_DIR%"
call mvn package -DskipTests -q
if %errorlevel% neq 0 (
    echo [✗] 后端构建失败
    pause
    exit /b 1
)
echo [✓] 后端构建完成
echo.

:: ===== Step 3: 分析模块依赖 =====
echo [3/5] 分析 JDK 模块依赖...
set "FAT_JAR=%PROJECT_DIR%\target\%MAIN_JAR%"
if not exist "%FAT_JAR%" (
    echo [错误] JAR 文件未找到: %FAT_JAR%
    pause
    exit /b 1
)

:: jdeps 可能因多版本 JAR 失败，用固定模块列表兜底
set "JDK_MODULES=java.base,java.desktop,java.management,java.naming,java.net.http,java.security.jgss,java.sql,java.transaction.xa,java.xml,jdk.unsupported,java.instrument,jdk.management"

"%JDK_HOME%\bin\jdeps" --print-module-deps --ignore-missing-deps "%FAT_JAR%" > "%TEMP%\jdeps_modules.txt" 2>nul
if %errorlevel% equ 0 (
    set /p JDK_MODULES=<"%TEMP%\jdeps_modules.txt"
    echo [✓] jdeps 分析完成
) else (
    echo [!] jdeps 分析失败，使用默认模块列表
)
echo 模块列表: %JDK_MODULES%
echo.

:: ===== Step 4: 创建精简 JRE =====
echo [4/5] 创建精简 JRE (jlink)...
set "JRE_DIR=%PROJECT_DIR%\target\runtime"
if exist "%JRE_DIR%" rmdir /s /q "%JRE_DIR%"
"%JDK_HOME%\bin\jlink" ^
    --module-path "%JDK_HOME%\jmods" ^
    --add-modules %JDK_MODULES% ^
    --output "%JRE_DIR%" ^
    --strip-debug ^
    --no-header-files ^
    --no-man-pages ^
    --compress zip-6
if %errorlevel% neq 0 (
    echo [✗] jlink 创建失败
    pause
    exit /b 1
)
echo [✓] 精简 JRE 创建完成
echo.

:: ===== Step 5: 打包 EXE =====
echo [5/5] 打包 EXE (jpackage)...
set "DIST_DIR=%PROJECT_DIR%\target\dist"
if exist "%DIST_DIR%" rmdir /s /q "%DIST_DIR%"

:: jpackage 需要 --win-console 来显示控制台输出（适合服务器应用）
"%JDK_HOME%\bin\jpackage" ^
    --type exe ^
    --name "%APP_NAME%" ^
    --app-version "%APP_VERSION%" ^
    --description "个人密码管理系统" ^
    --vendor "PwdMgr" ^
    --input "%PROJECT_DIR%\target" ^
    --main-jar "%MAIN_JAR%" ^
    --main-class "%MAIN_CLASS%" ^
    --runtime-image "%JRE_DIR%" ^
    --dest "%DIST_DIR%" ^
    --win-console ^
    --win-shortcut ^
    --win-menu ^
    --win-menu-group "PwdMgr"

if %errorlevel% neq 0 (
    echo.
    echo [警告] jpackage 可能因缺少 WiX Toolset 失败
    echo 备用方案：使用 launch4j 或 bat2exe
    echo.
    echo === 正在生成备用启动器 ===
    goto :fallback
)

echo [✓] EXE 打包完成
echo.
echo 输出位置: %DIST_DIR%\%APP_NAME%-%APP_VERSION%.exe
goto :done

:fallback
:: ===== 备用方案：生成 bat + jre 启动器 =====
echo [备用] 生成便携式启动包...
set "PORTABLE_DIR=%PROJECT_DIR%\target\portable\%APP_NAME%"
if exist "%PORTABLE_DIR%" rmdir /s /q "%PORTABLE_DIR%"
mkdir "%PORTABLE_DIR%\jre" 2>nul
mkdir "%PORTABLE_DIR%\data" 2>nul

:: 复制 JRE
xcopy /e /i /q "%JRE_DIR%\*" "%PORTABLE_DIR%\jre\" >nul

:: 复制 JAR
copy /y "%FAT_JAR%" "%PORTABLE_DIR%\%APP_NAME%.jar" >nul

:: 创建启动 bat
(
echo @echo off
echo chcp 65001 ^>nul
echo title 密码管理系统
echo echo 正在启动密码管理系统...
echo echo.
echo echo 打开浏览器访问: http://localhost:8880
echo echo 默认账号: admin / admin123
echo echo.
echo start http://localhost:8880
echo jre\bin\java.exe -jar %APP_NAME%.jar
echo pause
) > "%PORTABLE_DIR%\启动.bat"

echo [✓] 便携式启动包创建完成
echo 输出位置: %PORTABLE_DIR%
echo 使用说明: 运行 "启动.bat" 即可启动系统

:done
echo.
echo ============================================
echo   打包完成！
echo ============================================
pause
