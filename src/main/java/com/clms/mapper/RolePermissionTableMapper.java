package com.clms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clms.entity.po.RolePermissionTable;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色权限关联表 Mapper 接口
 * 
 * @author clms-backend
 * @since 1.0.0
 */
@Mapper
public interface RolePermissionTableMapper extends BaseMapper<RolePermissionTable> {

}
