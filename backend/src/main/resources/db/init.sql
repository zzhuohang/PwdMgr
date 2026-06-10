-- 密码管理系统数据库初始化脚本

CREATE DATABASE IF NOT EXISTS password_manager DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE password_manager;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
    master_key_encrypted VARCHAR(255) COMMENT '主密钥加密存储',
    status TINYINT DEFAULT 1 COMMENT '状态(1:正常 0:禁用)',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志(0:未删除 1:已删除)',
    INDEX idx_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 网站表
CREATE TABLE IF NOT EXISTS website (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '网站ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    domain VARCHAR(255) NOT NULL COMMENT '网站域名',
    name VARCHAR(100) COMMENT '网站名称',
    icon_url VARCHAR(500) COMMENT '网站图标URL',
    category VARCHAR(50) COMMENT '分类',
    login_url VARCHAR(500) COMMENT '登录页面URL',
    description VARCHAR(255) COMMENT '描述',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志(0:未删除 1:已删除)',
    INDEX idx_user_id (user_id),
    INDEX idx_domain (domain),
    INDEX idx_category (category),
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='网站表';

-- 账号密码表
CREATE TABLE IF NOT EXISTS credential (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '凭证ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    website_id BIGINT NOT NULL COMMENT '网站ID',
    username VARCHAR(100) NOT NULL COMMENT '用户名(加密存储)',
    password VARCHAR(255) NOT NULL COMMENT '密码(加密存储)',
    notes TEXT COMMENT '备注',
    strength TINYINT COMMENT '密码强度(1-5)',
    last_used_time DATETIME COMMENT '最后使用时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志(0:未删除 1:已删除)',
    INDEX idx_user_id (user_id),
    INDEX idx_website_id (website_id),
    INDEX idx_username (username),
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    FOREIGN KEY (website_id) REFERENCES website(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账号密码表';

-- 登录日志表
CREATE TABLE IF NOT EXISTS login_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    login_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    login_ip VARCHAR(50) COMMENT '登录IP',
    user_agent VARCHAR(500) COMMENT '用户代理',
    status TINYINT COMMENT '状态(1:成功 0:失败)',
    message VARCHAR(255) COMMENT '消息',
    INDEX idx_user_id (user_id),
    INDEX idx_login_time (login_time),
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    operation VARCHAR(50) NOT NULL COMMENT '操作类型',
    target_type VARCHAR(50) COMMENT '目标类型',
    target_id BIGINT COMMENT '目标ID',
    detail TEXT COMMENT '操作详情',
    ip VARCHAR(50) COMMENT '操作IP',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_operation (operation),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- 插入默认用户(密码: admin123)
INSERT INTO sys_user (username, password) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi');