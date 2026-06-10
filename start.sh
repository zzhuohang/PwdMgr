#!/bin/bash

echo "========================================"
echo "  个人密码管理系统 - 启动脚本"
echo "========================================"
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查Java
echo -e "${YELLOW}[1/4] 检查Java环境...${NC}"
if ! command -v java &> /dev/null; then
    echo -e "${RED}[错误] 未找到Java，请安装JDK 21+${NC}"
    exit 1
fi
java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$java_version" -lt 21 ]; then
    echo -e "${RED}[错误] Java版本过低，需要JDK 21+，当前版本: $java_version${NC}"
    exit 1
fi
echo -e "${GREEN}[✓] Java环境正常${NC}"

# 检查Node.js
echo -e "${YELLOW}[2/4] 检查Node.js环境...${NC}"
if ! command -v node &> /dev/null; then
    echo -e "${RED}[错误] 未找到Node.js，请安装Node.js 18+${NC}"
    exit 1
fi
node_version=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
if [ "$node_version" -lt 18 ]; then
    echo -e "${RED}[错误] Node.js版本过低，需要18+，当前版本: $node_version${NC}"
    exit 1
fi
echo -e "${GREEN}[✓] Node.js环境正常${NC}"

# 检查Maven
echo -e "${YELLOW}[3/4] 检查Maven环境...${NC}"
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}[错误] 未找到Maven，请安装Maven 3.6+${NC}"
    exit 1
fi
echo -e "${GREEN}[✓] Maven环境正常${NC}"

# 检查MySQL
echo -e "${YELLOW}[4/4] 检查MySQL连接...${NC}"
if command -v mysql &> /dev/null; then
    if mysql -u root -p -e "SELECT 1;" &> /dev/null; then
        echo -e "${GREEN}[✓] MySQL连接正常${NC}"
    else
        echo -e "${YELLOW}[警告] 无法连接到MySQL，请确保MySQL已启动并配置正确${NC}"
        echo -e "${YELLOW}[提示] 请先执行数据库初始化脚本: backend/src/main/resources/db/init.sql${NC}"
    fi
else
    echo -e "${YELLOW}[警告] 未找到MySQL客户端${NC}"
fi

echo ""
echo "========================================"
echo "  启动服务"
echo "========================================"
echo ""

# 启动后端
echo -e "${YELLOW}[启动] 后端服务...${NC}"
cd backend
mvn spring-boot:run &
BACKEND_PID=$!
cd ..

# 等待后端启动
echo -e "${YELLOW}[等待] 后端服务启动中（约30秒）...${NC}"
sleep 30

# 启动前端
echo -e "${YELLOW}[启动] 前端服务...${NC}"
cd frontend
npm install > /dev/null 2>&1
npm run dev &
FRONTEND_PID=$!
cd ..

echo ""
echo "========================================"
echo "  启动完成"
echo "========================================"
echo ""
echo -e "${GREEN}后端地址: http://localhost:8080${NC}"
echo -e "${GREEN}前端地址: http://localhost:5173${NC}"
echo ""
echo -e "${YELLOW}[提示] 浏览器扩展安装方法:${NC}"
echo "  1. 打开Chrome浏览器，访问 chrome://extensions/"
echo "  2. 开启「开发者模式」"
echo "  3. 点击「加载已解压的扩展程序」"
echo "  4. 选择 browser-extension 目录"
echo ""
echo -e "${YELLOW}按 Ctrl+C 停止所有服务${NC}"

# 捕获退出信号
trap "echo -e '${YELLOW}正在停止服务...${NC}'; kill $BACKEND_PID $FRONTEND_PID 2>/dev/null; exit 0" INT TERM

# 等待进程结束
wait