package com.clms.service.data.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clms.entity.po.RegistrationTable;
import com.clms.mapper.RegistrationTableMapper;
import com.clms.service.data.IRegistrationTableService;

@Service
public class RegistrationTableServiceImpl extends ServiceImpl<RegistrationTableMapper, RegistrationTable>
        implements IRegistrationTableService {
}
