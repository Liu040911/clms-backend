package com.clms.entity.dto;

import java.sql.Timestamp;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LectureDTO {

    @NotBlank(message = "讲座标题不能为空")
    private String title;

    @NotBlank(message = "讲座描述不能为空")
    private String description;

    @NotBlank(message = "讲座封面不能为空")
    private String coverImageUrl;

    @NotNull(message = "报名开始时间不能为空")
    private Timestamp registrationStartsTime;

    @NotNull(message = "报名结束时间不能为空")
    private Timestamp registrationEndsTime;

    @NotNull(message = "讲座开始时间不能为空")
    @JsonAlias("lectureStartsTime")
    private Timestamp lectureStartTime;

    @NotNull(message = "讲座结束时间不能为空")
    @JsonAlias("lectureEndsTime")
    private Timestamp lectureEndTime;

    private String teacherId;

    private String status;

    private String classId;

    // 兼容旧版前端提交的数组字段，业务层会校验只能有一个教室。
    @JsonAlias("classIds")
    private List<String> classIds;
}
