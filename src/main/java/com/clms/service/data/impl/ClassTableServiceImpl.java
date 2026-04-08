package com.clms.service.data.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clms.entity.po.ClassTable;
import com.clms.mapper.ClassTableMapper;
import com.clms.service.data.IClassTableService;

@Service
public class ClassTableServiceImpl extends ServiceImpl<ClassTableMapper, ClassTable> implements IClassTableService {

}
