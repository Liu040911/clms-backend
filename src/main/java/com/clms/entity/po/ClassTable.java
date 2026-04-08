package com.clms.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.clms.entity.base.BasePO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "class_table", autoResultMap = true)
@Schema(name = "ClassTable对象", description = "班级表")
public class ClassTable extends BasePO {

    @Schema(description = "班级地点")
    private String location;

    @Schema(description = "班级容量")
    private Integer capacity;

    @Schema(description = "班级状态：active/inactive")
    private String status;
}
