package com.clms.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户修改个人信息DTO")
public class UserUpdateInfoDTO {

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "性别: 0=未知 1=男 2=女")
    private String gender;
}
