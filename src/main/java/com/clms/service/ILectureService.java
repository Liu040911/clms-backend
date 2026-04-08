package com.clms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clms.entity.bo.LectureBO;
import com.clms.entity.dto.LectureDTO;

public interface ILectureService {

    void createLecture(LectureDTO lectureDTO);

    void updateLecture(String lectureId, LectureDTO lectureDTO);

    void deleteLecture(String lectureId);

    LectureBO getLectureInfo(String lectureId);

    Page<LectureBO> getLectureList(String title, String status, String teacherId, Integer page, Integer size, String sort,
            String order);
}
