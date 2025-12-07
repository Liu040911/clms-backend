package com.clms.service;

import java.util.List;

import com.clms.entity.po.RoleTable;

public interface IUserRoleService {
    /**
     * 为用户分配角色
     * 
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     */
    void bindRolesToUser(String userId, List<String> roleIds);

    /**
     * 获取默认角色字符串列表
    */
    List<RoleTable> getDefaultRoles();
}
