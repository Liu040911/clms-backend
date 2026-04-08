package com.clms.handle;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.clms.entity.po.PermissionTable;
import com.clms.entity.po.RoleTable;
import com.clms.service.data.IRolePermissionTableService;
import com.clms.service.data.IUserRoleTableService;

import cn.dev33.satoken.stp.StpInterface;
import jakarta.annotation.Resource;

/**
 * 自定义权限加载接口实现类
 */
@Component
public class SaTokenHandler implements StpInterface {
    @Resource
    private IUserRoleTableService userRoleTableService;

    @Resource
    private IRolePermissionTableService rolePermissionTableService;

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        String userId = String.valueOf(loginId);
        List<RoleTable> roles = userRoleTableService.getRolesByUserId(userId);
        if (roles == null || roles.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> roleIds = roles.stream()
                .map(RoleTable::getId)
                .collect(Collectors.toList());

        List<PermissionTable> permissions = rolePermissionTableService.getPermissionsByRoleIds(roleIds);
        if (permissions == null || permissions.isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> permissionSet = permissions.stream()
                .map(PermissionTable::getPermissionString)
                .filter(permission -> permission != null && !permission.trim().isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return new ArrayList<>(permissionSet);
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        String userId = String.valueOf(loginId);
        List<RoleTable> roles = userRoleTableService.getRolesByUserId(userId);
        if (roles == null || roles.isEmpty()) {
            return new ArrayList<>();
        }

        return roles.stream()
                .map(RoleTable::getRoleName)
                .filter(roleName -> roleName != null && !roleName.trim().isEmpty())
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
