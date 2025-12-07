package com.clms.entity.bo;

import java.util.List;

import com.clms.entity.base.BaseBO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户信息BO
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "用户信息BO")
public class UserInfoBO extends BaseBO {
    
    @Schema(description = "用户名")
    private String username;
    
    @Schema(description = "用户昵称")
    private String nickname;
    
    @Schema(description = "头像URL")
    private String avatar;
    
    @Schema(description = "手机号")
    private String phone;
    
    @Schema(description = "邮箱")
    private String email;
    
    @Schema(description = "性别")
    private String gender;

    @Schema(description = "班级名称")
    private String className;

    @Schema(description = "学院名称")
    private String collegeName;

    @Schema(description = "当前登录用户的角色")
    private List<String> roles;

    @Schema(description = "按钮级别权限")
    private List<String> permissions;
}
