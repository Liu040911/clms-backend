package com.clms.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.clms.entity.base.BasePO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "model_permission_table", autoResultMap = true)
@Schema(name = "ModelPermissionTable对象", description = "模块权限表")
public class ModelPermissionTable extends BasePO {
    
    private static final long serialVersionUID = 1233246356L;

    /**
     * 模块ID
     */
    @Schema(description = "模块ID")
    private String modelId;

    /**
     * 权限ID
     */
    @Schema(description = "权限ID")
    private String permissionId;
}
