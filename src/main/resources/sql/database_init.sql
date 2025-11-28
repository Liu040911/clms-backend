-- Active: 1764214974199@@192.168.10.134@3306@clms
SET NAMES utf8mb4;
SET character_set_client = 'utf8mb4';
SET character_set_results = 'utf8mb4';
SET character_set_connection = 'utf8mb4';
SET collation_connection = 'utf8mb4_unicode_ci';

USE clms;

CREATE TABLE IF NOT EXISTS `user_table` (
    `id` CHAR(32) CHARACTER SET ascii NOT NULL COMMENT '用户ID' DEFAULT(
        replace (
                uuid(),
                _utf8mb3 '-',
                _utf8mb3 ''
            )
    ),
    `nickname` VARCHAR(255) DEFAULT '' COMMENT '用户昵称',
    `gender` VARCHAR(255) DEFAULT '' COMMENT '用户性别',
    `email` VARCHAR(255) DEFAULT '' COMMENT '用户邮箱',
    `phone` VARCHAR(255) DEFAULT '' COMMENT '用户手机号',
    `password` VARCHAR(255) DEFAULT '' COMMENT '用户密码 (加密)',
    `class_id` CHAR(32) DEFAULT NULL COMMENT '班级id',
    `college_id` CHAR(32) DEFAULT NULL COMMENT '学院id',
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