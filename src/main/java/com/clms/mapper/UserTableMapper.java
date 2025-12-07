package com.clms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clms.entity.po.UserTable;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表 Mapper 接口
 * 
 * @author Liu
 * @since 1.0.0
 */
@Mapper
public interface UserTableMapper extends BaseMapper<UserTable> {

}
