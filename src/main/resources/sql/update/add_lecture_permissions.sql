-- ====================================
-- 讲座管理权限点添加
-- 创建日期: 2026-05-12
-- 说明: 为讲座管理模块添加 LectureController 中鉴权使用的权限点
-- ====================================

-- ====================================
-- 权限点插入（permission_table）
-- 权限点:
-- 1) lecture:create - 创建讲座
-- 2) lecture:update - 修改讲座
-- 3) lecture:delete - 删除讲座
-- 4) lecture:approve - 审批通过讲座
-- 5) lecture:reject - 驳回讲座
-- 6) lecture:auditList - 获取讲座审核记录
-- 7) lecture:list - 获取讲座列表
-- 8) lecture:end - 结束讲座
-- 9) lecture:checkInQrCode - 获取讲座签到二维码
-- 10) lecture:analyticsOverview - 获取讲座分析概览
-- 11) lecture:analyticsTrend - 获取讲座分析趋势
-- 12) lecture:analyticsTagTop - 获取讲座标签热度
-- ====================================

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019e1ab3716a764b8bd1bae3e06ca33a', 'lecture:create', '创建讲座', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'lecture:create'
);

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019e1ab3842076558215408a5231a8e8', 'lecture:update', '修改讲座', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'lecture:update'
);

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019e1ab39565754db495d5ed92c6acf7', 'lecture:delete', '删除讲座', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'lecture:delete'
);

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019e1ab3a842724895af92b09df734cf', 'lecture:approve', '审批通过讲座', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'lecture:approve'
);

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019e1ab3b93470fc94eac5650d6e92ec', 'lecture:reject', '驳回讲座', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'lecture:reject'
);

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019e1ab3c943721d8b37dea11aa61de0', 'lecture:auditList', '获取讲座审核记录', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'lecture:auditList'
);

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019e1ab3dda1727dae4cad4230fb27e4', 'lecture:list', '获取讲座列表', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'lecture:list'
);

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019e1ab3efc273bb8a917cbb9d1479ba', 'lecture:end', '结束讲座', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'lecture:end'
);

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019e1ab404a37281a158d22dfaa8a9f6', 'lecture:checkInQrCode', '获取讲座签到二维码', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'lecture:checkInQrCode'
);

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019e1ab41892700c80b9b6e3fb43fb80', 'lecture:analyticsOverview', '获取讲座分析概览', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'lecture:analyticsOverview'
);

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019e1ab42aa477eeb103ccc2ef7680d9', 'lecture:analyticsTrend', '获取讲座分析趋势', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'lecture:analyticsTrend'
);

INSERT INTO `permission_table`(`id`, `permission_string`, `permission_name`, `create_time`, `update_time`, `deleted`)
SELECT '019e1ab43bb870aea119bbbf3cb6ec20', 'lecture:analyticsTagTop', '获取讲座标签热度', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `permission_table`
    WHERE `permission_string` = 'lecture:analyticsTagTop'
);

-- ====================================
-- 关联模块权限（model_permission_table）
-- 模块名默认使用: 讲座管理模块
-- 如你环境中模块名不同，请调整 m.model_name 条件
-- ====================================

-- 先创建讲座管理模块
INSERT INTO `model_table`(`id`, `model_name`, `model_description`, `meta_data`, `create_time`, `update_time`, `deleted`)
SELECT '019e1ab46afe755badf0278353057479', '讲座管理模块', '讲座创建、审批、分析等相关模块', NULL, NOW(), NOW(), 0
WHERE NOT EXISTS (
        SELECT 1 FROM `model_table`
        WHERE `model_name` = '讲座管理模块'
            AND `deleted` = 0
);

INSERT INTO `model_permission_table`(`id`, `model_id`, `permission_id`, `create_time`, `update_time`, `deleted`)
SELECT REPLACE(UUID(), '-', ''), m.id, p.id, NOW(), NOW(), 0
FROM `model_table` m, `permission_table` p
WHERE m.model_name = '讲座管理模块'
    AND p.permission_string IN ('lecture:create', 'lecture:update', 'lecture:delete', 'lecture:approve', 'lecture:reject', 'lecture:auditList', 'lecture:list', 'lecture:end', 'lecture:checkInQrCode', 'lecture:analyticsOverview', 'lecture:analyticsTrend', 'lecture:analyticsTagTop')
  AND NOT EXISTS (
      SELECT 1 FROM `model_permission_table` mp
      WHERE mp.model_id = m.id AND mp.permission_id = p.id
  );

-- ====================================
-- 完成说明
-- LectureController 校验权限已添加:
-- lecture:create, lecture:update, lecture:delete
-- lecture:approve, lecture:reject, lecture:auditList
-- lecture:list, lecture:end, lecture:checkInQrCode
-- lecture:analyticsOverview, lecture:analyticsTrend, lecture:analyticsTagTop
-- ====================================
