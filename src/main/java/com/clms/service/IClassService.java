package com.clms.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clms.entity.bo.ClassBO;
import com.clms.entity.dto.ClassDTO;

public interface IClassService {

    void createClass(ClassDTO classDTO);

    void updateClass(String classId, ClassDTO classDTO);

    void deleteClass(String classId);

    ClassBO getClassInfo(String classId);

    Page<ClassBO> getClassList(String location, String status, Integer page, Integer size, String sort, String order);

    List<ClassBO> getAvailableClassList();
}
