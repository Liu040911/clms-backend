package com.clms.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.clms.entity.base.BasePO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "lecture_tag_table", autoResultMap = true)
public class LectureTagTable extends BasePO {
    private String id;

    private String lectureId;

    private String tagId;
}
