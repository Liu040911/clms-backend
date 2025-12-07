package com.clms.entity.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户登录凭证，邮箱/手机号/用户名必须有一个不为空，密码/代码必须有一个不为空，使用用户名登录必须使用密码")
public class UserCredentialDTO {
    @Schema(description = "邮箱/手机号")
    private String credential;
    @Schema(description = "密码")
    private String password;
    @Schema(description = "短信/邮箱代码")
    private String code;
}
