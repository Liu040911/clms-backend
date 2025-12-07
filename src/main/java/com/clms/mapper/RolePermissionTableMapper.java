package com.clms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clms.entity.po.PermissionTable;
import com.clms.entity.po.RolePermissionTable;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 角色权限关联表 Mapper 接口
 * 
 * @author Liu
 * @since 1.0.0
 */
@Mapper
public interface RolePermissionTableMapper extends BaseMapper<RolePermissionTable> {

    // 根据角色ids获取权限实体
    List<PermissionTable> getPermissionsByRoleIds(@Param("roleIds") List<String> roleIds);

}
