package com.clms.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LectureRegistrationDTO {

    @NotBlank(message = "讲座ID不能为空")
    private String lectureId;
}
