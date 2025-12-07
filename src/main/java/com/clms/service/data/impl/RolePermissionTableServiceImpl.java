package com.clms.service.data.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clms.entity.po.PermissionTable;
import com.clms.entity.po.RolePermissionTable;
import com.clms.mapper.RolePermissionTableMapper;
import com.clms.service.data.IRolePermissionTableService;

import jakarta.annotation.Resource;

import java.util.List;

import org.springframework.stereotype.Service;

/**
 * 角色权限关联表 Service 实现类
 * 
 * @author Liu
 * @since 1.0.0
 */
@Service
public class RolePermissionTableServiceImpl extends ServiceImpl<RolePermissionTableMapper, RolePermissionTable> implements IRolePermissionTableService {

    @Resource
    private RolePermissionTableMapper rolePermissionTableMapper;

    @Override
    public List<PermissionTable> getPermissionsByRoleIds(List<String> roleIds) {
        return rolePermissionTableMapper.getPermissionsByRoleIds(roleIds);
    }
}
