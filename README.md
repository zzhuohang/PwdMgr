# 密码管理系统

Spring Boot + Vue3 个人密码管理系统，支持浏览器扩展自动填充。

## 功能特性

- 🔐 AES-256-GCM 加密存储
- 🌐 网站账号密码管理（分类、搜索）
- 🧩 Chrome 浏览器扩展 — 自动检测登录页、一键填充
- 🎲 密码生成器 + 强度检测
- 📦 **零环境依赖** — H2 嵌入式数据库，无需安装 MySQL/Redis

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot 3.2 + Java 21 + MyBatis-Plus |
| 数据库 | **H2 嵌入式**（文件存储，无需安装） |
| 认证 | Spring Security + JWT |
| 前端 | Vue3 + Vite + Element Plus + Pinia |
| 扩展 | Chrome Extension Manifest V3 |
| 打包 | jlink + jpackage → **单 EXE** |

## 项目结构

```
PwdMgr/
├── backend/                    # Spring Boot（含前端）
│   ├── frontend/               # Vue3 前端（构建后输出到 static/）
│   ├── src/main/java/com/pwdmgr/
│   │   ├── config/             # 配置类
│   │   ├── controller/         # API 控制器
│   │   ├── service/            # 服务层
│   │   ├── repository/         # MyBatis Mapper
│   │   ├── entity/             # 实体类
│   │   ├── dto/                # 传输对象
│   │   ├── util/               # 工具类（JWT、加密）
│   │   └── exception/          # 异常处理
│   └── pom.xml
├── browser-extension/          # Chrome 扩展
├── build-exe.bat               # Windows 一键打包 EXE
├── build-exe.sh                # Linux/Mac 打包脚本
├── start.bat / start.sh        # 开发模式启动
└── DESIGN.md
```

## 快速开始

### 开发模式（需要有 Java + Maven）

```bash
# Windows 双击 start.bat
# Linux/Mac
./start.sh
```

访问 http://localhost:8880 | 默认账号: `admin` / `admin123`

### 生产模式（一键打包）

```bash
# Windows（需要 JDK 21）
build-exe.bat
```

打包流程：
1. `npm run build` → 前端输出到 `backend/src/main/resources/static/`
2. `mvn package` → 后端打成可执行 JAR
3. `jlink` → 裁剪精简 JRE（~40MB）
4. `jpackage` → 打包 `.exe` 安装包（需要 [WiX Toolset 3.x](https://wixtoolset.org/)）
5. 如果 jpackage 失败 → 自动降级为**便携包**（解压即用）

#### 输出位置

| 方案 | 路径 | 使用方式 |
|------|------|----------|
| EXE 安装包 | `backend/target/dist/PwdMgr-1.0.0.exe` | 双击安装 |
| 便携包 | `backend/target/portable/PwdMgr/` | 整个文件夹发给别人，双击 `start.bat` 即可 |

#### 便携包分发

便携包是一个**自包含**的文件夹，内含：

```
PwdMgr/
├── start.bat       # 双击启动（自动打开浏览器）
├── PwdMgr.jar      # 后端程序（前端已内嵌）
├── jre/            # 精简 JRE（无需安装 Java）
└── data/           # 数据库文件（自动创建）
```

**⚠️ `start.bat` 不能单独拿出来用**，它依赖同目录的 `jre/` 和 `PwdMgr.jar`。分发时必须把整个 `PwdMgr/` 文件夹打包（zip 等），对方解压后双击 `start.bat` 即可运行，无需安装任何环境。

#### 环境要求

| 场景 | 需要安装 |
|------|----------|
| 开发模式 | JDK 21 + Maven + Node.js |
| 运行便携包 | **无需任何环境** |
| 运行 EXE 安装包 | **无需任何环境** |
| 重新打包 | JDK 21 + Maven + Node.js（WiX 可选） |

## 浏览器扩展安装

1. Chrome → `chrome://extensions/`
2. 开启「开发者模式」
3. 「加载已解压的扩展程序」→ 选择 `browser-extension/` 目录

## 使用说明

1. **注册/登录** — 首次使用需注册（设置主密码用于加密数据）
2. **添加网站** — 在「网站管理」添加域名
3. **添加账号** — 在「账号密码」录入账号密码（支持密码生成器）
4. **浏览器填充** — 访问已保存的登录页，扩展自动弹出填充提示

## 安全

- 存储密码使用 AES-256-GCM 加密
- 主密码不存储，每次使用时派生加密密钥
- JWT 认证，Token 有效期 2 小时

## 许可证

MIT
