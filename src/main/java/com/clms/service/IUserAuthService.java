package com.clms.service;

import java.util.List;

import com.clms.entity.bo.PermissionBO;
import com.clms.entity.bo.RoleBO;

public interface IUserAuthService {

    /**
     * 获取用户角色列表
     * 
     * @param userId 用户ID
     * @return 角色列表
     */
    List<RoleBO> getUserRoleListByUserId(String userId);

    /**
     * 获取用户的权限列表
     * 
     * @param userId 用户ID
     * @return 权限列表
    */
    List<PermissionBO> getPermissionListByUserId(String userId);

}
