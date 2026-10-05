-- ============================================================
-- V1__init_schema.sql
-- 初始表结构（Flyway 版本迁移）
-- ============================================================

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    email VARCHAR(100) NOT NULL COMMENT '邮箱',
    password VARCHAR(255) NOT NULL COMMENT 'BCrypt加密密码',
    nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    gender TINYINT DEFAULT NULL COMMENT '性别 1男 2女',
    birthday DATE DEFAULT NULL COMMENT '生日',
    user_type TINYINT NOT NULL DEFAULT 0 COMMENT '角色 0普通用户 1管理员',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1正常 0禁用',
    token_version INT NOT NULL DEFAULT 0 COMMENT 'token版本号（改密/登出时+1）',
    login_fail_count INT NOT NULL DEFAULT 0 COMMENT '连续登录失败次数',
    locked_until DATETIME DEFAULT NULL COMMENT '锁定截止时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 情绪日记表
CREATE TABLE IF NOT EXISTS `emotion_diary` (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    diary_date DATE NOT NULL COMMENT '日记日期',
    mood_score TINYINT DEFAULT NULL COMMENT '心情评分1-10',
    dominant_emotion VARCHAR(50) DEFAULT NULL COMMENT '主要情绪',
    emotion_triggers TEXT COMMENT '情绪触发因素',
    diary_content TEXT COMMENT '日记内容',
    sleep_quality TINYINT DEFAULT NULL COMMENT '睡眠质量1-5',
    stress_level TINYINT DEFAULT NULL COMMENT '压力水平1-5',
    ai_emotion_analysis JSON DEFAULT NULL COMMENT 'AI情绪分析结果',
    ai_analysis_updated_at DATETIME DEFAULT NULL COMMENT 'AI分析时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_date (user_id, diary_date),
    KEY idx_user_date (user_id, diary_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='情绪日记';

-- AI分析任务表
CREATE TABLE IF NOT EXISTS `ai_analysis_task` (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    diary_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/PROCESSING/COMPLETED/FAILED',
    task_type VARCHAR(20) NOT NULL DEFAULT 'TYPE_AUTO',
    priority INT NOT NULL DEFAULT 2,
    retry_count INT NOT NULL DEFAULT 0,
    max_retry_count INT NOT NULL DEFAULT 3,
    error_message VARCHAR(1000) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    completed_at DATETIME DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_status_priority (status, priority, id),
    KEY idx_diary (diary_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 会话表
CREATE TABLE IF NOT EXISTS `consultation_session` (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    session_title VARCHAR(100) DEFAULT NULL,
    started_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_message_time DATETIME DEFAULT NULL,
    message_count INT NOT NULL DEFAULT 0,
    last_message_content TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user (user_id, started_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 消息表
CREATE TABLE IF NOT EXISTS `consultation_message` (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    session_id BIGINT UNSIGNED NOT NULL,
    sender_type TINYINT NOT NULL COMMENT '1用户 2AI',
    content TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_session (session_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 知识库分类
CREATE TABLE IF NOT EXISTS `knowledge_category` (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    parent_id BIGINT UNSIGNED DEFAULT 0,
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 知识库文章
CREATE TABLE IF NOT EXISTS `knowledge_article` (
    id VARCHAR(64) NOT NULL COMMENT 'UUID',
    category_id BIGINT UNSIGNED NOT NULL,
    title VARCHAR(200) NOT NULL,
    summary VARCHAR(500) DEFAULT NULL,
    content MEDIUMTEXT,
    cover_image VARCHAR(255) DEFAULT NULL,
    tags VARCHAR(255) DEFAULT NULL,
    author_id BIGINT UNSIGNED DEFAULT NULL,
    read_count INT NOT NULL DEFAULT 0,
    visited_users TEXT COMMENT '已访问用户ID列表（逗号分隔）',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0草稿 1发布',
    published_at DATETIME DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_category (category_id, status, published_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
