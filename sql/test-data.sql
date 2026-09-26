-- =============================================================================
--  mindAI 测试数据脚本
--  作用：① 清空所有表的业务数据  ② 灌入一套完整、互相引用正确的测试数据
--  库名：mental_health_assistant
--
--  ⚠️ 执行前请先备份（清空不可逆）：
--     mysqldump -uroot -proot --databases mental_health_assistant > backup.sql
--
--  执行方式（任选）：
--     mysql -uroot -proot -D mental_health_assistant < sql/test-data.sql
--     或在 Navicat / DataGrip 里整段运行
--
--  脚本是**可重复执行**的：每次都先清空再插入，日期用 CURDATE() / DATE_SUB(NOW(), ...)
--  相对表达，所以任何时候跑，趋势图都有"最近 7 天"的数据。
--
--  ⚠️ 生产环境警告：本文件里的账号密码是**公开的弱密码**（admin123 / 123456），
--     只用于本地测试。绝对不要把它跑在生产库上，也不要照这个方式建正式管理员账号。
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 测试账号（密码是 BCrypt 哈希，已用项目自身的 BCryptPasswordEncoder 生成并校验过）
-- -----------------------------------------------------------------------------
--  用户名          密码        角色      状态       用途
--  admin          admin123   管理员    正常       管理端全部功能（user_type=2）
--  zhangsan       123456     普通用户  正常       数据最多，主测试账号
--  lisi           123456     普通用户  正常       次测试账号
--  wangwu         123456     普通用户  正常       有会话、有日记
--  zhaoliu        123456     普通用户  正常       昵称 NULL，测"昵称回退用户名"
--  banned_user    123456     普通用户  **禁用**   测"账号已被禁用"分支
-- -----------------------------------------------------------------------------

SET NAMES utf8mb4;

-- =============================================================================
-- 第一步：清空所有表
--   InnoDB 有外键约束，直接 TRUNCATE 子表会被拒（父表还被引用），
--   所以先临时关掉外键检查，清完再打开。
--   TRUNCATE 比 DELETE 快，而且会把 AUTO_INCREMENT 重置为 1 —— 这正是我们要的
--   （下面所有 INSERT 都写死了 id，保证每次跑出来的数据完全一致）。
-- =============================================================================
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE user_favorite;
TRUNCATE TABLE ai_analysis_task;
TRUNCATE TABLE consultation_message;
TRUNCATE TABLE consultation_session;
TRUNCATE TABLE emotion_diary;
TRUNCATE TABLE knowledge_article;
TRUNCATE TABLE knowledge_category;
TRUNCATE TABLE sys_file_info;
TRUNCATE TABLE user;

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
-- 第二步：灌入测试数据
--   插入顺序必须"父表在前"，否则外键报错：
--   user → knowledge_category → knowledge_article → consultation_session
--        → consultation_message → emotion_diary → ai_analysis_task
--        → sys_file_info → user_favorite
-- =============================================================================

-- ---------- 1. 用户表 --------------------------------------------------------
-- 三处刻意设计：
--   · id=1 是管理员（user_type=2），管理端接口靠它测试
--   · id=5 昵称是 NULL，用于验证"昵称回退显示用户名"（User.getDisplayName）
--   · id=6 status=0，用于验证"账号被禁用"的登录分支
INSERT INTO `user`
  (id, username, email, phone, password, nickname, avatar, gender, birthday, user_type, status, created_at, updated_at)
VALUES
  (1, 'admin',       'admin@mindai.com',  '13800000001', '$2a$10$Jp96DoYN.bmcZKeHwG0T8.4xiS1B2Sma72xEztmkkaQV4xfwYkgau', '系统管理员', NULL, 1, '1990-01-01', 2, 1, DATE_SUB(NOW(), INTERVAL 60 DAY), NOW()),
  (2, 'zhangsan',    'zhangsan@test.com', '13800000002', '$2a$10$fQFw/x4X3jJLU7ml3R5/tO3ONsI22/i7Az7TMIMpR.AHwFU9X7.vW', '张三', '/files/2026-09/a56f4b34-3059-46ab-a62c-fd5cfcd30d96.png', 1, '2005-03-15', 1, 1, DATE_SUB(NOW(), INTERVAL 45 DAY), NOW()),
  (3, 'lisi',        'lisi@test.com',     '13800000003', '$2a$10$otnNbLb1VNNv.3t7VOnrhudIfzer21Z/3MPvL1LyUi8XbfDULOsra', '李四', NULL, 2, '2004-08-22', 1, 1, DATE_SUB(NOW(), INTERVAL 40 DAY), NOW()),
  (4, 'wangwu',      'wangwu@test.com',   '13800000004', '$2a$10$fQFw/x4X3jJLU7ml3R5/tO3ONsI22/i7Az7TMIMpR.AHwFU9X7.vW', '王五', NULL, 1, '2006-11-08', 1, 1, DATE_SUB(NOW(), INTERVAL 30 DAY), NOW()),
  (5, 'zhaoliu',     'zhaoliu@test.com',  '13800000005', '$2a$10$otnNbLb1VNNv.3t7VOnrhudIfzer21Z/3MPvL1LyUi8XbfDULOsra', NULL, NULL, 2, '2005-06-30', 1, 1, DATE_SUB(NOW(), INTERVAL 20 DAY), NOW()),
  (6, 'banned_user', 'banned@test.com',   NULL,          '$2a$10$fQFw/x4X3jJLU7ml3R5/tO3ONsI22/i7Az7TMIMpR.AHwFU9X7.vW', '已禁用账号', NULL, 1, NULL, 1, 0, DATE_SUB(NOW(), INTERVAL 15 DAY), NOW());

-- ---------- 2. 知识库分类 ----------------------------------------------------
-- 两级树：1 → 2/3，4 → 5
-- id=7 的 status=0，用于验证"分类列表只返回启用的分类"
INSERT INTO knowledge_category
  (id, parent_id, category_name, category_code, description, sort_order, status, created_at, updated_at)
VALUES
  (1, 0, '情绪管理',     'emotion_manage',    '识别、理解与调节自己的情绪',     1, 1, DATE_SUB(NOW(), INTERVAL 50 DAY), NOW()),
  (2, 1, '焦虑缓解',     'anxiety_relief',    '焦虑的识别与自助缓解方法',       1, 1, DATE_SUB(NOW(), INTERVAL 50 DAY), NOW()),
  (3, 1, '情绪识别',     'emotion_awareness', '学会给情绪命名，是调节的第一步', 2, 1, DATE_SUB(NOW(), INTERVAL 50 DAY), NOW()),
  (4, 0, '亲子沟通',     'parent_child',      '与孩子建立有效沟通的方式',       2, 1, DATE_SUB(NOW(), INTERVAL 48 DAY), NOW()),
  (5, 4, '青春期心理',   'puberty',           '青春期常见的心理困扰与应对',     1, 1, DATE_SUB(NOW(), INTERVAL 48 DAY), NOW()),
  (6, 0, '睡眠健康',     'sleep_health',      '改善睡眠质量、重建作息节律',     3, 1, DATE_SUB(NOW(), INTERVAL 46 DAY), NOW()),
  (7, 0, '停用分类测试', 'deprecated_test',   '状态为禁用，分类接口不应返回它', 9, 0, DATE_SUB(NOW(), INTERVAL 45 DAY), NOW());

-- ---------- 3. 知识文章 ------------------------------------------------------
-- id 以 ...009 结尾的那篇 status=0 是草稿：管理员能看到，普通用户应看不到
--   → 用于验证 KnowledgeArticleService 的 skipPublishFilter
-- author_id 分别是 1(admin) / 2(zhangsan)，用于验证作者联表
INSERT INTO knowledge_article
  (id, category_id, title, summary, content, cover_image, tags, author_id, read_count, status, published_at, created_at, updated_at)
VALUES
  ('a1000000-0000-4000-8000-000000000001', 2, '焦虑发作时，身体到底发生了什么',
   '心跳加快、手心出汗、呼吸变浅——这些不是「你太脆弱」，而是身体的应激反应。',
   '焦虑发作时，交感神经被激活，肾上腺素和皮质醇被释放出来。这套机制在远古时代用来应对猛兽，现在则被「明天要交的报告」触发。理解它，是缓解它的第一步：你感受到的是真实的生理变化，不是想象出来的。',
   NULL, '焦虑,躯体化,科普', 1, 128, 1, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY), NOW()),

  ('a1000000-0000-4000-8000-000000000002', 2, '三个可以立刻做的焦虑缓解练习',
   '不需要任何工具，坐在椅子上就能做。',
   '一、4-7-8 呼吸法：吸气 4 秒，屏息 7 秒，呼气 8 秒，重复 4 轮。二、5-4-3-2-1 感官着陆：说出你看到的 5 样东西、摸到的 4 样、听到的 3 种声音、闻到的 2 种气味、尝到的 1 个味道。三、写下最坏结果：把「万一」写下来，通常会发现问题比想象中小。',
   NULL, '焦虑,自助,练习', 1, 86, 1, DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY), NOW()),

  ('a1000000-0000-4000-8000-000000000003', 3, '情绪没有好坏，只有信息',
   '把「我不该难过」换成「我为什么难过」。',
   '我们从小被教育「要开心」，于是负面情绪成了需要隐藏的东西。但情绪本身是一种反馈机制：焦虑提示有不确定的风险，愤怒提示边界被侵犯，悲伤提示失去了重要的东西。与其压抑它，不如读一读它在说什么。',
   NULL, '情绪识别,认知', 1, 64, 1, DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY), NOW()),

  ('a1000000-0000-4000-8000-000000000004', 5, '孩子说「我不想上学」，家长的第一句话很关键',
   '先接住情绪，再处理事情。',
   '「你不想上学？你知道不上学的后果吗？」——这句话一出口，对话就结束了。换一种：「听起来学校有什么事让你很难受，能和我说说吗？」先让孩子感觉「说了也不会被骂」，他才会说真话。',
   NULL, '青春期,亲子,沟通', 1, 210, 1, DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 12 DAY), NOW()),

  ('a1000000-0000-4000-8000-000000000005', 5, '青春期孩子的沉默，不是对抗',
   '他不是不想理你，是在练习「成为自己」。',
   '青春期大脑的前额叶（负责理性决策）还没发育完全，而情绪中枢（杏仁核）异常活跃。所以这个年龄段的孩子容易情绪化、容易沉默。给他留空间，但让他知道你一直在。',
   NULL, '青春期,发展心理学', 2, 97, 1, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), NOW()),

  ('a1000000-0000-4000-8000-000000000006', 6, '睡前 30 分钟：把大脑交给睡眠',
   '睡不着的时候，越努力越清醒。',
   '睡眠不是「努力」就能做到的。睡前 30 分钟做三件事：调暗灯光、把手机放到床外、写下明天要做的三件事（把大脑的待办清空）。',
   NULL, '睡眠,作息', 1, 153, 1, DATE_SUB(NOW(), INTERVAL 9 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY), NOW()),

  ('a1000000-0000-4000-8000-000000000007', 1, '情绪日记怎么写才有用',
   '不是流水账，而是记录「事件—想法—情绪」的链条。',
   '写情绪日记时，试着分成三栏：发生了什么、我当时脑子里想的是什么、我的情绪是什么。有用的不是「今天很烦」，而是「今天被老师点名（事件）→ 我觉得自己很蠢（想法）→ 羞耻 + 焦虑（情绪）」。',
   NULL, '情绪管理,日记,方法', 2, 45, 1, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), NOW()),

  ('a1000000-0000-4000-8000-000000000008', 4, '一次失败的沟通复盘',
   '我越讲道理，孩子越不说话。',
   '记录一次真实的对话失败：我准备了 40 分钟的道理，讲了 3 分钟，孩子回房间关上了门。复盘下来最大的问题是：我全程在输出，没有问过一个「你怎么想」。',
   NULL, '亲子,复盘', 1, 31, 1, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), NOW()),

  ('a1000000-0000-4000-8000-000000000009', 1, '【草稿】尚未发布的测试文章',
   '这篇文章 status=0，用于验证：普通用户查不到草稿，管理员能查到。',
   '草稿内容。普通用户调文章列表 / 详情时不应看到这一篇。',
   NULL, '草稿,测试', 1, 0, 0, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), NOW());

-- ---------- 4. 咨询会话 ------------------------------------------------------
-- id=1 预置了情绪分析结果，且 last_emotion_msg_count = 6，和它实际的 6 条消息一致
--      → 用于验证「时间 + 消息条数」双判据缓存：消息没变时应直接命中缓存、不再调 AI
--        （想验证"缓存失效重算"，把会话 1 的消息再加一条即可）
-- id=2 / 4 / 5 没有分析结果 → 用于验证首次分析
-- id=5 标题为 NULL → 用于验证空标题的展示
--
-- 注意：last_emotion_analysis 是 **JSON 类型列**，写进去的必须是合法 JSON，
--      所以下面 JSON 里一律用全角引号「」，不用半角双引号（否则 JSON 会断掉、MySQL 直接报错）。
--      timestamp 字段留到后面用 JSON_SET 补，方便写成"相对当前时间"。
INSERT INTO consultation_session
  (id, user_id, session_title, started_at, last_emotion_analysis, last_emotion_updated_at, last_emotion_msg_count)
VALUES
  (1, 2, '学业压力让我喘不过气', DATE_SUB(NOW(), INTERVAL 3 DAY),
   '{"primaryEmotion":"焦虑","emotionScore":72,"isNegative":true,"riskLevel":1,"keywords":["学业","考试","失眠","自我否定"],"suggestion":"你正在用很高的标准要求自己，可以先从一件最小的事开始，重新获得掌控感。","icon":"😰","label":"anxious","riskDescription":"存在持续的学习压力与轻度睡眠困扰，建议关注作息。","improvementSuggestions":["把任务拆成 25 分钟一段，完成一段就休息 5 分钟","每天固定时间上床，睡前 30 分钟离开手机","把担心的事写下来，区分哪些是现在能做的"]}',
   DATE_SUB(NOW(), INTERVAL 3 DAY), 6),

  (2, 2, '和室友的关系困扰', DATE_SUB(NOW(), INTERVAL 1 DAY),
   NULL, NULL, 0),

  (3, 3, '最近总是提不起劲', DATE_SUB(NOW(), INTERVAL 2 DAY),
   '{"primaryEmotion":"沮丧","emotionScore":58,"isNegative":true,"riskLevel":1,"keywords":["疲惫","兴趣减退","社交回避"],"suggestion":"情绪低落时不必强迫自己积极，先保证睡眠和规律饮食，这两件事本身就在托住你。","icon":"😔","label":"dispirited","riskDescription":"近期动力与兴趣下降，建议持续观察，必要时寻求专业支持。","improvementSuggestions":["每天出门走 20 分钟，不需要有目的","把想做的事降到最小：只看一页书也算完成"]}',
   DATE_SUB(NOW(), INTERVAL 2 DAY), 4),

  (4, 4, '考试前的失眠', DATE_SUB(NOW(), INTERVAL 6 HOUR),
   NULL, NULL, 0),

  (5, 5, NULL, DATE_SUB(NOW(), INTERVAL 10 DAY),
   NULL, NULL, 0);

-- 补上两个 JSON 里的 timestamp（毫秒时间戳，由后端填，不是 AI 生成）
-- 用 JSON_SET 而不是直接写字符串，好处：① 能写成相对时间 ② 顺带验证上面那段 JSON 是合法的
UPDATE consultation_session
   SET last_emotion_analysis = JSON_SET(last_emotion_analysis, '$.timestamp', UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 3 DAY)) * 1000)
 WHERE id = 1;
UPDATE consultation_session
   SET last_emotion_analysis = JSON_SET(last_emotion_analysis, '$.timestamp', UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 2 DAY)) * 1000)
 WHERE id = 3;

-- ---------- 5. 咨询消息 ------------------------------------------------------
-- 会话 1 = 6 条、会话 3 = 4 条，与它们 last_emotion_msg_count 一致（缓存命中测试用）
-- sender_type：1=用户，2=AI 助手
INSERT INTO consultation_message
  (id, session_id, sender_type, message_type, content, emotion_tag, ai_model, created_at)
VALUES
  -- 会话 1（用户 2，共 6 条）
  (1,  1, 1, 1, '最近作业和考试堆在一起，我每天都睡不好。', '焦虑', NULL, DATE_SUB(NOW(), INTERVAL 3 DAY)),
  (2,  1, 2, 1, '听起来你这段时间压力很大，既要应付作业又要准备考试。这种「事情堆在一起」的感觉确实很消耗人。你方便说说，最让你睡不着的具体是哪件事吗？', NULL, 'deepseek-ai/DeepSeek-V3.2', DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 10 SECOND),
  (3,  1, 1, 1, '就是怕考砸，然后辜负家里人。', '焦虑', NULL, DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 30 SECOND),
  (4,  1, 2, 1, '「怕辜负家里人」——你对自己的要求里，好像装着很多别人的期待。你有没有想过，如果这次真的没考好，最坏会发生什么？', NULL, 'deepseek-ai/DeepSeek-V3.2', DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 45 SECOND),
  (5,  1, 1, 1, '可能他们会失望，但也还好吧……好像没我想的那么严重。', '平静', NULL, DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 60 SECOND),
  (6,  1, 2, 1, '你自己说出了「没我想的那么严重」，这很关键。焦虑常常把后果放大。今晚可以试试把明天要做的三件事写下来，写完就合上本子——让大脑知道清单已经在了。', NULL, 'deepseek-ai/DeepSeek-V3.2', DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 80 SECOND),

  -- 会话 2（用户 2，2 条，未分析过）
  (7,  2, 1, 1, '我和室友最近有点别扭，不知道该不该主动说话。', '压力', NULL, DATE_SUB(NOW(), INTERVAL 1 DAY)),
  (8,  2, 2, 1, '关系里出现别扭是很常见的。能说说具体是哪件事让你觉得不舒服吗？', NULL, 'deepseek-ai/DeepSeek-V3.2', DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 12 SECOND),

  -- 会话 3（用户 3，共 4 条）
  (9,  3, 1, 1, '什么都不想做，以前喜欢的事现在也没兴趣了。', '沮丧', NULL, DATE_SUB(NOW(), INTERVAL 2 DAY)),
  (10, 3, 2, 1, '听起来你已经有一段时间没有「想做什么」的感觉了，这确实很累。这种状态持续多久了？', NULL, 'deepseek-ai/DeepSeek-V3.2', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 15 SECOND),
  (11, 3, 1, 1, '大概两三周了吧，也说不上发生什么事。', '低落', NULL, DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 40 SECOND),
  (12, 3, 2, 1, '两三周、又没有明确的诱因，这种「提不起劲」值得认真对待。今天先不要求自己振作，只做一件小事：出门走 20 分钟。如果这种状态持续到一个月以上，建议找学校的心理老师聊一次。', NULL, 'deepseek-ai/DeepSeek-V3.2', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 70 SECOND),

  -- 会话 4（用户 4，1 条，刚开）
  (13, 4, 1, 1, '一想到考试就睡不着，躺在床上脑子停不下来。', '焦虑', NULL, DATE_SUB(NOW(), INTERVAL 6 HOUR)),

  -- 会话 5（用户 5，1 条）
  (14, 5, 1, 1, '我就随便问问，没什么事。', NULL, NULL, DATE_SUB(NOW(), INTERVAL 10 DAY));

-- ---------- 6. 情绪日记 ------------------------------------------------------
-- 唯一键是 (user_id, diary_date)，所以同一用户同一天只能有一条。
-- 用户 2 覆盖完整近 7 天 → /api/data-analytics/overview 的 emotionTrend 一定有 7 个点。
-- id=1/2/3/14 填了 ai_emotion_analysis，其余留空
--   → 管理端「详情弹窗有 / 无 AI 卡片」两种展示都能测到。
INSERT INTO emotion_diary
  (id, user_id, diary_date, mood_score, dominant_emotion, emotion_triggers, diary_content, sleep_quality, stress_level, ai_emotion_analysis, ai_analysis_updated_at, created_at, updated_at)
VALUES
  -- 用户 2：近 7 天（趋势图的完整一周）
  (1, 2, CURDATE(), 9, '开心', '和朋友一起复习，效率很高', '今天状态很好，把拖了很久的作业写完了，晚上还去操场跑了三圈。', 5, 1,
   '{"primaryEmotion":"开心","emotionScore":18,"riskLevel":0,"isNegative":false,"suggestion":"保持这样的节奏，你正在把掌控感一点点拿回来。","riskDescription":"情绪状态良好，未发现风险信号。","improvementSuggestions":["把今天有效的方法记下来，状态差的时候可以照做","继续保持运动的习惯"]}',
   DATE_SUB(NOW(), INTERVAL 2 HOUR), CURDATE(), NOW()),

  (2, 2, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 6, '焦虑', '临近考试，复习进度慢', '有点慌，但还是坐下来学了两小时，算是没白过。', 3, 3,
   '{"primaryEmotion":"焦虑","emotionScore":62,"riskLevel":1,"isNegative":true,"suggestion":"焦虑在提醒你这件事对你重要，先做最小的一步就好。","riskDescription":"存在考试相关焦虑，暂未影响基本生活。","improvementSuggestions":["把复习任务拆成 25 分钟一段","睡前写下明天要完成的三件事","减少睡前刷手机的时间"]}',
   DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(CURDATE(), INTERVAL 1 DAY), NOW()),

  (3, 2, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 4, '低落', '和室友闹了点小别扭', '一整天都提不起劲，不想说话，也不太想吃饭。', 2, 4,
   '{"primaryEmotion":"低落","emotionScore":71,"riskLevel":2,"isNegative":true,"suggestion":"人际摩擦带来的低落很常见，先照顾好自己的睡眠和饮食。","riskDescription":"情绪低落伴随食欲与睡眠下降，建议持续关注。","improvementSuggestions":["如果愿意，可以主动说一句：那天的事我想聊聊","把今天想说的话写在日记里","保证按时吃饭，哪怕少吃一点"]}',
   DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(CURDATE(), INTERVAL 2 DAY), NOW()),

  (4,  2, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 7, '平静', '去操场跑了几圈', '运动完心情好多了，脑子里那些乱七八糟的声音小了很多。', 4, 2, NULL, NULL, DATE_SUB(CURDATE(), INTERVAL 3 DAY), NOW()),
  (5,  2, DATE_SUB(CURDATE(), INTERVAL 4 DAY), 3, '焦虑', '想到未来找工作', '晚上失眠，脑子里全是以后怎么办。', 2, 5, NULL, NULL, DATE_SUB(CURDATE(), INTERVAL 4 DAY), NOW()),
  (6,  2, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 5, '疲惫', '连续几天没睡好', '白天一直打哈欠，上课注意力集中不了。', 3, 3, NULL, NULL, DATE_SUB(CURDATE(), INTERVAL 5 DAY), NOW()),
  (7,  2, DATE_SUB(CURDATE(), INTERVAL 6 DAY), 8, '满足', '帮同学讲懂了一道题', '被人需要的感觉挺好的。', 4, 1, NULL, NULL, DATE_SUB(CURDATE(), INTERVAL 6 DAY), NOW()),

  -- 用户 3：3 条
  (8,  3, CURDATE(), 4, '低落', '没什么特别的事，就是累', '说不上来，就是不想动。', 3, 3, NULL, NULL, CURDATE(), NOW()),
  (9,  3, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 2, '低落', '被老师当众批评', '觉得很没面子，一整天都缓不过来。', 2, 5, NULL, NULL, DATE_SUB(CURDATE(), INTERVAL 2 DAY), NOW()),
  (10, 3, DATE_SUB(CURDATE(), INTERVAL 4 DAY), 6, '平静', '看了会儿书', '安静地待了一下午，还行。', 4, 2, NULL, NULL, DATE_SUB(CURDATE(), INTERVAL 4 DAY), NOW()),

  -- 用户 4：3 条
  (11, 4, CURDATE(), 4, '焦虑', '明天有随堂测验', '一直在背，但感觉什么都没记住。', 3, 4, NULL, NULL, CURDATE(), NOW()),
  (12, 4, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 3, '焦虑', '又熬夜了', '凌晨两点才睡，早上起不来。', 1, 5, NULL, NULL, DATE_SUB(CURDATE(), INTERVAL 1 DAY), NOW()),
  (13, 4, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 7, '平静', '早睡了一次', '十一点上床，第二天状态明显不一样。', 5, 1, NULL, NULL, DATE_SUB(CURDATE(), INTERVAL 3 DAY), NOW()),

  -- 用户 5：1 条，带 AI 分析结果
  (14, 5, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 8, '平静', '一个人看了场电影', '难得放松，不用考虑别人的感受。', 5, 1,
   '{"primaryEmotion":"平静","emotionScore":22,"riskLevel":0,"isNegative":false,"suggestion":"独处也是一种恢复能量的方式，允许自己享受它。","riskDescription":"情绪稳定，未发现风险信号。","improvementSuggestions":["保持这样的自我照顾节奏"]}',
   DATE_SUB(NOW(), INTERVAL 20 HOUR), DATE_SUB(CURDATE(), INTERVAL 1 DAY), NOW());

-- ---------- 7. AI 分析任务 ---------------------------------------------------
-- 覆盖 4 种状态，用于验证：
--   · 管理端任务状态统计
--   · 定时任务只捡 PENDING（id 3、4）
--   · PROCESSING 超时回收（id 5 的 started_at 是 1 小时前，模拟"卡死"，recycleStuck 应该回收它）
--   · FAILED 重试：id 6 的 retry_count=1 < max，可重试；id 7 的 3=3，应被标为最终失败
-- diary_id / user_id 必须和上面的日记对得上（有外键）
INSERT INTO ai_analysis_task
  (id, diary_id, user_id, status, task_type, priority, retry_count, max_retry_count, error_message, started_at, completed_at, created_at, updated_at)
VALUES
  (1, 1,  2, 'COMPLETED',  'AUTO',   2, 0, 3, NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR) + INTERVAL 8 SECOND, DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW()),
  (2, 2,  2, 'COMPLETED',  'AUTO',   2, 0, 3, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY),  DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 7 SECOND,  DATE_SUB(NOW(), INTERVAL 1 DAY),  NOW()),
  (3, 3,  2, 'PENDING',    'AUTO',   2, 0, 3, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 10 MINUTE), NOW()),
  (4, 8,  3, 'PENDING',    'AUTO',   2, 0, 3, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 5 MINUTE),  NOW()),
  (5, 9,  3, 'PROCESSING', 'AUTO',   3, 0, 3, NULL, DATE_SUB(NOW(), INTERVAL 1 HOUR), NULL, DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR)),
  (6, 11, 4, 'FAILED',     'AUTO',   2, 1, 3, 'AI 服务响应超时（模拟失败，仍可重试）', DATE_SUB(NOW(), INTERVAL 3 HOUR), NULL, DATE_SUB(NOW(), INTERVAL 3 HOUR), NOW()),
  (7, 12, 4, 'FAILED',     'AUTO',   4, 3, 3, '重试次数已达上限（模拟最终失败）',       DATE_SUB(NOW(), INTERVAL 5 HOUR), NULL, DATE_SUB(NOW(), INTERVAL 5 HOUR), NOW()),
  (8, 14, 5, 'COMPLETED',  'MANUAL', 1, 0, 3, NULL, DATE_SUB(NOW(), INTERVAL 20 HOUR), DATE_SUB(NOW(), INTERVAL 20 HOUR) + INTERVAL 9 SECOND, DATE_SUB(NOW(), INTERVAL 20 HOUR), NOW());

-- ---------- 8. 文件信息 ------------------------------------------------------
-- 覆盖三种情况：正式业务文件 / 未过期的临时文件 / 已过期的临时文件 / 已删除文件
-- file_path 格式与 FileService 实际写库的一致：/files/年-月/UUID.ext
INSERT INTO sys_file_info
  (id, original_name, file_path, file_size, file_type, business_type, business_id, business_field, upload_user_id, is_temp, status, create_time, expire_time)
VALUES
  (1, 'avatar.png',        '/files/2026-09/a56f4b34-3059-46ab-a62c-fd5cfcd30d96.png', 1810217, 'IMG', 'avatar',     '2',                                    'avatar',      2, 0, 1, DATE_SUB(NOW(), INTERVAL 20 DAY), NULL),
  (2, 'cover-anxiety.jpg', '/files/2026-09/b1c2d3e4-1111-4222-8333-444455556666.jpg', 245678,  'IMG', 'cover',      'a1000000-0000-4000-8000-000000000001', 'cover_image', 1, 0, 1, DATE_SUB(NOW(), INTERVAL 10 DAY), NULL),
  (3, '测试文档.pdf',       '/files/2026-09/c2d3e4f5-2222-4333-8444-555566667777.pdf', 512000,  'PDF', 'document',   NULL,                                   NULL,          2, 0, 1, DATE_SUB(NOW(), INTERVAL 5 DAY),  NULL),
  (4, '临时截图.png',       '/files/2026-09/d3e4f5a6-3333-4444-8555-666677778888.png', 102400,  'IMG', 'attachment', NULL,                                   NULL,          3, 1, 1, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_ADD(NOW(), INTERVAL 22 HOUR)),
  (5, '过期临时文件.png',   '/files/2026-09/e4f5a6b7-4444-4555-8666-777788889999.png', 88900,   'IMG', 'attachment', NULL,                                   NULL,          4, 1, 1, DATE_SUB(NOW(), INTERVAL 3 DAY),  DATE_SUB(NOW(), INTERVAL 2 DAY)),
  (6, '已删除的文件.png',   '/files/2026-09/f5a6b7c8-5555-4666-8777-888899990000.png', 66600,   'IMG', 'attachment', NULL,                                   NULL,          4, 0, 0, DATE_SUB(NOW(), INTERVAL 8 DAY),  NULL);

-- ---------- 9. 用户收藏 ------------------------------------------------------
-- 唯一键是 (user_id, article_id)
INSERT INTO user_favorite (id, user_id, article_id, created_at)
VALUES
  (1, 2, 'a1000000-0000-4000-8000-000000000001', DATE_SUB(NOW(), INTERVAL 7 DAY)),
  (2, 2, 'a1000000-0000-4000-8000-000000000002', DATE_SUB(NOW(), INTERVAL 6 DAY)),
  (3, 2, 'a1000000-0000-4000-8000-000000000006', DATE_SUB(NOW(), INTERVAL 4 DAY)),
  (4, 3, 'a1000000-0000-4000-8000-000000000003', DATE_SUB(NOW(), INTERVAL 5 DAY)),
  (5, 3, 'a1000000-0000-4000-8000-000000000004', DATE_SUB(NOW(), INTERVAL 3 DAY)),
  (6, 4, 'a1000000-0000-4000-8000-000000000006', DATE_SUB(NOW(), INTERVAL 2 DAY)),
  (7, 5, 'a1000000-0000-4000-8000-000000000001', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- =============================================================================
-- 第三步：核对结果
--   期望：user=6, category=7, article=9, session=5, message=14,
--        diary=14, task=8, file=6, favorite=7
-- =============================================================================
SELECT 'user'                 AS 表名, COUNT(*) AS 行数 FROM `user`
UNION ALL SELECT 'knowledge_category',   COUNT(*) FROM knowledge_category
UNION ALL SELECT 'knowledge_article',    COUNT(*) FROM knowledge_article
UNION ALL SELECT 'consultation_session', COUNT(*) FROM consultation_session
UNION ALL SELECT 'consultation_message', COUNT(*) FROM consultation_message
UNION ALL SELECT 'emotion_diary',        COUNT(*) FROM emotion_diary
UNION ALL SELECT 'ai_analysis_task',     COUNT(*) FROM ai_analysis_task
UNION ALL SELECT 'sys_file_info',        COUNT(*) FROM sys_file_info
UNION ALL SELECT 'user_favorite',        COUNT(*) FROM user_favorite;
