package com.clms.handle;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.clms.entity.bo.PermissionBO;
import com.clms.entity.bo.RoleBO;
import com.clms.entity.po.UserTable;
import com.clms.service.IUserAuthService;
import com.clms.service.data.IUserTableService;

import cn.dev33.satoken.stp.StpInterface;
import jakarta.annotation.Resource;

/**
 * 自定义权限加载接口实现类
 */
@Component
public class SaTokenHandler implements StpInterface {
    @Resource
    private IUserAuthService userAuthService;
    @Resource
    private IUserTableService userTableService;

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        UserTable userTable = userTableService.getById((String) loginId);
        return userAuthService.getPermissionListByUserId(
                userTable.getId())
                .stream()
                .map(PermissionBO::getPermissionString)
                .collect(Collectors.toList());
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        UserTable userTable = userTableService.getById((String) loginId);
        return userAuthService.getUserRoleListByUserId(
                userTable.getId())
                .stream()
                .map(RoleBO::getRoleName)
                .collect(Collectors.toList());
    }
}
