-- ====================================
-- 问卷导出功能权限点添加
-- 创建日期: 2025-09-15
-- 说明: 为问卷管理模块添加导出 Excel 功能的权限点
-- ====================================

USE baile_test;

-- 问卷导出权限 - single渠道
INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `app_key`, `app_channel`, `deleted`) 
SELECT '6f204d64b8164ddba3bb52c525da4971', 'questionnaire:export', '导出问卷答案', NOW(), NOW(), 'mzu0tc0y86q49hfvy669h9k0qatpzezs', 'single', 0
WHERE NOT EXISTS (SELECT 1 FROM `permission_table` WHERE `permission_string` = 'questionnaire:export' AND `app_key` = 'mzu0tc0y86q49hfvy669h9k0qatpzezs' AND `app_channel` = 'single');

-- 问卷导出权限 - 渠道1
INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `app_key`, `app_channel`, `deleted`) 
SELECT '793f0c6cdb3c4251abef85f9d7d5fbb7', 'questionnaire:export', '导出问卷答案', NOW(), NOW(), 'mzu0tc0y86q49hfvy669h9k0qatpzezs', '1', 0
WHERE NOT EXISTS (SELECT 1 FROM `permission_table` WHERE `permission_string` = 'questionnaire:export' AND `app_key` = 'mzu0tc0y86q49hfvy669h9k0qatpzezs' AND `app_channel` = '1');

-- 问卷导出权限 - 渠道2
INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `app_key`, `app_channel`, `deleted`) 
SELECT 'f2799a2130314fdd8d23cfb0c5038bfe', 'questionnaire:export', '导出问卷答案', NOW(), NOW(), 'mzu0tc0y86q49hfvy669h9k0qatpzezs', '2', 0
WHERE NOT EXISTS (SELECT 1 FROM `permission_table` WHERE `permission_string` = 'questionnaire:export' AND `app_key` = 'mzu0tc0y86q49hfvy669h9k0qatpzezs' AND `app_channel` = '2');

-- 问卷导出权限 - 渠道3
INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `app_key`, `app_channel`, `deleted`) 
SELECT '30a4dca042154888bc1263dda396c5ca', 'questionnaire:export', '导出问卷答案', NOW(), NOW(), 'mzu0tc0y86q49hfvy669h9k0qatpzezs', '3', 0
WHERE NOT EXISTS (SELECT 1 FROM `permission_table` WHERE `permission_string` = 'questionnaire:export' AND `app_key` = 'mzu0tc0y86q49hfvy669h9k0qatpzezs' AND `app_channel` = '3');

-- ====================================
-- 关联模块权限 (使用动态查询获取模块ID和权限ID)
-- ====================================

-- questionnaire:export - single渠道
INSERT INTO `model_permission_table`(`id`, `model_id`, `permission_id`, `create_time`, `update_time`, `app_key`, `app_channel`, `deleted`) 
SELECT 
    '283df4d2adc0415c9efb827f1f6f30a0',
    m.id,
    p.id,
    NOW(), 
    NOW(), 
    'mzu0tc0y86q49hfvy669h9k0qatpzezs', 
    'single', 
    0
FROM `model_table` m, `permission_table` p
WHERE m.model_name = '问卷管理' 
    AND m.app_key = 'mzu0tc0y86q49hfvy669h9k0qatpzezs' 
    AND m.app_channel = 'single'
    AND p.permission_string = 'questionnaire:export' 
    AND p.app_key = 'mzu0tc0y86q49hfvy669h9k0qatpzezs' 
    AND p.app_channel = 'single'
    AND NOT EXISTS (SELECT 1 FROM `model_permission_table` WHERE `model_id` = m.id AND `permission_id` = p.id);

-- questionnaire:export - 渠道1
INSERT INTO `model_permission_table`(`id`, `model_id`, `permission_id`, `create_time`, `update_time`, `app_key`, `app_channel`, `deleted`) 
SELECT 
    'b538709c4e48426ebccfd9e38848cd02',
    m.id,
    p.id,
    NOW(), 
    NOW(), 
    'mzu0tc0y86q49hfvy669h9k0qatpzezs', 
    '1', 
    0
FROM `model_table` m, `permission_table` p
WHERE m.model_name = '问卷管理' 
    AND m.app_key = 'mzu0tc0y86q49hfvy669h9k0qatpzezs' 
    AND m.app_channel = '1'
    AND p.permission_string = 'questionnaire:export' 
    AND p.app_key = 'mzu0tc0y86q49hfvy669h9k0qatpzezs' 
    AND p.app_channel = '1'
    AND NOT EXISTS (SELECT 1 FROM `model_permission_table` WHERE `model_id` = m.id AND `permission_id` = p.id);

-- questionnaire:export - 渠道2
INSERT INTO `model_permission_table`(`id`, `model_id`, `permission_id`, `create_time`, `update_time`, `app_key`, `app_channel`, `deleted`) 
SELECT 
    '7aef9254b4ca4508ac8c2e4e3e6896f5',
    m.id,
    p.id,
    NOW(), 
    NOW(), 
    'mzu0tc0y86q49hfvy669h9k0qatpzezs', 
    '2', 
    0
FROM `model_table` m, `permission_table` p
WHERE m.model_name = '问卷管理' 
    AND m.app_key = 'mzu0tc0y86q49hfvy669h9k0qatpzezs' 
    AND m.app_channel = '2'
    AND p.permission_string = 'questionnaire:export' 
    AND p.app_key = 'mzu0tc0y86q49hfvy669h9k0qatpzezs' 
    AND p.app_channel = '2'
    AND NOT EXISTS (SELECT 1 FROM `model_permission_table` WHERE `model_id` = m.id AND `permission_id` = p.id);

-- questionnaire:export - 渠道3
INSERT INTO `model_permission_table`(`id`, `model_id`, `permission_id`, `create_time`, `update_time`, `app_key`, `app_channel`, `deleted`) 
SELECT 
    '2b68a305c84242e79cb8033a758b5a08',
    m.id,
    p.id,
    NOW(), 
    NOW(), 
    'mzu0tc0y86q49hfvy669h9k0qatpzezs', 
    '3', 
    0
FROM `model_table` m, `permission_table` p
WHERE m.model_name = '问卷管理' 
    AND m.app_key = 'mzu0tc0y86q49hfvy669h9k0qatpzezs' 
    AND m.app_channel = '3'
    AND p.permission_string = 'questionnaire:export' 
    AND p.app_key = 'mzu0tc0y86q49hfvy669h9k0qatpzezs' 
    AND p.app_channel = '3'
    AND NOT EXISTS (SELECT 1 FROM `model_permission_table` WHERE `model_id` = m.id AND `permission_id` = p.id);

-- ====================================
-- 完成插入
-- ====================================
-- 问卷导出权限已成功添加到问卷管理模块
-- 权限点: questionnaire:export - 导出问卷答案
-- 对应API: GET /admin/questionnaire/export - 导出问卷答案为Excel文件

-- ====================================
-- 使用说明
-- ====================================
-- 此脚本使用动态查询方式关联模块权限：
-- 1. 通过模块名称 '问卷管理' 动态查询模块ID
-- 2. 通过权限字符串 'questionnaire:export' 动态查询权限ID  
-- 3. 避免硬编码ID，提高脚本的通用性和可维护性