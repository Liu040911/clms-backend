package com.clms.entity.bo;

import com.clms.entity.base.BaseBO;
import com.clms.entity.po.PermissionTable;

import cn.hutool.core.bean.BeanUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(name = "PermissionBO对象", description = "权限BO")
public class PermissionBO extends BaseBO {
    public PermissionBO(PermissionTable permissionTable) {
        BeanUtil.copyProperties(permissionTable, this);
    }
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
