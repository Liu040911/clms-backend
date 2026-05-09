package com.clms.entity.po;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName(value = "chat_message_table", autoResultMap = true)
public class ChatMessageTable {

    @TableId(type = IdType.INPUT)
    private String id;

    private String sessionId;

    private String role;

    private String content;

    private String toolName;

    private String toolCallId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
