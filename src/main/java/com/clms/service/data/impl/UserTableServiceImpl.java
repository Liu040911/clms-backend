package com.clms.service.data.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clms.entity.po.UserTable;
import com.clms.mapper.UserTableMapper;
import com.clms.service.data.IUserTableService;
import org.springframework.stereotype.Service;

/**
 * 用户表 Service 实现类
 * 
 * @author clms-backend
 * @since 1.0.0
 */
@Service
public class UserTableServiceImpl extends ServiceImpl<UserTableMapper, UserTable> implements IUserTableService {
    
}
