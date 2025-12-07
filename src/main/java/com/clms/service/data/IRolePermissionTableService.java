package com.clms.service.data;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.extension.service.IService;
import com.clms.entity.po.PermissionTable;
import com.clms.entity.po.RolePermissionTable;

/**
 * 角色权限关联表 Service 接口
 * 
 * @author Liu
 * @since 1.0.0
 */
public interface IRolePermissionTableService extends IService<RolePermissionTable> {
    // 根据角色ids获取权限实体
    List<PermissionTable> getPermissionsByRoleIds(@Param("roleIds") List<String> roleIds);
}
