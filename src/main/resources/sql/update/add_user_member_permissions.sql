-- ====================================
-- 用户成员管理权限点添加
-- 创建日期: 2026-05-11
-- 说明: 为用户成员管理模块添加 UserMemberAdminController 中鉴权使用的权限点
-- ====================================

-- ====================================
-- 权限点插入（permission_table）
-- 权限点:
-- 1) user:list
-- 2) user:edit
-- 3) user:enable
-- 4) user:disable
-- ====================================

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019e1ab560b27040b0b1cbfac8297cf4', 'user:list', '获取用户列表', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'user:list'
);

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019e1ab5790d742ea1b87f7d42ef8919', 'user:edit', '编辑用户信息', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'user:edit'
);

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019e1ab58a6f744eb5116a9019a11642', 'user:enable', '启用用户', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'user:enable'
);

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019e1ab59c33774ca663174bfe799445', 'user:disable', '停用用户', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'user:disable'
);

-- ====================================
-- 关联模块权限（model_permission_table）
-- 模块名: 用户管理模块
-- ====================================

-- 先创建用户管理模块
INSERT INTO `model_table`(`id`, `model_name`, `model_description`, `meta_data`, `create_time`, `update_time`, `deleted`)
SELECT '019e1ab5ac9c70e0b4c4a299bab40dc5', '用户管理模块', '普通用户成员管理相关模块', NULL, NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `model_table`
    WHERE `model_name` = '用户管理模块'
        AND `deleted` = 0
);

INSERT INTO `model_permission_table`(`id`, `model_id`, `permission_id`, `create_time`, `update_time`, `deleted`)
SELECT REPLACE(UUID(), '-', ''), m.id, p.id, NOW(), NOW(), 0
FROM `model_table` m, `permission_table` p
WHERE m.model_name = '用户管理模块'
    AND p.permission_string IN ('user:list', 'user:edit', 'user:enable', 'user:disable')
  AND NOT EXISTS (
      SELECT 1 FROM `model_permission_table` mp
      WHERE mp.model_id = m.id AND mp.permission_id = p.id
  );

-- ====================================
-- 完成说明
-- UserMemberAdminController 校验权限已添加:
-- user:list
-- user:edit
-- user:enable
-- user:disable
-- ====================================
