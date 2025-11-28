package com.clms.service.data.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clms.entity.po.ModelPermissionTable;
import com.clms.mapper.ModelPermissionTableMapper;
import com.clms.service.data.IModelPermissionTableService;
import org.springframework.stereotype.Service;

/**
 * 模型权限表 Service 实现类
 * 
 * @author clms-backend
 * @since 1.0.0
 */
@Service
public class ModelPermissionTableServiceImpl extends ServiceImpl<ModelPermissionTableMapper, ModelPermissionTable> implements IModelPermissionTableService {

}
