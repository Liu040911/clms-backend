package com.clms.entity.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClassDTO {

    @NotBlank(message = "班级地点不能为空")
    private String location;

    @Min(value = 1, message = "班级容量必须大于0")
    private Integer capacity;

    private String status;
}
