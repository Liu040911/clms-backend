package com.clms.service.data.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clms.entity.po.ChatSessionTable;
import com.clms.mapper.ChatSessionTableMapper;
import com.clms.service.data.IChatSessionTableService;

@Service
public class ChatSessionTableServiceImpl extends ServiceImpl<ChatSessionTableMapper, ChatSessionTable> implements IChatSessionTableService {

}
