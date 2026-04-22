-- Active: 1776868316691@@49.234.51.49@3306@clms
SET NAMES utf8mb4;
SET character_set_client = 'utf8mb4';
SET character_set_results = 'utf8mb4';
SET character_set_connection = 'utf8mb4';
SET collation_connection = 'utf8mb4_unicode_ci';

USE clms_test;

CREATE TABLE IF NOT EXISTS `user_table` (
    `id` CHAR(32) CHARACTER SET ascii NOT NULL COMMENT '用户ID' DEFAULT(
        replace (
                uuid(),
                _utf8mb3 '-',
                _utf8mb3 ''
            )
    ),
    `nickname` VARCHAR(255) DEFAULT '' COMMENT '用户昵称',
    `gender` TINYINT(1) DEFAULT '0' COMMENT '用户性别：0-未知，1-男，2-女',
    `email` VARCHAR(255) UNIQUE NULL COMMENT '用户邮箱',
    `phone` VARCHAR(255) UNIQUE NOT NULL COMMENT '用户手机号',
    `password` VARCHAR(255) DEFAULT '' COMMENT '用户密码 (加密)',
    `avatar_url` VARCHAR(255) DEFAULT '' COMMENT '用户头像URL',
    `user_roles` JSON NOT NULL COMMENT '用户角色',
    `user_permissions` JSON NOT NULL COMMENT '用户权限',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0表示未删除，1表示已删除',
    PRIMARY KEY (`id`)
) COMMENT='用户表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====== 权限系统表 ======

-- 模块表
CREATE TABLE IF NOT EXISTS `model_table` (
    `id` CHAR(32) CHARACTER SET ascii NOT NULL COMMENT '模块ID' DEFAULT(
        replace (
                uuid(),
                _utf8mb3 '-',
                _utf8mb3 ''
            )
    ),
    `model_name` VARCHAR(255) NOT NULL COMMENT '模块名称',
    `model_description` VARCHAR(255) DEFAULT '' COMMENT '模块描述',
    `meta_data` JSON COMMENT '模块元数据',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0表示未删除，1表示已删除',
    PRIMARY KEY (`id`)
) COMMENT='模块表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 权限表
CREATE TABLE IF NOT EXISTS `permission_table` (
    `id` CHAR(32) CHARACTER SET ascii NOT NULL COMMENT '权限ID' DEFAULT(
        replace (
                uuid(),
                _utf8mb3 '-',
                _utf8mb3 ''
            )
    ),
    `permission_string` VARCHAR(255) NOT NULL UNIQUE COMMENT '权限字符串',
    `permission_name` VARCHAR(255) NOT NULL COMMENT '权限名称',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0表示未删除，1表示已删除',
    PRIMARY KEY (`id`),
    INDEX `idx_permission_string` (`permission_string`)
) COMMENT='权限表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 角色表
CREATE TABLE IF NOT EXISTS `role_table` (
    `id` CHAR(32) CHARACTER SET ascii NOT NULL COMMENT '角色ID' DEFAULT(
        replace (
                uuid(),
                _utf8mb3 '-',
                _utf8mb3 ''
            )
    ),
    `role_name` VARCHAR(255) NOT NULL UNIQUE COMMENT '角色名称',
    `role_description` VARCHAR(255) DEFAULT '' COMMENT '角色描述',
    `role_status` VARCHAR(50) DEFAULT 'active' COMMENT '角色状态',
    `default_role` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否默认角色',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0表示未删除，1表示已删除',
    PRIMARY KEY (`id`),
    INDEX `idx_role_name` (`role_name`)
) COMMENT='角色表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 模块权限关联表
CREATE TABLE IF NOT EXISTS `model_permission_table` (
    `id` CHAR(32) CHARACTER SET ascii NOT NULL COMMENT '关联ID' DEFAULT(
        replace (
                uuid(),
                _utf8mb3 '-',
                _utf8mb3 ''
            )
    ),
    `model_id` CHAR(32) CHARACTER SET ascii NOT NULL COMMENT '模块ID',
    `permission_id` CHAR(32) CHARACTER SET ascii NOT NULL COMMENT '权限ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0表示未删除，1表示已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_model_permission` (`model_id`, `permission_id`),
    FOREIGN KEY (`model_id`) REFERENCES `model_table` (`id`),
    FOREIGN KEY (`permission_id`) REFERENCES `permission_table` (`id`)
) COMMENT='模块权限关联表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS `role_permission_table` (
    `id` CHAR(32) CHARACTER SET ascii NOT NULL COMMENT '关联ID' DEFAULT(
        replace (
                uuid(),
                _utf8mb3 '-',
                _utf8mb3 ''
            )
    ),
    `role_id` CHAR(32) CHARACTER SET ascii NOT NULL COMMENT '角色ID',
    `permission_id` CHAR(32) CHARACTER SET ascii NOT NULL COMMENT '权限ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0表示未删除，1表示已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    FOREIGN KEY (`role_id`) REFERENCES `role_table` (`id`),
    FOREIGN KEY (`permission_id`) REFERENCES `permission_table` (`id`)
) COMMENT='角色权限关联表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS `user_role_table` (
    `id` CHAR(32) CHARACTER SET ascii NOT NULL COMMENT '关联ID' DEFAULT(
        replace (
                uuid(),
                _utf8mb3 '-',
                _utf8mb3 ''
            )
    ),
    `user_id` CHAR(32) CHARACTER SET ascii NOT NULL COMMENT '用户ID',
    `role_id` CHAR(32) CHARACTER SET ascii NOT NULL COMMENT '角色ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0表示未删除，1表示已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user_table` (`id`),
    FOREIGN KEY (`role_id`) REFERENCES `role_table` (`id`)
) COMMENT='用户角色关联表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 微信用户关联表
CREATE TABLE IF NOT EXISTS `wechat_user_table` (
    `id` CHAR(32) CHARACTER SET ascii NOT NULL COMMENT '关联ID' DEFAULT(
        replace (
                uuid(),
                _utf8mb3 '-',
                _utf8mb3 ''
            )
    ),
    `user_id` CHAR(32) CHARACTER SET ascii NOT NULL COMMENT '用户ID',
    `wechat_openid` VARCHAR(255) NOT NULL UNIQUE COMMENT '微信OpenID',
    `wechat_unionid` VARCHAR(255) DEFAULT '' COMMENT '微信UnionID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0表示未删除，1表示已删除',
    PRIMARY KEY (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `user_table` (`id`)
) COMMENT='微信用户关联表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 班级表
CREATE TABLE IF NOT EXISTS `class_table` (
    `id` CHAR(32) CHARACTER SET ascii NOT NULL COMMENT '班级ID' DEFAULT(
        replace (
                uuid(),
                _utf8mb3 '-',
                _utf8mb3 ''
            )
    ),
    `location` VARCHAR(255) DEFAULT '' COMMENT '班级地点',
    `capacity` INT NOT NULL DEFAULT 0 COMMENT '班级容量',
    `status` ENUM('active', 'inactive') NOT NULL DEFAULT 'active' COMMENT '班级状态：active表示可用，inactive表示不可用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0表示未删除，1表示已删除',
    PRIMARY KEY (`id`)
) COMMENT='班级表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 讲座表
CREATE TABLE IF NOT EXISTS `lecture_table` (
    `id` CHAR(32) CHARACTER SET ascii NOT NULL COMMENT '讲座ID' DEFAULT(
        replace (
                uuid(),
                _utf8mb3 '-',
                _utf8mb3 ''
            )
    ),
    `title` VARCHAR(255) NOT NULL COMMENT '讲座标题',
    `description` TEXT COMMENT '讲座描述',
    `cover_image_url` VARCHAR(255) DEFAULT '' COMMENT '讲座封面图片URL',
    `teacher_id` CHAR(32) DEFAULT '' COMMENT '讲师ID',
    `teacher_name` VARCHAR(255) DEFAULT '' COMMENT '讲师昵称（冗余）',
    `registration_starts_time` DATETIME NOT NULL COMMENT '报名开始时间',
    `registration_ends_time` DATETIME NOT NULL COMMENT '报名结束时间',
    `lecture_start_time` DATETIME NOT NULL COMMENT '讲座开始时间',
    `lecture_end_time` DATETIME NOT NULL COMMENT '讲座结束时间',
    `remaining` INT NOT NULL DEFAULT 0 COMMENT '剩余名额',
    `status` ENUM('draft', 'pending', 'reject','published', 'finished', 'cancelled') NOT NULL COMMENT '讲座状态：draft表示草稿，pending表示待审核，reject表示已驳回，published表示已发布，finished表示已结束，cancelled表示已取消',
    `reason` VARCHAR(255) DEFAULT '' COMMENT '驳回原因',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0表示未删除，1表示已删除',
    PRIMARY KEY (`id`)
) COMMENT='讲座表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 标签表
CREATE TABLE IF NOT EXISTS `tag_table` (
    `id` CHAR(32) CHARACTER SET ascii NOT NULL COMMENT '标签ID' DEFAULT(
        replace (
                uuid(),
                _utf8mb3 '-',
                _utf8mb3 ''
            )
    ),
    `tag_name` VARCHAR(50) NOT NULL COMMENT '标签名称',
    `tag_description` TEXT COMMENT '标签描述',
    `tag_type` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '标签类型',
    `tag_status` VARCHAR(50) NOT NULL DEFAULT 'draft' COMMENT '标签状态',
    `creator_id` CHAR(32) CHARACTER SET ascii NOT NULL COMMENT '创建者ID',
    `meta_data` JSON COMMENT '标签元数据',
    `is_system` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否为系统标签：0表示非系统标签，1表示系统标签',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0表示未删除，1表示已删除',
    PRIMARY KEY (`id`)
) COMMENT='标签表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 讲座标签关联表
CREATE TABLE IF NOT EXISTS `lecture_tag_table` (
    `id` CHAR(32) CHARACTER SET ascii NOT NULL COMMENT '关联ID' DEFAULT(
        replace (
                uuid(),
                _utf8mb3 '-',
                _utf8mb3 ''
            )
    ),
    `lecture_id` CHAR(32) CHARACTER SET ascii NOT NULL COMMENT '讲座ID',
    `tag_id` CHAR(32) CHARACTER SET ascii NOT NULL COMMENT '标签ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0表示未删除，1表示已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_lecture_tag` (`lecture_id`, `tag_id`),
    INDEX `idx_lecture_tag_lecture_id` (`lecture_id`),
    INDEX `idx_lecture_tag_tag_id` (`tag_id`),
    FOREIGN KEY (`lecture_id`) REFERENCES `lecture_table` (`id`),
    FOREIGN KEY (`tag_id`) REFERENCES `tag_table` (`id`)
) COMMENT='讲座标签关联表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 讲座审核记录表
CREATE TABLE IF NOT EXISTS `lecture_audit_table` (
    `id` CHAR(32) CHARACTER SET ascii NOT NULL COMMENT '审核记录ID' DEFAULT(
        replace (
                uuid(),
                _utf8mb3 '-',
                _utf8mb3 ''
            )
    ),
    `lecture_id` CHAR(32) CHARACTER SET ascii NOT NULL COMMENT '讲座ID',
    `lecture_title` VARCHAR(255) NOT NULL COMMENT '讲座标题快照',
    `audit_action` ENUM('approve', 'reject') NOT NULL COMMENT '审核动作：approve表示通过，reject表示驳回',
    `before_status` VARCHAR(50) NOT NULL COMMENT '审核前状态',
    `after_status` VARCHAR(50) NOT NULL COMMENT '审核后状态',
    `reason` VARCHAR(255) DEFAULT '' COMMENT '审核原因/驳回原因',
    `auditor_id` CHAR(32) CHARACTER SET ascii NOT NULL COMMENT '审核人ID',
    `auditor_name` VARCHAR(255) DEFAULT '' COMMENT '审核人昵称',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0表示未删除，1表示已删除',
    PRIMARY KEY (`id`),
    INDEX `idx_lecture_audit_lecture_id` (`lecture_id`),
    INDEX `idx_lecture_audit_auditor_id` (`auditor_id`),
    FOREIGN KEY (`lecture_id`) REFERENCES `lecture_table` (`id`),
    FOREIGN KEY (`auditor_id`) REFERENCES `user_table` (`id`)
) COMMENT='讲座审核记录表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 讲座班级关联表
CREATE TABLE IF NOT EXISTS `lecture_class_table` (
    `id` CHAR(32) CHARACTER SET ascii NOT NULL COMMENT '关联ID' DEFAULT(
        replace (
                uuid(),
                _utf8mb3 '-',
                _utf8mb3 ''
            )
    ),
    `lecture_id` CHAR(32) CHARACTER SET ascii UNIQUE NOT NULL COMMENT '讲座ID',
    `class_id` CHAR(32) CHARACTER SET ascii NOT NULL COMMENT '班级ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0表示未删除，1表示已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_lecture_class` (`lecture_id`, `class_id`),
    FOREIGN KEY (`lecture_id`) REFERENCES `lecture_table` (`id`),
    FOREIGN KEY (`class_id`) REFERENCES `class_table` (`id`)
) COMMENT='讲座班级关联表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 报名表
CREATE TABLE IF NOT EXISTS `registration_table` (
    `id` CHAR(32) CHARACTER SET ascii NOT NULL COMMENT '报名ID' DEFAULT(
        replace (
                uuid(),
                _utf8mb3 '-',
                _utf8mb3 ''
            )
    ),
    `user_id` CHAR(32) CHARACTER SET ascii NOT NULL COMMENT '用户ID',
    `lecture_id` CHAR(32) CHARACTER SET ascii NOT NULL COMMENT '讲座ID',
    `registration_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
    `status` ENUM('pending', 'cancelled', 'checked_in', 'not_signed_in') NOT NULL DEFAULT 'pending' COMMENT '报名状态：已报名(pending)，已取消(cancelled)，已签到(checked_in)，未签到(not signed in)',
    `check_in_time` DATETIME DEFAULT NULL COMMENT '签到时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0表示未删除，1表示已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_lecture_registration` (`user_id`, `lecture_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user_table` (`id`),
    FOREIGN KEY (`lecture_id`) REFERENCES `lecture_table` (`id`)
) COMMENT='报名表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 应用配置表（动态路由配置）
CREATE TABLE IF NOT EXISTS `app_config_table` (
    `id` CHAR(32) CHARACTER SET ascii NOT NULL COMMENT '配置ID',
    `config_data` JSON NOT NULL COMMENT '配置JSON',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0表示未删除，1表示已删除',
    PRIMARY KEY (`id`)
) COMMENT='应用配置表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
