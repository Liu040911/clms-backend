package com.clms.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.clms.entity.base.BasePO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "user_role_table", autoResultMap = true)
@Schema(name = "UserRoleTable对象", description = "用户角色表")
public class UserRoleTable extends BasePO {
    private static final long serialVersionUID = 123456789L;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "角色ID")
    private String roleId;
}
