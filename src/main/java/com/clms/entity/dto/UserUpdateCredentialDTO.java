package com.clms.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "用户更新手机号或邮箱DTO")
public class UserUpdateCredentialDTO {

    @NotBlank(message = "类型不能为空")
    @Schema(description = "类型: phone 或 email", requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;

    @NotBlank(message = "新手机号或邮箱不能为空")
    @Schema(description = "新手机号或新邮箱", requiredMode = Schema.RequiredMode.REQUIRED)
    private String value;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码（新手机号或邮箱的验证码）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @NotBlank(message = "验证令牌不能为空")
    @Schema(description = "旧手机号/邮箱验证通过的步骤令牌", requiredMode = Schema.RequiredMode.REQUIRED)
    private String stepToken;
}
