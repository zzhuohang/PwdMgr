#!/bin/bash

echo "========================================"
echo "  密码管理系统 - 一键启动"
echo "========================================"
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 切换到后端目录
cd "$(dirname "$0")/backend"

# 检查 Java
echo -e "${YELLOW}[1/2] 检查 Java 环境...${NC}"
if ! command -v java &> /dev/null; then
    echo -e "${RED}[错误] 未找到 Java，请安装 JDK 21+${NC}"
    exit 1
fi
java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$java_version" -lt 21 ]; then
    echo -e "${RED}[错误] Java 版本过低，需要 JDK 21+，当前版本: $java_version${NC}"
    exit 1
fi
echo -e "${GREEN}[✓] Java 环境正常${NC}"

# 检查 Maven
echo -e "${YELLOW}[2/2] 检查 Maven 环境...${NC}"
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}[错误] 未找到 Maven，请安装 Maven 3.6+${NC}"
    exit 1
fi
echo -e "${GREEN}[✓] Maven 环境正常${NC}"

echo ""
echo "========================================"
echo "  启动服务（H2 嵌入式数据库）"
echo "========================================"
echo ""
echo -e "${GREEN}访问地址: http://localhost:8880${NC}"
echo -e "${GREEN}默认账号: admin / admin123${NC}"
echo -e "${GREEN}H2控制台: http://localhost:8880/h2-console${NC}"
echo ""

# 自动打开浏览器
if command -v open &> /dev/null; then
    open "http://localhost:8880"
elif command -v xdg-open &> /dev/null; then
    xdg-open "http://localhost:8880"
fi

mvn spring-boot:run

# 捕获 Ctrl+C
trap "echo -e '${YELLOW}正在停止服务...${NC}'; exit 0" INT TERM
