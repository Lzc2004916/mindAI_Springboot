-- ============================================================
-- V2__add_missing_tables.sql
-- 补充 V1 遗漏的表：sys_file_info / user_favorite
-- ============================================================

-- 文件信息表（SysFileInfo 实体对应）
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

-- 用户收藏表
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