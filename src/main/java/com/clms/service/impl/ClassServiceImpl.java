package com.clms.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clms.entity.bo.ClassBO;
import com.clms.entity.dto.ClassDTO;
import com.clms.entity.po.ClassTable;
import com.clms.entity.po.LectureClassTable;
import com.clms.entity.po.LectureTable;
import com.clms.enums.LectureStatusEnum;
import com.clms.exception.BusinessException;
import com.clms.service.IClassService;
import com.clms.service.data.IClassTableService;
import com.clms.service.data.ILectureClassTableService;
import com.clms.service.data.ILectureTableService;
import com.clms.utils.CommonUtil;
import com.clms.utils.DataContainerConvertor;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;

@Service
public class ClassServiceImpl implements IClassService {

    @Resource
    private IClassTableService classTableService;

    @Resource
    private ILectureTableService lectureTableService;

    @Resource
    private ILectureClassTableService lectureClassTableService;

    @Override
    public void createClass(ClassDTO classDTO) {
        ClassTable classTable = new ClassTable();
        classTable.setId(CommonUtil.generateUuidV7());
        classTable.setLocation(classDTO.getLocation());
        classTable.setCapacity(classDTO.getCapacity());
        classTable.setStatus(StrUtil.isBlank(classDTO.getStatus()) ? "active" : classDTO.getStatus());

        boolean saved = classTableService.save(classTable);
        if (!saved) {
            throw new BusinessException(500, "创建班级失败");
        }
    }

    @Override
    public void updateClass(String classId, ClassDTO classDTO) {
        ClassTable existed = classTableService.getById(classId);
        if (existed == null) {
            throw new BusinessException(404, "班级不存在");
        }

        existed.setLocation(classDTO.getLocation());
        existed.setCapacity(classDTO.getCapacity());
        existed.setStatus(StrUtil.isBlank(classDTO.getStatus()) ? "active" : classDTO.getStatus());

        boolean updated = classTableService.updateById(existed);
        if (!updated) {
            throw new BusinessException(500, "修改班级失败");
        }
    }

    @Override
    public void deleteClass(String classId) {
        ClassTable existed = classTableService.getById(classId);
        if (existed == null) {
            throw new BusinessException(404, "班级不存在");
        }

        boolean removed = classTableService.removeById(classId);
        if (!removed) {
            throw new BusinessException(500, "删除班级失败");
        }
    }

    @Override
    public ClassBO getClassInfo(String classId) {
        ClassTable classTable = classTableService.getById(classId);
        if (classTable == null) {
            throw new BusinessException(404, "班级不存在");
        }
        return new ClassBO(classTable);
    }

    @Override
    public Page<ClassBO> getClassList(String location, String status, Integer page, Integer size, String sort, String order) {
        LambdaQueryChainWrapper<ClassTable> query = classTableService.lambdaQuery();

        if (StrUtil.isNotBlank(location)) {
            query.like(ClassTable::getLocation, location);
        }
        if (StrUtil.isNotBlank(status)) {
            query.eq(ClassTable::getStatus, status);
        }

        if (StrUtil.isNotBlank(sort) && StrUtil.isNotBlank(order)) {
            boolean isAsc = "asc".equalsIgnoreCase(order);
            if ("location".equals(sort)) {
                query = isAsc ? query.orderByAsc(ClassTable::getLocation) : query.orderByDesc(ClassTable::getLocation);
            } else if ("capacity".equals(sort)) {
                query = isAsc ? query.orderByAsc(ClassTable::getCapacity) : query.orderByDesc(ClassTable::getCapacity);
            } else if ("status".equals(sort)) {
                query = isAsc ? query.orderByAsc(ClassTable::getStatus) : query.orderByDesc(ClassTable::getStatus);
            } else if ("createTime".equals(sort)) {
                query = isAsc ? query.orderByAsc(ClassTable::getCreateTime) : query.orderByDesc(ClassTable::getCreateTime);
            } else if ("updateTime".equals(sort)) {
                query = isAsc ? query.orderByAsc(ClassTable::getUpdateTime) : query.orderByDesc(ClassTable::getUpdateTime);
            }
        } else {
            query = query.orderByDesc(ClassTable::getCreateTime);
        }

        Page<ClassTable> tablePage = query.page(new Page<>(page, size));
        return DataContainerConvertor.convertPage(tablePage, ClassTable.class, ClassBO.class);
    }

    @Override
    /**
     * 获取当前可用教室列表。
     *
     * <p>占用判定规则：当教室关联的讲座状态属于「草稿(draft)、待审核(pending)、已发布(published)」
     * 时，视为该教室被占用，不可用于新的讲座申请。</p>
     *
     * <p>查询流程：</p>
     * <ol>
     *   <li>先查询处于占用状态的讲座ID集合；</li>
     *   <li>再通过讲座-教室关联表查询被占用的教室ID集合；</li>
     *   <li>最后从教室表中过滤掉被占用教室，并仅返回状态为 active 的教室。</li>
     * </ol>
     *
     * @return 可用教室列表（按教室地点升序）
     */
    public List<ClassBO> getAvailableClassList() {
        List<String> occupiedLectureIds = lectureTableService.lambdaQuery()
                .in(LectureTable::getStatus, LectureStatusEnum.occupiedStatuses())
                .list()
                .stream()
                .map(LectureTable::getId)
                .toList();

        List<String> occupiedClassIds = occupiedLectureIds.isEmpty()
                ? List.of()
                : lectureClassTableService.lambdaQuery()
                        .in(LectureClassTable::getLectureId, occupiedLectureIds)
                        .list()
                        .stream()
                        .map(LectureClassTable::getClassId)
                        .distinct()
                        .toList();

        LambdaQueryChainWrapper<ClassTable> availableQuery = classTableService.lambdaQuery()
                .eq(ClassTable::getStatus, "active")
                .orderByAsc(ClassTable::getLocation);

        if (!occupiedClassIds.isEmpty()) {
            availableQuery = availableQuery.notIn(ClassTable::getId, occupiedClassIds);
        }

        List<ClassTable> availableClasses = availableQuery.list();
        return DataContainerConvertor.convertList(availableClasses, ClassTable.class, ClassBO.class);
    }
}
