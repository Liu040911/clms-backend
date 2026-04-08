package com.clms.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.clms.entity.base.BasePO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "lecture_class_table", autoResultMap = true)
@Schema(name = "LectureClassTable对象", description = "讲座教室关联表")
public class LectureClassTable extends BasePO {

    @Schema(description = "讲座ID")
    private String lectureId;

    @Schema(description = "教室ID")
    private String classId;
}
