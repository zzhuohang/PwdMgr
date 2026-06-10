# 个人密码管理系统 - 设计文档

## 系统概述
基于Spring Boot + Vue3的个人密码管理系统，支持浏览器扩展实时监控和自动填充。

## 技术栈
- **后端**: Spring Boot 3.2 + Java 21 + H2 嵌入式数据库 + JWT + AES加密
- **前端**: Vue3 + Vite + Element Plus + Pinia（构建后嵌入后端 static 目录）
- **浏览器扩展**: Chrome Extension (Manifest V3)
- **打包**: jlink + jpackage → 单 EXE 分发（自带 JRE + 数据库，零环境依赖）

## 架构设计

### 1. 后端架构
```
backend/
├── src/main/java/com/pwdmgr/
│   ├── config/          # 配置类
│   ├── controller/      # 控制器
│   ├── service/         # 服务层
│   ├── repository/      # 数据访问层
│   ├── entity/          # 实体类
│   ├── dto/             # 数据传输对象
│   ├── util/            # 工具类
│   └── exception/       # 异常处理
```

### 2. 前端架构
```
frontend/
├── src/
│   ├── api/             # API调用
│   ├── components/      # 组件
│   ├── views/           # 页面
│   ├── stores/          # Pinia状态管理
│   ├── utils/           # 工具函数
│   └── router/          # 路由
```

### 3. 浏览器扩展
```
browser-extension/
├── manifest.json        # 扩展配置
├── background.js        # 后台脚本
├── content.js           # 内容脚本
├── popup/               # 弹出窗口
└── icons/               # 图标
```

## 数据库设计

### 用户表 (sys_user)
```sql
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL, -- BCrypt加密
    master_key_encrypted VARCHAR(255), -- 主密钥加密存储
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 网站表 (website)
```sql
CREATE TABLE website (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    domain VARCHAR(255) NOT NULL, -- 网站域名
    name VARCHAR(100), -- 网站名称
    icon_url VARCHAR(500), -- 网站图标
    category VARCHAR(50), -- 分类
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
);
```

### 账号密码表 (credential)
```sql
CREATE TABLE credential (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    website_id BIGINT NOT NULL,
    username VARCHAR(100) NOT NULL, -- 加密存储
    password VARCHAR(255) NOT NULL, -- 加密存储
    notes TEXT, -- 备注
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    FOREIGN KEY (website_id) REFERENCES website(id)
);
```

## 安全设计

### 1. 密码加密
- 用户密码：BCrypt加密
- 存储的账号密码：AES-256-GCM加密
- 主密钥：用户主密码派生，不存储

### 2. 认证授权
- JWT Token认证
- Token有效期：2小时
- 刷新Token机制

### 3. 数据传输
- 全站HTTPS
- API请求签名

## 核心功能

### 1. 密码管理
- 添加/编辑/删除账号密码
- 分类管理
- 搜索功能
- 密码生成器

### 2. 浏览器监控
- 监控当前标签页URL
- 检测登录页面（通过URL模式和页面内容）
- 匹配已存储的账号密码
- 弹出提示框显示账号密码

### 3. 自动填充
- 一键填充账号密码表单
- 支持常见登录页面结构

## 阿里云开发规范

### 1. 代码规范
- 类名：UpperCamelCase
- 方法名：lowerCamelCase
- 常量：UPPER_SNAKE_CASE
- 包名：全小写

### 2. 注释规范
- 类注释：作者、日期、功能描述
- 方法注释：参数、返回值、异常
- 关键逻辑注释

### 3. 异常处理
- 统一异常处理
- 业务异常继承RuntimeException
- 异常码规范

### 4. 日志规范
- 使用SLF4J
- 日志级别规范
- 日志格式规范

## 优化建议

### 1. 安全性优化
- 实现密码强度检测
- 支持双因素认证
- 登录异常检测

### 2. 用户体验优化
- 密码分类标签
- 批量导入导出
- 密码历史记录
- 自动锁定功能

### 3. 性能优化
- 数据库索引优化
- 缓存机制
- 懒加载

### 4. 扩展功能
- 多设备同步
- 密码共享（家庭/团队）
- 密码过期提醒
- 安全审计日志