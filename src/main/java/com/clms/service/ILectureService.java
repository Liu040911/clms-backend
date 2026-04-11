package com.clms.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clms.entity.bo.HotLectureBO;
import com.clms.entity.bo.LectureAuditBO;
import com.clms.entity.bo.LectureBO;
import com.clms.entity.bo.LectureTagBO;
import com.clms.entity.dto.LectureDTO;

public interface ILectureService {

    void createLecture(LectureDTO lectureDTO);

    void updateLecture(String lectureId, LectureDTO lectureDTO);

    void deleteLecture(String lectureId);

    void approveLecture(String lectureId);

    void rejectLecture(String lectureId, String reason);

    LectureBO getLectureInfo(String lectureId);

    Page<LectureAuditBO> getLectureAuditList(String lectureId, Integer page, Integer size);

    Page<LectureBO> getLectureList(String title, String status, String teacherId, String tagId, Integer page, Integer size, String sort,
            String order);

    List<LectureTagBO> getLectureTagList();

    List<HotLectureBO> getHotLectureList(String tagId, Integer limit);
}
