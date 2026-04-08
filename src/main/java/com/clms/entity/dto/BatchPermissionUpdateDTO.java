package com.clms.entity.dto;

import lombok.Data;
import java.util.List;

@Data
public class BatchPermissionUpdateDTO {
    /**
     * 需要绑定的权限ID列表
     */
    private List<String> permissionsToBind;
    
    /**
     * 需要解绑的权限ID列表
     */
    private List<String> permissionsToUnbind;
}
