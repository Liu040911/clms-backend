package com.clms.service.data.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clms.entity.po.AppConfigTable;
import com.clms.mapper.AppConfigTableMapper;
import com.clms.service.data.IAppConfigTableService;

@Service
public class AppConfigTableServiceImpl extends ServiceImpl<AppConfigTableMapper, AppConfigTable> implements IAppConfigTableService {

}
