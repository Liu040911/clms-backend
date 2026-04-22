package com.clms.entity.bo;

import lombok.Data;

@Data
public class LectureAnalyticsTrendPointBO {

    private String time;
    private Long createdCount;
    private Long publishedCount;
    private Long registrationCount;
    private Long checkInCount;
    private Long cancelCount;
}
