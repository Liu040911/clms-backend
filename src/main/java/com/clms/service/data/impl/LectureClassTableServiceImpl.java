package com.clms.service.data.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clms.entity.po.LectureClassTable;
import com.clms.mapper.LectureClassTableMapper;
import com.clms.service.data.ILectureClassTableService;

@Service
public class LectureClassTableServiceImpl extends ServiceImpl<LectureClassTableMapper, LectureClassTable> implements ILectureClassTableService {

}
