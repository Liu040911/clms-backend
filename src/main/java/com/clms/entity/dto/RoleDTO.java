package com.clms.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleDTO {
    @NotBlank(message = "角色名称不能为空")
    private String roleName;
    private String roleDescription;
    private String roleStatus;
    private boolean defaultRole;
}
