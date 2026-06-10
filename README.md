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

### 生产模式（一键打包 EXE）

```bash
# Windows（需要 JDK 21）
build-exe.bat

# 输出在 backend/target/dist/ 或 backend/target/portable/
# 用户拿到 EXE 后双击即可运行，无需安装任何环境
```

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
