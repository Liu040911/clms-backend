package com.clms.service.data.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clms.entity.po.RoleTable;
import com.clms.entity.po.UserRoleTable;
import com.clms.mapper.UserRoleTableMapper;
import com.clms.service.data.IUserRoleTableService;

import jakarta.annotation.Resource;

import java.util.List;

import org.springframework.stereotype.Service;

/**
 * 用户角色关联表 Service 实现类
 * 
 * @author Liu
 * @since 1.0.0
 */
@Service
public class UserRoleTableServiceImpl extends ServiceImpl<UserRoleTableMapper, UserRoleTable> implements IUserRoleTableService {

    @Resource
    private UserRoleTableMapper userRoleTableMapper;

    @Override
    public List<RoleTable> getRolesByUserId(String userId) {
        return userRoleTableMapper.getRolesByUserId(userId);
    }



}
