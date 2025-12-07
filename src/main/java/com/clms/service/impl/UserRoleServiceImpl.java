package com.clms.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clms.entity.po.RoleTable;
import com.clms.entity.po.UserRoleTable;
import com.clms.service.IUserRoleService;
import com.clms.service.data.IRoleTableService;
import com.clms.service.data.IUserRoleTableService;
import com.clms.utils.CommonUtil;

import jakarta.annotation.Resource;

@Service
public class UserRoleServiceImpl implements IUserRoleService {

    @Resource
    private IUserRoleTableService userRoleTableService;

    @Resource
    private IRoleTableService roleTableService;

    @Override
    public void bindRolesToUser(String userId, List<String> roleIds) {
        // 1、查询当前用户已有的角色关联
        List<RoleTable> existingRoles = userRoleTableService.getRolesByUserId(userId);
        List<String> existingRoleIds = existingRoles.stream()
                .map(RoleTable::getId)
                .toList();
        // 2、过滤用户已有的角色，只保留需要新增的角色关联
        List<String> newRoleIds = roleIds.stream()
                .filter(roleId -> !existingRoleIds.contains(roleId))
                .toList();
        // 3、创建用户角色关联实体列表
        List<UserRoleTable> userRoleEntities = newRoleIds.stream()
                .map(roleId -> {
                    UserRoleTable userRole = new UserRoleTable();
                    userRole.setId(CommonUtil.generateUuidV7());
                    userRole.setUserId(userId);
                    userRole.setRoleId(roleId);
                    return userRole;
                })
                .toList();
        // 4、批量保存新的用户角色关联记录
        userRoleTableService.saveBatch(userRoleEntities);
    }

    @Override
    public List<RoleTable> getDefaultRoles() {
        // 1. 从数据库查询所有默认角色（defaultRole = true）
        LambdaQueryWrapper<RoleTable> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleTable::isDefaultRole, true);
        
        List<RoleTable> defaultRoles = roleTableService.list(wrapper);
        
        // 2. 返回默认角色实体
        return defaultRoles;
    }
    
    
}
