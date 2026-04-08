package com.clms.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.clms.entity.po.PermissionTable;
import com.clms.entity.po.RoleTable;
import com.clms.entity.bo.UserInfoBO;
import com.clms.entity.po.UserTable;
import com.clms.exception.BusinessException;
import com.clms.service.IUserAccountService;
import com.clms.service.data.IRolePermissionTableService;
import com.clms.service.data.IUserRoleTableService;
import com.clms.service.data.impl.UserTableServiceImpl;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;

@Service
public class UserAccountServiceImpl implements IUserAccountService {
    
    @Resource
    private UserTableServiceImpl userTableService;

    @Resource
    private IUserRoleTableService userRoleTableService;

    @Resource
    private IRolePermissionTableService rolePermissionTableService;

    @Override
    public UserInfoBO getCurrentUserInfo(String userId) {

        // 1. 查询用户信息
        UserTable user = userTableService.getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        
        // 2. 构建返回对象
        UserInfoBO userInfo = new UserInfoBO();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getNickname()); // 使用用户昵称作为用户名
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatar(user.getAvatarUrl());
        userInfo.setPhone(user.getPhone());
        userInfo.setEmail(user.getEmail());
        userInfo.setGender(user.getGender());
        userInfo.setCreateTime(user.getCreateTime());
        userInfo.setUpdateTime(user.getUpdateTime());
        
        List<RoleTable> roleTables = userRoleTableService.getRolesByUserId(user.getId());
        List<String> roleNames = roleTables.stream()
            .map(RoleTable::getRoleName)
            .filter(StrUtil::isNotBlank)
            .distinct()
            .toList();

        List<String> roleIds = roleTables.stream()
            .map(RoleTable::getId)
            .filter(StrUtil::isNotBlank)
            .distinct()
            .toList();

        List<String> permissionStrings = roleIds.isEmpty()
            ? List.of()
            : rolePermissionTableService.getPermissionsByRoleIds(roleIds)
                .stream()
                .map(PermissionTable::getPermissionString)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .toList();

        userInfo.setRoles(roleNames);
        userInfo.setPermissions(permissionStrings);
        
        return userInfo;
    }

    @Override
    public void updateUserAvatar(String userId, String avatarUrl) {
        UserTable user = userTableService.getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setAvatarUrl(avatarUrl);
        userTableService.updateById(user); 
    }
}
