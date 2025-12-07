package com.clms.service.impl;

import org.springframework.stereotype.Service;

import com.clms.entity.bo.UserInfoBO;
import com.clms.entity.po.UserTable;
import com.clms.exception.BusinessException;
import com.clms.service.IUserAccountService;
import com.clms.service.data.impl.UserTableServiceImpl;

import jakarta.annotation.Resource;

@Service
public class UserAccountServiceImpl implements IUserAccountService {
    
    @Resource
    private UserTableServiceImpl userTableService;

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
        
        if (user.getUserRoles() != null) {
            userInfo.setRoles(user.getUserRoles().toList(String.class));
        }
        
        if (user.getUserPermissions() != null) {
            userInfo.setPermissions(user.getUserPermissions().toList(String.class));
        }
        
        return userInfo;
    }
}
