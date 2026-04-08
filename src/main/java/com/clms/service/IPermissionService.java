package com.clms.service;

import java.util.List;
import java.util.Map;
import com.clms.entity.bo.PermissionBO;

public interface IPermissionService {
    PermissionBO getPermissionById(String permissionId);
    List<PermissionBO> getAllPermission();
    
    /**
     * 批量获取权限信息
     * 
     * @param permissionIds 权限ID列表
     * @return 权限ID到权限BO的映射
     */
    Map<String, PermissionBO> getBatchPermissions(List<String> permissionIds);
    
    /**
     * 添加权限
     * 
     * @param permissionBO 权限BO对象
     * @param appKey 应用键
     * @param appChannel 应用渠道
     * @return 新添加的权限ID
     */
    String addPermission(PermissionBO permissionBO);
}
