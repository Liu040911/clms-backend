package com.clms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clms.entity.po.UserRoleTable;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户角色关联表 Mapper 接口
 * 
 * @author clms-backend
 * @since 1.0.0
 */
@Mapper
public interface UserRoleTableMapper extends BaseMapper<UserRoleTable> {

}
