package com.clms.service;

import java.util.List;

import com.clms.entity.po.PermissionTable;

public interface IRolePermissionService {
    
    // 根据角色id获取权限列表实体
    List<PermissionTable> getPermissionsByRoleId(List<String> roleId);

}
