package com.clms.entity.bo;

import lombok.Data;

@Data
public class LectureAnalyticsOverviewBO {

    private Long totalLectures;
    private Long publishedLectures;
    private Long pendingLectures;
    private Long rejectedLectures;
    private Long finishedLectures;
    private Long cancelledLectures;

    private Long totalRegistrations;
    private Long totalCheckIns;
    private Long totalCancelledRegistrations;

    private Double checkInRate;
    private Double cancelRate;
    private Double avgAttendanceRate;
}
