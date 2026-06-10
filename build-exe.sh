#!/bin/bash
set -e

echo "============================================"
echo "  密码管理系统 - 一键打包"
echo "============================================"
echo ""

# 配置
PROJECT_DIR="$(cd "$(dirname "$0")/backend" && pwd)"
APP_NAME="PwdMgr"
APP_VERSION="1.0.0"
MAIN_CLASS="com.pwdmgr.PasswordManagerApplication"
MAIN_JAR="password-manager-${APP_VERSION}.jar"

# 检查 JDK
JAVA_HOME="${JAVA_HOME:-$(dirname $(dirname $(readlink -f $(which java))))}"
if [ -z "$JAVA_HOME" ] || [ ! -f "$JAVA_HOME/bin/jlink" ]; then
    echo "[错误] JDK 21+ 未找到，请设置 JAVA_HOME"
    exit 1
fi
echo "[✓] JAVA_HOME: $JAVA_HOME"
echo ""

# Step 1: 构建前端
echo "[1/5] 构建前端..."
cd "$PROJECT_DIR/frontend"
npm run build
echo "[✓] 前端构建完成"
echo ""

# Step 2: 构建后端
echo "[2/5] 构建后端 JAR..."
cd "$PROJECT_DIR"
mvn package -DskipTests -q
echo "[✓] 后端构建完成"
echo ""

# Step 3: 分析模块依赖
echo "[3/5] 分析 JDK 模块依赖..."
FAT_JAR="$PROJECT_DIR/target/$MAIN_JAR"
JDK_MODULES="$($JAVA_HOME/bin/jdeps --print-module-deps --ignore-missing-deps "$FAT_JAR" 2>/dev/null)"

if [ -z "$JDK_MODULES" ]; then
    JDK_MODULES="java.base,java.desktop,java.management,java.naming,java.net.http,java.security.jgss,java.sql,java.transaction.xa,java.xml,jdk.unsupported,java.instrument,jdk.management"
    echo "[!] jdeps 分析失败，使用默认模块列表"
else
    echo "[✓] jdeps 分析完成"
fi
echo "模块列表: $JDK_MODULES"
echo ""

# Step 4: 创建精简 JRE
echo "[4/5] 创建精简 JRE (jlink)..."
JRE_DIR="$PROJECT_DIR/target/runtime"
rm -rf "$JRE_DIR"
"$JAVA_HOME/bin/jlink" \
    --module-path "$JAVA_HOME/jmods" \
    --add-modules "$JDK_MODULES" \
    --output "$JRE_DIR" \
    --strip-debug \
    --no-header-files \
    --no-man-pages \
    --compress zip-6
echo "[✓] 精简 JRE 创建完成"
echo ""

# Step 5: 打包安装包
echo "[5/5] 打包安装包 (jpackage)..."
DIST_DIR="$PROJECT_DIR/target/dist"
rm -rf "$DIST_DIR"

"$JAVA_HOME/bin/jpackage" \
    --type app-image \
    --name "$APP_NAME" \
    --app-version "$APP_VERSION" \
    --description "个人密码管理系统" \
    --vendor "PwdMgr" \
    --input "$PROJECT_DIR/target" \
    --main-jar "$MAIN_JAR" \
    --main-class "$MAIN_CLASS" \
    --runtime-image "$JRE_DIR" \
    --dest "$DIST_DIR"

echo "[✓] 打包完成"
echo ""
echo "输出位置: $DIST_DIR/$APP_NAME"
echo "运行方式: $DIST_DIR/$APP_NAME/bin/$APP_NAME"
