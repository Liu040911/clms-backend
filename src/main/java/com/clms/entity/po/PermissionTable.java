package com.clms.entity.po;


import com.baomidou.mybatisplus.annotation.TableName;
import com.clms.entity.base.BasePO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName(value = "permission_table", autoResultMap = true)
@Schema(name = "PermissionTable对象", description = "权限表")
public class PermissionTable extends BasePO {
    private static final long serialVersionUID = 1345347467L;
    /**
     * 权限字符串
     */
    @Schema(description = "权限字符串")
    private String permissionString;

    /**
     * 权限描述
     */
    @Schema(description = "权限名称")
    private String permissionName;
}
