package com.clms.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.clms.entity.base.BasePO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "role_table", autoResultMap = true)
@Schema(name = "RoleTable对象", description = "角色表")
public class RoleTable extends BasePO {
    private static final long serialVersionUID = 129381287L;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "角色描述")
    private String roleDescription;

    @Schema(description = "角色状态")
    private String roleStatus;
    
    @Schema(description = "是否默认角色")
    private boolean defaultRole;
}
