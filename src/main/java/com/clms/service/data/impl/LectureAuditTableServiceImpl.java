package com.clms.service.data.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clms.entity.po.LectureAuditTable;
import com.clms.mapper.LectureAuditTableMapper;
import com.clms.service.data.ILectureAuditTableService;

@Service
public class LectureAuditTableServiceImpl extends ServiceImpl<LectureAuditTableMapper, LectureAuditTable>
        implements ILectureAuditTableService {

}
