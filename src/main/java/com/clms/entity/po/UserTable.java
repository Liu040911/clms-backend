package com.clms.entity.po;

import com.clms.handle.JSONArrayTypeHandler;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.clms.entity.base.BasePO;

import cn.hutool.json.JSONArray;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "user_table", autoResultMap = true)
@Schema(name = "UserTable对象", description = "用户表")
public class UserTable extends BasePO {
    
    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "用户性别")
    private String gender;

    @Schema(description = "用户邮箱")
    private String email;

    @Schema(description = "用户手机号")
    private String phone;

    @Schema(description = "用户密码 (加密)")
    private String password;

    @Schema(description = "用户头像URL")
    private String avatarUrl;

    @Schema(description = "班级id")
    private Long classId;

    @Schema(description = "学院id")
    private Long collegeId;

    @Schema(description = "用户角色")
    @TableField(typeHandler = JSONArrayTypeHandler.class, fill = FieldFill.INSERT, value = "user_roles")
    private JSONArray userRoles;

    @Schema(description = "用户权限")
    @TableField(typeHandler = JSONArrayTypeHandler.class, fill = FieldFill.INSERT, value = "user_permissions")
    private JSONArray userPermissions;

}
