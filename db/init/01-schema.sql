-- ============================================================
-- 01-schema.sql — 合并 V1 + V2 的完整建表
-- MySQL 容器 docker-entrypoint-initdb.d 挂载执行
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
    token_version INT NOT NULL DEFAULT 0 COMMENT 'token版本号',
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
    started_at DATETIME DEFAULT NULL COMMENT '任务开始执行时间',
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
    last_emotion_analysis TEXT DEFAULT NULL COMMENT '最后一次情绪分析结果(JSON)',
    last_emotion_updated_at DATETIME DEFAULT NULL COMMENT '最后一次情绪分析更新时间',
    last_emotion_msg_count INT DEFAULT 0 COMMENT '上次分析时的消息条数',
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
    category_name VARCHAR(50) DEFAULT NULL COMMENT '分类名称',
    category_code VARCHAR(50) DEFAULT NULL COMMENT '分类编码（唯一标识）',
    description VARCHAR(255) DEFAULT NULL COMMENT '分类描述',
    parent_id BIGINT UNSIGNED DEFAULT 0,
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
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
    visited_users TEXT COMMENT '已访问用户ID列表',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0草稿 1发布',
    published_at DATETIME DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_category (category_id, status, published_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 文件信息表（V2 新增）
CREATE TABLE IF NOT EXISTS `sys_file_info` (
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `original_name` VARCHAR(255)    NOT NULL                COMMENT '原始文件名',
    `file_path`     VARCHAR(500)    NOT NULL                COMMENT '访问路径',
    `file_size`     BIGINT UNSIGNED DEFAULT 0               COMMENT '文件大小（字节）',
    `file_type`     VARCHAR(20)     DEFAULT 'OTHER'         COMMENT 'IMG/PDF/TXT/DOC/XLS/OTHER',
    `business_type` VARCHAR(50)     DEFAULT NULL            COMMENT 'avatar/article_cover/attachment',
    `business_id`   VARCHAR(64)     DEFAULT NULL            COMMENT '业务对象ID',
    `business_field`VARCHAR(50)     DEFAULT NULL            COMMENT '业务字段名',
    `upload_user_id`BIGINT UNSIGNED DEFAULT NULL            COMMENT '上传用户ID',
    `is_temp`       TINYINT         DEFAULT 0               COMMENT '是否临时文件 0:否 1:是',
    `status`        TINYINT         DEFAULT 1               COMMENT '0:删除 1:正常',
    `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `expire_time`   DATETIME        DEFAULT NULL            COMMENT '过期时间（仅临时文件）',
    PRIMARY KEY (`id`),
    KEY `idx_business` (`business_type`, `business_id`),
    KEY `idx_upload_user` (`upload_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件信息表';

-- 用户收藏表（V2 新增）
CREATE TABLE IF NOT EXISTS `user_favorite` (
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `user_id`       BIGINT UNSIGNED NOT NULL                COMMENT '用户ID',
    `target_type`   VARCHAR(30)     NOT NULL                COMMENT '收藏类型：article',
    `target_id`     VARCHAR(64)     NOT NULL                COMMENT '收藏对象ID',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏表';