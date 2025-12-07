package com.clms.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.clms.entity.po.PermissionTable;
import com.clms.service.IRolePermissionService;
import com.clms.service.data.impl.RolePermissionTableServiceImpl;

import jakarta.annotation.Resource;

@Service
public class RolePermissionServiceImpl implements IRolePermissionService {

    @Resource
    private RolePermissionTableServiceImpl rolePermissionTableService;

    @Override
    public List<PermissionTable> getPermissionsByRoleId(List<String> roleId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPermissionsByRoleId'");
    }
    
}
