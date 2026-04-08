-- ====================================
-- 角色管理权限点添加
-- 创建日期: 2026-04-02
-- 说明: 为角色管理模块添加 RoleAdminController 中鉴权使用的权限点
-- ====================================

USE baile_test;

-- ====================================
-- 权限点插入（permission_table）
-- 权限点:
-- 1) role:create
-- 2) role:update
-- 3) role:delete
-- 4) role:info
-- 5) role:list
-- 6) role:bindPermission
-- 7) role:unbindPermission
-- 8) role:permissionList
-- ====================================

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019d4df5c28c708aa9d239edfb2c7c3c', 'role:create', '创建角色', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'role:create'
);

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019d4df5dfcf733db92fcc9479e16242', 'role:update', '修改角色', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'role:update'
);

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019d4df5f50c709e8902ff0481fb7dbf', 'role:delete', '删除角色', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'role:delete'
);

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019d4df60a7073ef9fd79ef2de1d614a', 'role:info', '获取角色信息', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'role:info'
);

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019d4df61c07763eb094b11dd22013bc', 'role:list', '获取角色列表', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'role:list'
);

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019d4df631df74abb6491d81425af087', 'role:bindPermission', '角色绑定权限', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'role:bindPermission'
);

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019d4df648bb72f9a296d6384d351527', 'role:unbindPermission', '角色解绑权限', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'role:unbindPermission'
);

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019d4df659c47273a0d922fbeca94608', 'role:permissionList', '查看角色权限列表', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'role:permissionList'
);

-- ====================================
-- 关联模块权限（model_permission_table）
-- 模块名默认使用: 角色管理模块
-- 如你环境中模块名不同，请调整 m.model_name 条件
-- ====================================

-- 先创建角色管理模块
INSERT INTO `model_table`(`id`, `model_name`, `model_description`, `meta_data`, `create_time`, `update_time`, `deleted`)
SELECT '019d4df6733d701e8cf265bae43e4c59', '角色管理模块', '角色与权限管理相关模块', NULL, NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `model_table`
    WHERE `model_name` = '角色管理模块'
      AND `deleted` = 0
);

INSERT INTO `model_permission_table`(`id`, `model_id`, `permission_id`, `create_time`, `update_time`, `deleted`)
SELECT REPLACE(UUID(), '-', ''), m.id, p.id, NOW(), NOW(), 0
FROM `model_table` m, `permission_table` p
WHERE m.model_name = '角色管理模块'
  AND p.permission_string IN (
      'role:create',
      'role:update',
      'role:delete',
      'role:info',
      'role:list',
      'role:bindPermission',
      'role:unbindPermission',
      'role:permissionList'
  )
  AND NOT EXISTS (
      SELECT 1 FROM `model_permission_table` mp
      WHERE mp.model_id = m.id AND mp.permission_id = p.id
  );

-- ====================================
-- 完成说明
-- RoleAdminController 鉴权权限已添加:
-- role:create
-- role:update
-- role:delete
-- role:info
-- role:list
-- role:bindPermission
-- role:unbindPermission
-- role:permissionList
-- ====================================
