package com.clms.service.data.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clms.entity.po.RoleTable;
import com.clms.mapper.RoleTableMapper;
import com.clms.service.data.IRoleTableService;
import org.springframework.stereotype.Service;

/**
 * 角色表 Service 实现类
 * 
 * @author Liu
 * @since 1.0.0
 */
@Service
public class RoleTableServiceImpl extends ServiceImpl<RoleTableMapper, RoleTable> implements IRoleTableService {

}
