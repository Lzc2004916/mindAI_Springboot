-- ============================================================
-- V3__fix_missing_columns.sql
-- 修复 V1/V2 与实体定义不一致导致的全新部署 500 问题：
--   1. knowledge_category 缺 category_name / category_code / description / updated_at
--   2. consultation_session 缺 3 个情绪分析列
--   3. ai_analysis_task 缺 started_at（markProcessing 写入 / recycleStuck 查询）
-- 使用存储过程实现幂等：即使本地已手工改过列，重复执行也不会报错
-- ============================================================

DELIMITER //

CREATE PROCEDURE IF NOT EXISTS v3_add_column_if_missing(
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

-- ============================================================
-- 1. knowledge_category：补充实体所需列
-- ============================================================
CALL v3_add_column_if_missing('knowledge_category', 'category_name', "VARCHAR(50)  DEFAULT NULL COMMENT '分类名称' AFTER `name`");
CALL v3_add_column_if_missing('knowledge_category', 'category_code', "VARCHAR(50)  DEFAULT NULL COMMENT '分类编码（唯一标识）' AFTER `category_name`");
CALL v3_add_column_if_missing('knowledge_category', 'description',    "VARCHAR(255) DEFAULT NULL COMMENT '分类描述' AFTER `category_code`");
CALL v3_add_column_if_missing('knowledge_category', 'updated_at',     "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`");

-- 将旧列 name 的数据迁移到 category_name（存量数据兼容）
UPDATE `knowledge_category` SET `category_name` = `name`
WHERE `category_name` IS NULL
  AND `name` IS NOT NULL
  AND EXISTS (SELECT 1 FROM information_schema.COLUMNS
              WHERE TABLE_SCHEMA = DATABASE()
                AND TABLE_NAME = 'knowledge_category'
                AND COLUMN_NAME = 'name');

-- ============================================================
-- 2. consultation_session：补充情绪分析相关列
-- ============================================================
CALL v3_add_column_if_missing('consultation_session', 'last_emotion_analysis',   "TEXT     DEFAULT NULL COMMENT '最后一次情绪分析结果(JSON)' AFTER `status`");
CALL v3_add_column_if_missing('consultation_session', 'last_emotion_updated_at', "DATETIME DEFAULT NULL COMMENT '最后一次情绪分析更新时间' AFTER `last_emotion_analysis`");
CALL v3_add_column_if_missing('consultation_session', 'last_emotion_msg_count',  "INT      DEFAULT 0   COMMENT '上次分析时的消息条数' AFTER `last_emotion_updated_at`");

-- ============================================================
-- 3. ai_analysis_task：补充 started_at（markProcessing 写 / recycleStuck 查）
-- ============================================================
CALL v3_add_column_if_missing('ai_analysis_task', 'started_at', "DATETIME DEFAULT NULL COMMENT '任务开始执行时间' AFTER `error_message`");

-- 清理存储过程
DROP PROCEDURE IF EXISTS v3_add_column_if_missing;