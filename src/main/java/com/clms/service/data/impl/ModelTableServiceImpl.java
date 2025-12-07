package com.clms.service.data.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clms.entity.po.ModelTable;
import com.clms.mapper.ModelTableMapper;
import com.clms.service.data.IModelTableService;
import org.springframework.stereotype.Service;

/**
 * 模型表 Service 实现类
 * 
 * @author Liu
 * @since 1.0.0
 */
@Service
public class ModelTableServiceImpl extends ServiceImpl<ModelTableMapper, ModelTable> implements IModelTableService {

}
