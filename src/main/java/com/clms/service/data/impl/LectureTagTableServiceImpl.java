package com.clms.service.data.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clms.entity.po.LectureTagTable;
import com.clms.mapper.LectureTagTableMapper;
import com.clms.service.data.ILectureTagTableService;

@Service
public class LectureTagTableServiceImpl extends ServiceImpl<LectureTagTableMapper, LectureTagTable>
        implements ILectureTagTableService {

}
