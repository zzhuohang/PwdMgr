# 个人密码管理系统

一个基于 Spring Boot + Vue3 的个人密码管理系统，支持浏览器扩展实时监控和自动填充。

## 功能特性

- 🔐 安全的密码存储（AES-256-GCM加密）
- 🌐 网站账号密码管理
- 🔍 快速搜索和分类
- 🧩 浏览器扩展支持
  - 自动检测登录页面
  - 一键填充账号密码
  - 实时监控网站状态
- 🎲 密码生成器
- 📊 密码强度分析
- 📦 数据导入导出

## 技术栈

### 后端
- Java 21
- Spring Boot 3.2
- Spring Security + JWT
- MyBatis-Plus
- MySQL 8.0
- Redis

### 前端
- Vue 3
- Vite
- Element Plus
- Pinia
- Vue Router

### 浏览器扩展
- Chrome Extension (Manifest V3)
- Content Script
- Background Service Worker

## 项目结构

```
PwdMgr/
├── backend/                # Spring Boot后端
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/pwdmgr/
│   │   │   │       ├── config/      # 配置类
│   │   │   │       ├── controller/  # 控制器
│   │   │   │       ├── service/     # 服务层
│   │   │   │       ├── repository/  # 数据访问层
│   │   │   │       ├── entity/      # 实体类
│   │   │   │       ├── dto/         # 数据传输对象
│   │   │   │       ├── util/        # 工具类
│   │   │   │       └── exception/   # 异常处理
│   │   │   └── resources/
│   │   │       ├── application.yml  # 配置文件
│   │   │       └── db/              # 数据库脚本
│   │   └── test/
│   └── pom.xml
├── frontend/               # Vue3前端
│   ├── src/
│   │   ├── api/            # API调用
│   │   ├── components/     # 组件
│   │   ├── views/          # 页面
│   │   ├── stores/         # 状态管理
│   │   ├── router/         # 路由
│   │   └── utils/          # 工具函数
│   ├── package.json
│   └── vite.config.js
├── browser-extension/      # 浏览器扩展
│   ├── manifest.json
│   ├── background.js
│   ├── content.js
│   ├── content.css
│   ├── popup/
│   └── icons/
├── DESIGN.md               # 设计文档
└── README.md               # 项目说明
```

## 快速开始

### 环境要求

- JDK 21+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.6+

### 1. 数据库准备

```bash
# 登录MySQL
mysql -u root -p

# 执行初始化脚本
source backend/src/main/resources/db/init.sql
```

### 2. 启动后端

```bash
# 进入后端目录
cd backend

# 修改配置文件
# 编辑 src/main/resources/application.yml
# 配置数据库连接、Redis连接等

# 启动应用
mvn spring-boot:run
```

后端将在 http://localhost:8080 启动

### 3. 启动前端

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端将在 http://localhost:5173 启动

### 4. 安装浏览器扩展

1. 打开 Chrome 浏览器，访问 `chrome://extensions/`
2. 开启右上角的「开发者模式」
3. 点击「加载已解压的扩展程序」
4. 选择 `browser-extension` 目录

## 配置说明

### 后端配置

编辑 `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/password_manager?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password

jwt:
  secret: your-256-bit-secret-key-here-make-it-long-enough
```

### 前端配置

编辑 `frontend/.env.development`:

```properties
VITE_API_BASE_URL=http://localhost:8080/api
```

## 使用说明

### 1. 注册账号

首次使用需要注册账号，注册时需要设置：
- 用户名
- 登录密码
- 主密码（用于加密数据，请牢记！）

### 2. 添加网站

在「网站管理」页面添加需要存储账号的网站：
- 网站名称
- 域名（如 github.com）
- 分类（可选）

### 3. 添加账号密码

在「账号密码」页面添加账号：
- 选择网站
- 输入用户名
- 输入密码（可使用密码生成器）

### 4. 使用浏览器扩展

安装扩展后：
1. 访问已保存账号的网站登录页
2. 扩展会自动检测并显示提示
3. 点击「一键填充」自动填写账号密码

## 安全说明

- 所有密码使用 AES-256-GCM 加密存储
- 主密码不会存储在服务器上
- 使用 JWT 进行身份认证
- 支持自动锁定功能

## 开发规范

本项目遵循阿里巴巴 Java 开发规范：

- 类名：UpperCamelCase
- 方法名：lowerCamelCase
- 常量：UPPER_SNAKE_CASE
- 包名：全小写

## 许可证

MIT License