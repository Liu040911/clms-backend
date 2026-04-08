package com.clms.service.data.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clms.entity.po.LectureTable;
import com.clms.mapper.LectureTableMapper;
import com.clms.service.data.ILectureTableService;

@Service
public class LectureTableServiceImpl extends ServiceImpl<LectureTableMapper, LectureTable> implements ILectureTableService {

}
