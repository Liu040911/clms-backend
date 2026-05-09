package com.clms.entity.bo;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageBO {
    private String role;

    private String content;

    private LocalDateTime createTime;
}
