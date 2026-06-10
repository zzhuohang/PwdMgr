@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ========================================
echo   个人密码管理系统 - 启动脚本
echo ========================================
echo.

:: 检查Java
echo [1/4] 检查Java环境...
java -version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到Java，请安装JDK 21+
    pause
    exit /b 1
)
echo [✓] Java环境正常

:: 检查Node.js
echo [2/4] 检查Node.js环境...
node -v >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到Node.js，请安装Node.js 18+
    pause
    exit /b 1
)
echo [✓] Node.js环境正常

:: 检查Maven
echo [3/4] 检查Maven环境...
mvn -v >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到Maven，请安装Maven 3.6+
    pause
    exit /b 1
)
echo [✓] Maven环境正常

:: 检查MySQL
echo [4/4] 检查MySQL连接...
mysql -u root -p -e "SELECT 1;" >nul 2>&1
if errorlevel 1 (
    echo [警告] 无法连接到MySQL，请确保MySQL已启动并配置正确
    echo [提示] 请先执行数据库初始化脚本: backend/src/main/resources/db/init.sql
)
echo [✓] 环境检查完成

echo.
echo ========================================
echo   启动服务
echo ========================================
echo.

:: 启动后端
echo [启动] 后端服务...
cd backend
start "密码管理系统-后端" cmd /k "mvn spring-boot:run"
cd ..

:: 等待后端启动
echo [等待] 后端服务启动中（约30秒）...
timeout /t 30 /nobreak >nul

:: 启动前端
echo [启动] 前端服务...
cd frontend
npm install >nul 2>&1
start "密码管理系统-前端" cmd /k "npm run dev"
cd ..

echo.
echo ========================================
echo   启动完成
echo ========================================
echo.
echo 后端地址: http://localhost:8080
echo 前端地址: http://localhost:5173
echo.
echo [提示] 浏览器扩展安装方法:
echo   1. 打开Chrome浏览器，访问 chrome://extensions/
echo   2. 开启「开发者模式」
echo   3. 点击「加载已解压的扩展程序」
echo   4. 选择 browser-extension 目录
echo.
pause