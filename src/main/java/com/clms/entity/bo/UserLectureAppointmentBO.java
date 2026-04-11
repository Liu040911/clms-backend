package com.clms.entity.bo;

import java.sql.Timestamp;

import com.clms.entity.base.BaseBO;
import com.clms.entity.po.LectureTable;
import com.clms.entity.po.RegistrationTable;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserLectureAppointmentBO extends BaseBO {

    private String userId;

    private String lectureId;

    private Timestamp registrationTime;

    private String registrationStatus;

    private Timestamp checkInTime;

    private String title;

    private String coverImageUrl;

    private String teacherId;

    private String teacherName;

    private Timestamp registrationStartsTime;

    private Timestamp registrationEndsTime;

    private Timestamp lectureStartTime;

    private Timestamp lectureEndTime;

    private Integer remaining;

    private String lectureStatus;

    private String location;

    public UserLectureAppointmentBO(RegistrationTable registration, LectureTable lecture, String location) {
        this.setId(registration.getId());
        this.setCreateTime(registration.getCreateTime());
        this.setUpdateTime(registration.getUpdateTime());
        this.userId = registration.getUserId();
        this.lectureId = registration.getLectureId();
        this.registrationTime = registration.getRegistrationTime();
        this.registrationStatus = registration.getStatus();
        this.checkInTime = registration.getCheckInTime();

        if (lecture == null) {
            this.title = "讲座已删除";
            this.coverImageUrl = null;
            this.teacherId = null;
            this.teacherName = null;
            this.registrationStartsTime = null;
            this.registrationEndsTime = null;
            this.lectureStartTime = null;
            this.lectureEndTime = null;
            this.remaining = 0;
            this.lectureStatus = "deleted";
            this.location = null;
            return;
        }

        this.title = lecture.getTitle();
        this.coverImageUrl = lecture.getCoverImageUrl();
        this.teacherId = lecture.getTeacherId();
        this.teacherName = lecture.getTeacherName();
        this.registrationStartsTime = lecture.getRegistrationStartsTime();
        this.registrationEndsTime = lecture.getRegistrationEndsTime();
        this.lectureStartTime = lecture.getLectureStartTime();
        this.lectureEndTime = lecture.getLectureEndTime();
        this.remaining = lecture.getRemaining();
        this.lectureStatus = lecture.getStatus();
        this.location = location;
    }
}
