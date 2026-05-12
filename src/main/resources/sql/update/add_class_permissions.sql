-- ====================================
-- 教室管理权限点添加
-- 创建日期: 2026-05-12
-- 说明: 为教室管理模块添加 ClassController 中鉴权使用的权限点
-- ====================================

-- ====================================
-- 权限点插入（permission_table）
-- 权限点:
-- 1) class:create - 创建教室
-- 2) class:update - 修改教室
-- 3) class:delete - 删除教室
-- 4) class:info - 获取教室信息
-- 5) class:list - 获取教室列表
-- 6) class:availableList - 获取可用教室列表
-- ====================================

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019e1ab224e875afb3032c477b80581c', 'class:create', '创建教室', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'class:create'
);

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019e1ab24aeb72ac9e76eda42f9a4fe1', 'class:update', '修改教室', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'class:update'
);

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019e1ab261ca727b804480ac0bcd4f7b', 'class:delete', '删除教室', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'class:delete'
);

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019e1ab274bc711da7beb326ef8aa769', 'class:info', '获取教室信息', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'class:info'
);

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019e1ab28853722b8c4a2a3d12543506', 'class:list', '获取教室列表', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'class:list'
);

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019e1ab2a3727303ae42b29d0750e136', 'class:availableList', '获取可用教室列表', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'class:availableList'
);

-- ====================================
-- 关联模块权限（model_permission_table）
-- 模块名默认使用: 教室管理模块
-- ====================================

-- 先创建教室管理模块
INSERT INTO `model_table`(`id`, `model_name`, `model_description`, `meta_data`, `create_time`, `update_time`, `deleted`)
SELECT '019e1ab2bb2c7000ba757077eeb63e6e', '教室管理模块', '教室信息维护与查询相关模块', NULL, NOW(), NOW(), 0
WHERE NOT EXISTS (
        SELECT 1 FROM `model_table`
        WHERE `model_name` = '教室管理模块'
            AND `deleted` = 0
);

INSERT INTO `model_permission_table`(`id`, `model_id`, `permission_id`, `create_time`, `update_time`, `deleted`)
SELECT REPLACE(UUID(), '-', ''), m.id, p.id, NOW(), NOW(), 0
FROM `model_table` m, `permission_table` p
WHERE m.model_name = '教室管理模块'
    AND p.permission_string IN ('class:create', 'class:update', 'class:delete', 'class:info', 'class:list', 'class:availableList')
  AND NOT EXISTS (
      SELECT 1 FROM `model_permission_table` mp
      WHERE mp.model_id = m.id AND mp.permission_id = p.id
  );

-- ====================================
-- 完成说明
-- ClassController 校验权限已添加:
-- class:create, class:update, class:delete
-- class:info, class:list, class:availableList
-- ====================================
