-- ====================================
-- 标签管理权限点添加
-- 创建日期: 2026-05-12
-- 说明: 为标签管理模块添加 TagController 中鉴权使用的权限点
-- ====================================

-- ====================================
-- 权限点插入（permission_table）
-- 权限点:
-- 1) tag:create - 创建标签
-- 2) tag:update - 修改标签
-- 3) tag:delete - 删除标签
-- 4) tag:info - 获取标签详情
-- 5) tag:list - 获取标签列表
-- ====================================

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019e1ab4d8f073df8a68b0631bee32f6', 'tag:create', '创建标签', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'tag:create'
);

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019e1ab4ede173de946d1bc913c44cb1', 'tag:update', '修改标签', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'tag:update'
);

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019e1ab5046071aaa18922a62c7161cf', 'tag:delete', '删除标签', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'tag:delete'
);

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019e1ab516807067a3f0ba5afbf51624', 'tag:info', '获取标签详情', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'tag:info'
);

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019e1ab528b774dabe35436fc5b23ea6', 'tag:list', '获取标签列表', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'tag:list'
);

-- ====================================
-- 关联模块权限（model_permission_table）
-- 模块名默认使用: 标签管理模块
-- ====================================

-- 先创建标签管理模块
INSERT INTO `model_table`(`id`, `model_name`, `model_description`, `meta_data`, `create_time`, `update_time`, `deleted`)
SELECT '019e1ab53e84711891dd406a87121e7f', '标签管理模块', '讲座标签分类管理相关模块', NULL, NOW(), NOW(), 0
WHERE NOT EXISTS (
        SELECT 1 FROM `model_table`
        WHERE `model_name` = '标签管理模块'
            AND `deleted` = 0
);

INSERT INTO `model_permission_table`(`id`, `model_id`, `permission_id`, `create_time`, `update_time`, `deleted`)
SELECT REPLACE(UUID(), '-', ''), m.id, p.id, NOW(), NOW(), 0
FROM `model_table` m, `permission_table` p
WHERE m.model_name = '标签管理模块'
    AND p.permission_string IN ('tag:create', 'tag:update', 'tag:delete', 'tag:info', 'tag:list')
  AND NOT EXISTS (
      SELECT 1 FROM `model_permission_table` mp
      WHERE mp.model_id = m.id AND mp.permission_id = p.id
  );

-- ====================================
-- 完成说明
-- TagController 校验权限已添加:
-- tag:create, tag:update, tag:delete
-- tag:info, tag:list
-- ====================================
