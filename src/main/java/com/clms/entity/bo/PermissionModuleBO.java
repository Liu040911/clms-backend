package com.clms.entity.bo;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "PermissionModuleBO对象", description = "权限模块分组BO")
public class PermissionModuleBO {

    @Schema(description = "权限模块名称")
    private String moduleName;

    @Schema(description = "模块下的权限列表")
    private List<PermissionBO> permissions;
}