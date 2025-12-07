package com.clms.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户注册请求DTO")
public class AccountRegisterDTO {
    
    @Schema(description = "用户昵称", example = "张三")
    private String nickname;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "验证码", example = "123456")
    private String verificationCode;

    @Schema(description = "密码", example = "P@ssw0rd!")
    private String password;
}
