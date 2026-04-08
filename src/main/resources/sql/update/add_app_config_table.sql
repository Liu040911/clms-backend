CREATE TABLE IF NOT EXISTS `app_config_table` (
    `id` CHAR(32) CHARACTER SET ascii NOT NULL COMMENT '配置ID',
    `app_key` VARCHAR(64) NOT NULL DEFAULT 'default' COMMENT '应用标识',
    `app_channel` VARCHAR(64) NOT NULL DEFAULT 'default' COMMENT '应用渠道',
    `config_data` JSON NOT NULL COMMENT '配置JSON',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0表示未删除，1表示已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_app_key_channel` (`app_key`, `app_channel`)
) COMMENT='应用配置表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `app_config_table` (`id`, `app_key`, `app_channel`, `config_data`, `deleted`)
SELECT
    'b3d2f4a7dcd94d8da50d38a8a1a80a11',
    'default',
    'default',
    JSON_OBJECT(
        'pageConfig',
        JSON_OBJECT(
            'my',
            JSON_ARRAY(
                JSON_OBJECT(
                    'title', '我的预约',
                    'icon', 'i-carbon-calendar',
                    'url', '/pages-sub/lecture/my-appointments/index',
                    'disabled', false
                ),
                JSON_OBJECT(
                    'title', '申请讲座',
                    'icon', 'i-carbon-presentation-file',
                    'url', '/pages-sub/lecture/apply/index',
                    'disabled', false,
                    'roles', JSON_ARRAY('teacher', '教师')
                )
            )
        )
    ),
    0
WHERE NOT EXISTS (
    SELECT 1 FROM `app_config_table` WHERE `app_key` = 'default' AND `app_channel` = 'default'
);
