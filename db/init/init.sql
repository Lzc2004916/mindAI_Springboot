-- ============================================================
-- init.sql — MySQL 容器首次启动时自动执行
-- 作用：即便 Flyway 还没跑，数据库也能先有完整表结构
-- 注意：所有 CREATE TABLE 使用 IF NOT EXISTS，与 Flyway V1/V2 不冲突
-- ============================================================
SOURCE /docker-entrypoint-initdb.d/01-schema.sql;