package com.clms.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clms.entity.bo.PermissionBO;
import com.clms.entity.po.PermissionTable;
import com.clms.service.IPermissionService;
import com.clms.service.data.IPermissionTableService;
import com.clms.utils.CommonUtil;

import jakarta.annotation.Resource;

@Service
public class PermissionServiceImpl implements IPermissionService {
    @Resource
    private IPermissionTableService permissionTableService;

    @Override
    public PermissionBO getPermissionById(String permissionId) {
        PermissionTable byId = permissionTableService.getById(permissionId);
        if (byId == null) {
            return null;
        }
        return new PermissionBO(byId);
    }

    @Override
    public List<PermissionBO> getAllPermission() {
        List<PermissionTable> list = permissionTableService
                .lambdaQuery()
                .orderByAsc(PermissionTable::getPermissionName)
                .list();
        return list.stream().map(PermissionBO::new).toList();
    }

    @Override
    public Map<String, PermissionBO> getBatchPermissions(List<String> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return new HashMap<>();
        }
        
        List<PermissionTable> permissions = permissionTableService.lambdaQuery()
                .in(PermissionTable::getId, permissionIds)
                .list();
        
        return permissions.stream()
                .map(PermissionBO::new)
                .collect(Collectors.toMap(PermissionBO::getId, permission -> permission));
    }
    
    @Override
    @Transactional
    public String addPermission(PermissionBO permissionBO) {
        // 创建PermissionTable对象
        PermissionTable permissionTable = new PermissionTable();
        
        // 设置ID
        String permissionId = CommonUtil.generateUuidV7();
        permissionTable.setId(permissionId);
        
        // 从PermissionBO复制相关属性
        permissionTable.setPermissionString(permissionBO.getPermissionString());
        permissionTable.setPermissionName(permissionBO.getPermissionName());

        // 保存到数据库
        permissionTableService.save(permissionTable);
        
        return permissionId;
    }
}
