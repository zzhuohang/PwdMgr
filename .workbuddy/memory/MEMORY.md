# PwdMgr 项目长期记忆

## 项目概述
个人密码管理系统 — Spring Boot + Vue3 + Chrome Extension，支持浏览器自动填充。目标：打包为单 EXE，零环境依赖。

## 技术栈
- 后端：Spring Boot 3.2 + Java 21 + MyBatis-Plus + H2 嵌入式数据库
- 前端：Vue3 + Vite + Element Plus + Pinia（构建到 backend/src/main/resources/static/）
- 扩展：Chrome Extension Manifest V3
- 打包：jlink + jpackage → 单 EXE

## 目录结构
- `backend/` — Spring Boot 主项目（含 frontend/ 子目录）
- `backend/frontend/` — Vue3 前端（构建输出到 `../src/main/resources/static/`）
- `browser-extension/` — Chrome 扩展
- `build-exe.bat/sh` — 打包脚本
- `start.bat/sh` — 开发启动脚本

## 关键设计决策
1. **H2 嵌入式数据库**替代 MySQL（文件存储 `./data/pwdmgr.mv.db`，MySQL 兼容模式）
2. **无 Redis**（代码未实际使用，已移除）
3. **前端嵌入后端**（单端口 8880，无跨域问题）
4. **API 路由**：每个 Controller 类前缀 `/api/xxx`，前端 axios baseURL `/api`
5. **SPA 路由**：WebMvcConfig 将非 `/api/**`/静态资源请求回退到 index.html
6. **打包策略**：jlink 精简 JRE + jpackage 打包 EXE，失败则降级为 7z 便携包

## 用户偏好
- 中文交流，直接口语化
- 偏好详细的文件路径和代码片段
- 追求最小改动原则
- 遵循阿里开发规范
