-- Active: 1764214974199@@192.168.10.134@3306@clms
-- ====================================
-- AI聊天权限点添加
-- 创建日期: 2026-05-12
-- 说明: 为AI聊天模块添加 ChatController 中鉴权使用的权限点
-- ====================================

-- ====================================
-- 权限点插入（permission_table）
-- 权限点:
-- 1) chat:send - 发送消息
-- 2) chat:sessions - 获取会话列表
-- 3) chat:messages - 获取会话消息
-- 4) chat:delete - 删除会话
-- ====================================

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019e1ab0e8e5729b90cffb0bd6134a96', 'chat:send', '发送消息', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'chat:send'
);

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019e1ab0fa09758c99617bf848ca2fab', 'chat:sessions', '获取会话列表', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'chat:sessions'
);

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019e1ab10cd2706d880cc2ade9e182cf', 'chat:messages', '获取会话消息', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'chat:messages'
);

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019e1ab12a4776eda3c1bd381d007a4e', 'chat:delete', '删除会话', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'chat:delete'
);

-- ====================================
-- 关联模块权限（model_permission_table）
-- 模块名默认使用: AI聊天模块
-- ====================================

-- 先创建AI聊天模块
INSERT INTO `model_table`(`id`, `model_name`, `model_description`, `meta_data`, `create_time`, `update_time`, `deleted`)
SELECT '019e1ab205f673d891193ce102e9feea', 'AI聊天模块', 'AI智能对话相关模块', NULL, NOW(), NOW(), 0
WHERE NOT EXISTS (
        SELECT 1 FROM `model_table`
        WHERE `model_name` = 'AI聊天模块'
            AND `deleted` = 0
);

INSERT INTO `model_permission_table`(`id`, `model_id`, `permission_id`, `create_time`, `update_time`, `deleted`)
SELECT REPLACE(UUID(), '-', ''), m.id, p.id, NOW(), NOW(), 0
FROM `model_table` m, `permission_table` p
WHERE m.model_name = 'AI聊天模块'
    AND p.permission_string IN ('chat:send', 'chat:sessions', 'chat:messages', 'chat:delete')
  AND NOT EXISTS (
      SELECT 1 FROM `model_permission_table` mp
      WHERE mp.model_id = m.id AND mp.permission_id = p.id
  );

-- ====================================
-- 完成说明
-- ChatController 校验权限已添加:
-- chat:send, chat:sessions, chat:messages, chat:deleteSession
-- ====================================
