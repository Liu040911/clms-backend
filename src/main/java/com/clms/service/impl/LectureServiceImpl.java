package com.clms.service.impl;

import java.sql.Timestamp;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clms.entity.bo.LectureBO;
import com.clms.entity.dto.LectureDTO;
import com.clms.entity.po.ClassTable;
import com.clms.entity.po.LectureClassTable;
import com.clms.entity.po.LectureTable;
import com.clms.entity.po.UserTable;
import com.clms.enums.LectureStatusEnum;
import com.clms.exception.BusinessException;
import com.clms.service.ILectureService;
import com.clms.service.data.IClassTableService;
import com.clms.service.data.ILectureClassTableService;
import com.clms.service.data.ILectureTableService;
import com.clms.service.data.IUserTableService;
import com.clms.utils.CommonUtil;
import com.clms.utils.DataContainerConvertor;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;

@Service
public class LectureServiceImpl implements ILectureService {

    @Resource
    private ILectureTableService lectureTableService;

    @Resource
    private ILectureClassTableService lectureClassTableService;

    @Resource
    private IClassTableService classTableService;

    @Resource
    private IUserTableService userTableService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createLecture(LectureDTO lectureDTO) {
        validateLectureTimes(lectureDTO.getRegistrationStartsTime(), lectureDTO.getRegistrationEndsTime(),
                lectureDTO.getLectureStartTime(), lectureDTO.getLectureEndTime());

        String classId = resolveClassId(lectureDTO);
        ClassTable classTable = validateAndGetClass(classId);
        validateClassAvailability(classId, null);
        String teacherName = resolveTeacherName(lectureDTO.getTeacherId());

        LectureTable lectureTable = new LectureTable();
        lectureTable.setId(CommonUtil.generateUuidV7());
        lectureTable.setTitle(lectureDTO.getTitle());
        lectureTable.setDescription(lectureDTO.getDescription());
        lectureTable.setCoverImageUrl(lectureDTO.getCoverImageUrl());
        lectureTable.setTeacherId(lectureDTO.getTeacherId());
        lectureTable.setTeacherName(teacherName);
        lectureTable.setRegistrationStartsTime(lectureDTO.getRegistrationStartsTime());
        lectureTable.setRegistrationEndsTime(lectureDTO.getRegistrationEndsTime());
        lectureTable.setLectureStartTime(lectureDTO.getLectureStartTime());
        lectureTable.setLectureEndTime(lectureDTO.getLectureEndTime());
        lectureTable.setRemaining(classTable.getCapacity());
        lectureTable.setStatus(StrUtil.isBlank(lectureDTO.getStatus()) ? LectureStatusEnum.PENDING.getStatus() : lectureDTO.getStatus());

        boolean saved = lectureTableService.save(lectureTable);
        if (!saved) {
            throw new BusinessException(500, "创建讲座失败");
        }

        saveLectureClassRelation(lectureTable.getId(), classId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLecture(String lectureId, LectureDTO lectureDTO) {
        LectureTable existed = lectureTableService.getById(lectureId);
        if (existed == null) {
            throw new BusinessException(404, "讲座不存在");
        }

        validateLectureTimes(lectureDTO.getRegistrationStartsTime(), lectureDTO.getRegistrationEndsTime(),
                lectureDTO.getLectureStartTime(), lectureDTO.getLectureEndTime());

        String classId = resolveClassId(lectureDTO);
        ClassTable classTable = validateAndGetClass(classId);
        validateClassAvailability(classId, lectureId);
        String teacherName = resolveTeacherName(lectureDTO.getTeacherId());

        existed.setTitle(lectureDTO.getTitle());
        existed.setDescription(lectureDTO.getDescription());
        existed.setCoverImageUrl(lectureDTO.getCoverImageUrl());
        existed.setTeacherId(lectureDTO.getTeacherId());
        existed.setTeacherName(teacherName);
        existed.setRegistrationStartsTime(lectureDTO.getRegistrationStartsTime());
        existed.setRegistrationEndsTime(lectureDTO.getRegistrationEndsTime());
        existed.setLectureStartTime(lectureDTO.getLectureStartTime());
        existed.setLectureEndTime(lectureDTO.getLectureEndTime());
        existed.setRemaining(classTable.getCapacity());
        if (StrUtil.isNotBlank(lectureDTO.getStatus())) {
            existed.setStatus(lectureDTO.getStatus());
        }

        boolean updated = lectureTableService.updateById(existed);
        if (!updated) {
            throw new BusinessException(500, "修改讲座失败");
        }

        lectureClassTableService.lambdaUpdate()
                .eq(LectureClassTable::getLectureId, lectureId)
                .remove();
        saveLectureClassRelation(lectureId, classId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLecture(String lectureId) {
        LectureTable existed = lectureTableService.getById(lectureId);
        if (existed == null) {
            throw new BusinessException(404, "讲座不存在");
        }

        lectureClassTableService.lambdaUpdate()
                .eq(LectureClassTable::getLectureId, lectureId)
                .remove();

        boolean removed = lectureTableService.removeById(lectureId);
        if (!removed) {
            throw new BusinessException(500, "删除讲座失败");
        }
    }

    @Override
    public LectureBO getLectureInfo(String lectureId) {
        LectureTable lectureTable = lectureTableService.getById(lectureId);
        if (lectureTable == null) {
            throw new BusinessException(404, "讲座不存在");
        }

        LectureBO lectureBO = new LectureBO(lectureTable);
        LectureClassTable relation = lectureClassTableService.lambdaQuery()
                .eq(LectureClassTable::getLectureId, lectureId)
                .one();
        ClassTable classTable = relation == null ? null : classTableService.getById(relation.getClassId());
        lectureBO.setClassId(classTable == null ? null : classTable.getId());
        lectureBO.setLocation(classTable == null ? null : classTable.getLocation());
        return lectureBO;
    }

    @Override
    public Page<LectureBO> getLectureList(String title, String status, String teacherId, Integer page, Integer size,
            String sort, String order) {
        LambdaQueryChainWrapper<LectureTable> query = lectureTableService.lambdaQuery();

        if (StrUtil.isNotBlank(title)) {
            query.like(LectureTable::getTitle, title);
        }
        if (StrUtil.isNotBlank(status)) {
            query.eq(LectureTable::getStatus, status);
        }
        if (StrUtil.isNotBlank(teacherId)) {
            query.eq(LectureTable::getTeacherId, teacherId);
        }

        if (StrUtil.isNotBlank(sort) && StrUtil.isNotBlank(order)) {
            boolean isAsc = "asc".equalsIgnoreCase(order);
            if ("title".equals(sort)) {
                query = isAsc ? query.orderByAsc(LectureTable::getTitle) : query.orderByDesc(LectureTable::getTitle);
            } else if ("status".equals(sort)) {
                query = isAsc ? query.orderByAsc(LectureTable::getStatus) : query.orderByDesc(LectureTable::getStatus);
            } else if ("registrationStartsTime".equals(sort)) {
                query = isAsc ? query.orderByAsc(LectureTable::getRegistrationStartsTime)
                        : query.orderByDesc(LectureTable::getRegistrationStartsTime);
            } else if ("lectureStartTime".equals(sort)) {
                query = isAsc ? query.orderByAsc(LectureTable::getLectureStartTime)
                        : query.orderByDesc(LectureTable::getLectureStartTime);
            } else if ("createTime".equals(sort)) {
                query = isAsc ? query.orderByAsc(LectureTable::getCreateTime) : query.orderByDesc(LectureTable::getCreateTime);
            } else if ("updateTime".equals(sort)) {
                query = isAsc ? query.orderByAsc(LectureTable::getUpdateTime) : query.orderByDesc(LectureTable::getUpdateTime);
            }
        } else {
            query = query.orderByDesc(LectureTable::getCreateTime);
        }

        Page<LectureTable> tablePage = query.page(new Page<>(page, size));
        Page<LectureBO> boPage = DataContainerConvertor.convertPage(tablePage, LectureTable.class, LectureBO.class);

        List<String> lectureIds = boPage.getRecords().stream().map(LectureBO::getId).toList();
        if (lectureIds.isEmpty()) {
            return boPage;
        }

        Map<String, String> lectureClassMap = lectureClassTableService.lambdaQuery()
            .in(LectureClassTable::getLectureId, lectureIds)
            .list()
            .stream()
            .collect(Collectors.toMap(LectureClassTable::getLectureId, LectureClassTable::getClassId, (a, b) -> a));

        Set<String> classIds = lectureClassMap.values().stream().filter(StrUtil::isNotBlank).collect(Collectors.toSet());
        Map<String, ClassTable> classMap = classIds.isEmpty()
            ? Map.of()
            : classTableService.listByIds(classIds).stream()
                .collect(Collectors.toMap(ClassTable::getId, Function.identity(), (a, b) -> a));

        for (LectureBO lectureBO : boPage.getRecords()) {
            String classId = lectureClassMap.get(lectureBO.getId());
            ClassTable classTable = StrUtil.isBlank(classId) ? null : classMap.get(classId);
            lectureBO.setClassId(classTable == null ? null : classTable.getId());
            lectureBO.setLocation(classTable == null ? null : classTable.getLocation());
        }

        return boPage;
    }

    private void saveLectureClassRelation(String lectureId, String classId) {
        LectureClassTable relation = new LectureClassTable();
        relation.setId(CommonUtil.generateUuidV7());
        relation.setLectureId(lectureId);
        relation.setClassId(classId);

        boolean saved = lectureClassTableService.save(relation);
        if (!saved) {
            throw new BusinessException(500, "保存讲座教室关联失败");
        }
    }

    private String resolveClassId(LectureDTO lectureDTO) {
        if (StrUtil.isNotBlank(lectureDTO.getClassId())) {
            return lectureDTO.getClassId();
        }

        List<String> classIds = lectureDTO.getClassIds();
        if (classIds == null || classIds.isEmpty()) {
            throw new BusinessException(400, "请至少选择一个教室");
        }

        String resolved = null;
        for (String classId : classIds) {
            if (StrUtil.isBlank(classId)) {
                continue;
            }

            if (resolved != null && !StrUtil.equals(resolved, classId)) {
                throw new BusinessException(400, "一个讲座只能选择一个教室");
            }
            resolved = classId;
        }

        if (StrUtil.isBlank(resolved)) {
            throw new BusinessException(400, "请至少选择一个教室");
        }
        return resolved;
    }

    private ClassTable validateAndGetClass(String classId) {
        ClassTable classTable = classTableService.getById(classId);
        if (classTable == null) {
            throw new BusinessException(404, "教室不存在");
        }

        if (!"active".equals(classTable.getStatus())) {
            throw new BusinessException(400, "仅可选择启用状态教室");
        }

        return classTable;
    }

    private String resolveTeacherName(String teacherId) {
        if (StrUtil.isBlank(teacherId)) {
            return "";
        }

        UserTable teacher = userTableService.getById(teacherId);
        if (teacher == null) {
            throw new BusinessException(404, "讲师不存在");
        }
        return StrUtil.blankToDefault(teacher.getNickname(), "");
    }

    private void validateClassAvailability(String classId, String excludeLectureId) {
        List<String> occupiedLectureIds = lectureTableService.lambdaQuery()
                .in(LectureTable::getStatus, LectureStatusEnum.occupiedStatuses())
                .list()
                .stream()
                .filter(lecture -> !StrUtil.equals(lecture.getId(), excludeLectureId))
                .map(LectureTable::getId)
                .toList();

        if (occupiedLectureIds.isEmpty()) {
            return;
        }

    boolean occupied = lectureClassTableService.lambdaQuery()
                .in(LectureClassTable::getLectureId, occupiedLectureIds)
        .eq(LectureClassTable::getClassId, classId)
        .exists();

    if (occupied) {
            throw new BusinessException(400, "所选教室已被其他讲座占用");
        }
    }

    private void validateLectureTimes(Timestamp registrationStartsTime, Timestamp registrationEndsTime,
            Timestamp lectureStartTime, Timestamp lectureEndTime) {
        if (registrationStartsTime.after(registrationEndsTime) || registrationStartsTime.equals(registrationEndsTime)) {
            throw new BusinessException(400, "报名结束时间必须晚于报名开始时间");
        }

        if (lectureStartTime.after(lectureEndTime) || lectureStartTime.equals(lectureEndTime)) {
            throw new BusinessException(400, "讲座结束时间必须晚于讲座开始时间");
        }

        if (registrationEndsTime.after(lectureStartTime)) {
            throw new BusinessException(400, "报名结束时间必须早于或等于讲座开始时间");
        }
    }
}
