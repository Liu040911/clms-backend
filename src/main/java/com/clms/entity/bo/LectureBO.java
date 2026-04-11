package com.clms.entity.bo;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import com.clms.entity.po.LectureTable;

import lombok.Data;

@Data
public class LectureBO {

    private String id;

    private String title;

    private String description;

    private String coverImageUrl;

    private String teacherId;

    private String teacherName;

    private Timestamp registrationStartsTime;

    private Timestamp registrationEndsTime;

    private Timestamp lectureStartTime;

    private Timestamp lectureEndTime;

    private Integer remaining;

    private String status;

    private String reason;

    private String classId;

    private String location;

    private List<LectureTagBO> tags;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public LectureBO(LectureTable table) {
        this.id = table.getId();
        this.title = table.getTitle();
        this.description = table.getDescription();
        this.coverImageUrl = table.getCoverImageUrl();
        this.teacherId = table.getTeacherId();
        this.teacherName = table.getTeacherName();
        this.registrationStartsTime = table.getRegistrationStartsTime();
        this.registrationEndsTime = table.getRegistrationEndsTime();
        this.lectureStartTime = table.getLectureStartTime();
        this.lectureEndTime = table.getLectureEndTime();
        this.remaining = table.getRemaining();
        this.status = table.getStatus();
        this.reason = table.getReason();
        this.createTime = table.getCreateTime();
        this.updateTime = table.getUpdateTime();
        this.tags = List.of();
    }
}
