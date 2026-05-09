package com.clms.service.data.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clms.entity.po.ChatMessageTable;
import com.clms.mapper.ChatMessageTableMapper;
import com.clms.service.data.IChatMessageTableService;

@Service
public class ChatMessageTableServiceImpl extends ServiceImpl<ChatMessageTableMapper, ChatMessageTable> implements IChatMessageTableService {

}
