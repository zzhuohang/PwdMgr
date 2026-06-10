@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ========================================
echo   密码管理系统 - 一键启动
echo ========================================
echo.

:: 切换到后端目录
cd /d "%~dp0backend"

:: 检查Java
echo [1/2] 检查 Java 环境...
java -version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到 Java，请安装 JDK 21+
    pause
    exit /b 1
)
echo [✓] Java 环境正常

:: 检查 Maven（开发模式需要）
echo [2/2] 检查 Maven 环境...
mvn -v >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到 Maven，请安装 Maven 3.6+
    pause
    exit /b 1
)
echo [✓] Maven 环境正常

echo.
echo ========================================
echo   启动服务（H2 嵌入式数据库）
echo ========================================
echo.

:: 启动后端（包含前端静态文件）
echo [启动] 密码管理系统...
echo.
echo 访问地址: http://localhost:8880
echo 默认账号: admin / admin123
echo H2控制台: http://localhost:8880/h2-console
echo.
start http://localhost:8880
mvn spring-boot:run

pause
