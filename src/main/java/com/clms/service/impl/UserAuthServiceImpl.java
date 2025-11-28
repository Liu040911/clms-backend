package com.clms.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.clms.entity.bo.PermissionBO;
import com.clms.entity.bo.RoleBO;
import com.clms.service.IUserAuthService;
import com.clms.service.data.IUserTableService;

import jakarta.annotation.Resource;

@Service
public class UserAuthServiceImpl implements IUserAuthService {

    @Resource
    private IUserTableService userTableService;

    @Override
    public List<RoleBO> getUserRoleListByUserId(String userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUserRoleListByUserId'");
    }

    @Override
    public List<PermissionBO> getPermissionListByUserId(String userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPermissionListByUserId'");
    }
    
}
