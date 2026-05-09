-- 聊天会话表
CREATE TABLE IF NOT EXISTS chat_session_table (
    id             VARCHAR(32)  NOT NULL COMMENT '会话ID (UUID v7)',
    user_id        VARCHAR(32)  NOT NULL COMMENT '用户ID',
    title          VARCHAR(100) DEFAULT '新对话' COMMENT '会话标题',
    create_time    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted        TINYINT(1)   DEFAULT 0 COMMENT '逻辑删除: 0=否, 1=是',
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话表';

-- 聊天消息表
CREATE TABLE IF NOT EXISTS chat_message_table (
    id             VARCHAR(32)  NOT NULL COMMENT '消息ID (UUID v7)',
    session_id     VARCHAR(32)  NOT NULL COMMENT '会话ID',
    role           VARCHAR(25)  NOT NULL COMMENT '消息角色: SYSTEM/USER/ASSISTANT/TOOL_RESULT',
    content        TEXT         COMMENT 'JSON序列化的消息内容',
    tool_name      VARCHAR(100) COMMENT '工具名称 (TOOL_EXECUTION_RESULT)',
    tool_call_id   VARCHAR(100) COMMENT '工具调用ID (TOOL_EXECUTION_RESULT)',
    create_time    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_session_id (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';
