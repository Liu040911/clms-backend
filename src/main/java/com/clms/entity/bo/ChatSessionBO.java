package com.clms.entity.bo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ChatSessionBO {
    private String id;

    private String title;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
