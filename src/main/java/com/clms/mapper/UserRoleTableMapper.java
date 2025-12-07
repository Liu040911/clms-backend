package com.clms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clms.entity.po.RoleTable;
import com.clms.entity.po.UserRoleTable;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

/**
 * 用户角色关联表 Mapper 接口
 * 
 * @author Liu
 * @since 1.0.0
 */
@Mapper
public interface UserRoleTableMapper extends BaseMapper<UserRoleTable> {
    // 根据用户id查询出该用户已有角色
    public List<RoleTable> getRolesByUserId(String userId);
}
