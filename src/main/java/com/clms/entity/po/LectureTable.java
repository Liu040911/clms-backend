package com.clms.entity.po;

import java.sql.Timestamp;

import com.baomidou.mybatisplus.annotation.TableName;
import com.clms.entity.base.BasePO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "lecture_table", autoResultMap = true)
public class LectureTable extends BasePO {
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

}
