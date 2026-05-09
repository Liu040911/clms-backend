package com.clms.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.clms.entity.base.BasePO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "chat_session_table", autoResultMap = true)
public class ChatSessionTable extends BasePO {

    private String userId;

    private String title;
}
