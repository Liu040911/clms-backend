package com.clms.service.data.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clms.entity.po.PermissionTable;
import com.clms.entity.po.RolePermissionTable;
import com.clms.mapper.PermissionTableMapper;
import com.clms.service.data.IPermissionTableService;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

/**
 * 权限表 Service 实现类
 * 
 * @author Liu
 * @since 1.0.0
 */
@Service
public class PermissionTableServiceImpl extends ServiceImpl<PermissionTableMapper, PermissionTable> implements IPermissionTableService {

}
