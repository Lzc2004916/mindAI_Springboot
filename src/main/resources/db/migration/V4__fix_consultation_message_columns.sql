-- ============================================================
-- V4__fix_consultation_message_columns.sql
-- consultation_message 实体 8 个字段，V1 只建了 5 个
-- 补上缺失的 message_type / emotion_tag / ai_model
-- ============================================================

-- MySQL 5.7 不支持 CREATE PROCEDURE IF NOT EXISTS（仅 CREATE TABLE/DATABASE 支持），
-- 改为先删再建，保证重复执行幂等
DROP PROCEDURE IF EXISTS v4_add_column_if_missing;

DELIMITER //

CREATE PROCEDURE v4_add_column_if_missing(
    IN tbl VARCHAR(128), IN col VARCHAR(128), IN col_def VARCHAR(1024)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = tbl
          AND COLUMN_NAME = col
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', tbl, '` ADD COLUMN `', col, '` ', col_def);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //

DELIMITER ;

CALL v4_add_column_if_missing('consultation_message', 'message_type', "TINYINT DEFAULT 1 COMMENT '消息类型 1:文本' AFTER `sender_type`");
CALL v4_add_column_if_missing('consultation_message', 'emotion_tag',  "VARCHAR(50) DEFAULT NULL COMMENT '情绪标签' AFTER `content`");
CALL v4_add_column_if_missing('consultation_message', 'ai_model',     "VARCHAR(50) DEFAULT NULL COMMENT 'AI模型' AFTER `emotion_tag`");

DROP PROCEDURE IF EXISTS v4_add_column_if_missing;