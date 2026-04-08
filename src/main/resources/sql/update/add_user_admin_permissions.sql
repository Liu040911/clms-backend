-- ====================================
-- 管理员管理权限点添加
-- 创建日期: 2026-04-02
-- 说明: 为用户管理模块添加 UserAdminController 中鉴权使用的权限点
-- ====================================

USE baile_test;

-- ====================================
-- 权限点插入（permission_table）
-- 权限点:
-- 1) admin:addAdmin
-- 2) admin:disableAdmin
-- 3) admin:enableAdmin
-- 4) admin:editAdminInfo
-- 5) admin:getAdminList
-- ====================================

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019d4d3df48771659c68d8f1b95d948e', 'admin:addAdmin', '新增管理员', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'admin:addAdmin'
);

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019d4d3e0703707b8912a179f15cbc96', 'admin:disableAdmin', '停用管理员权限', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'admin:disableAdmin'
);

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019d4d3e1930747bb3bfd94ef27cb55c', 'admin:enableAdmin', '启用管理员权限', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'admin:enableAdmin'
);

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019d4d3e2a6a74f2a6a8c2f6d8d14a31', 'admin:editAdminInfo', '编辑管理员信息', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'admin:editAdminInfo'
);

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019d4d3e317974a7b4782de4049920b7', 'admin:getAdminList', '获取管理员列表', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'admin:getAdminList'
);

-- ====================================
-- 关联模块权限（model_permission_table）
-- 模块名默认使用: 管理员管理模块
-- 如你环境中模块名不同，请调整 m.model_name 条件
-- ====================================

-- 先创建管理员管理模块
INSERT INTO `model_table`(`id`, `model_name`, `model_description`, `meta_data`, `create_time`, `update_time`, `deleted`)
SELECT '019d4d4db3b27381b283909cd41eda89', '管理员管理模块', '管理员角色与权限管理相关模块', NULL, NOW(), NOW(), 0
WHERE NOT EXISTS (
        SELECT 1 FROM `model_table`
        WHERE `model_name` = '管理员管理模块'
            AND `deleted` = 0
);

INSERT INTO `model_permission_table`(`id`, `model_id`, `permission_id`, `create_time`, `update_time`, `deleted`)
SELECT REPLACE(UUID(), '-', ''), m.id, p.id, NOW(), NOW(), 0
FROM `model_table` m, `permission_table` p
WHERE m.model_name = '管理员管理模块'
    AND p.permission_string IN ('admin:addAdmin', 'admin:disableAdmin', 'admin:enableAdmin', 'admin:editAdminInfo', 'admin:getAdminList')
  AND NOT EXISTS (
      SELECT 1 FROM `model_permission_table` mp
      WHERE mp.model_id = m.id AND mp.permission_id = p.id
  );

-- ====================================
-- 完成说明
-- UserAdminController 校验权限已添加:
-- admin:addAdmin
-- admin:disableAdmin
-- admin:enableAdmin
-- admin:editAdminInfo
-- admin:getAdminList
-- ====================================
