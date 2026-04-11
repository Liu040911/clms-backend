package com.clms.service.data.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clms.entity.po.TagTable;
import com.clms.mapper.TagTableMapper;
import com.clms.service.data.ITagTableService;

@Service
public class TagTableServiceImpl extends ServiceImpl<TagTableMapper, TagTable> implements ITagTableService {

}
