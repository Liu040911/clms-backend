package com.clms.enums;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LectureStatusEnum {

    DRAFT("draft", "草稿"),
    PENDING("pending", "待审核"),
    PUBLISHED("published", "已发布"),
    FINISHED("finished", "已结束"),
    CANCELLED("cancelled", "已取消");

    private final String status;
    private final String description;

    public static List<String> occupiedStatuses() {
        return List.of(DRAFT.status, PENDING.status, PUBLISHED.status);
    }
}
