package com.clms.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.clms.entity.base.BasePO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "role_permission_table", autoResultMap = true)
@Schema(name = "RolePermissionTable对象", description = "角色权限表")
public class RolePermissionTable extends BasePO {

    private static final long serialVersionUID = 157984984L;

    @Schema(description = "角色ID")
    private String roleId;

    @Schema(description = "权限ID")
    private String permissionId;
    
}
