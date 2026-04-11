package com.clms.service.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clms.entity.bo.HotLectureBO;
import com.clms.entity.bo.LectureAuditBO;
import com.clms.entity.bo.LectureBO;
import com.clms.entity.bo.LectureTagBO;
import com.clms.entity.dto.LectureDTO;
import com.clms.entity.po.LectureAuditTable;
import com.clms.entity.po.ClassTable;
import com.clms.entity.po.LectureClassTable;
import com.clms.entity.po.LectureTagTable;
import com.clms.entity.po.LectureTable;
import com.clms.entity.po.TagTable;
import com.clms.entity.po.UserTable;
import com.clms.enums.LectureStatusEnum;
import com.clms.exception.BusinessException;
import com.clms.mapper.LectureTableMapper;
import com.clms.service.IAiChatService;
import com.clms.service.ILectureService;
import com.clms.service.data.IClassTableService;
import com.clms.service.data.ILectureAuditTableService;
import com.clms.service.data.ILectureClassTableService;
import com.clms.service.data.ILectureTagTableService;
import com.clms.service.data.ILectureTableService;
import com.clms.service.data.ITagTableService;
import com.clms.service.data.IUserTableService;
import com.clms.utils.CommonUtil;
import com.clms.utils.DataContainerConvertor;

import cn.hutool.core.util.StrUtil;
import dev.langchain4j.data.message.UserMessage;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LectureServiceImpl implements ILectureService {

    @Resource
    private ILectureTableService lectureTableService;

    @Resource
    private ILectureClassTableService lectureClassTableService;

    @Resource
    private ILectureAuditTableService lectureAuditTableService;

    @Resource
    private IClassTableService classTableService;

    @Resource
    private IUserTableService userTableService;

    @Resource
    private ITagTableService tagTableService;

    @Resource
    private ILectureTagTableService lectureTagTableService;

    @Resource
    private IAiChatService aiChatService;

    @Resource
    private LectureTableMapper lectureTableMapper;

    @Resource(name = "asyncPoolTaskExecutor")
    private ThreadPoolTaskExecutor asyncPoolTaskExecutor;

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
        lectureTable.setReason(StrUtil.blankToDefault(lectureDTO.getReason(), ""));

        boolean saved = lectureTableService.save(lectureTable);
        if (!saved) {
            throw new BusinessException(500, "创建讲座失败");
        }

        saveLectureClassRelation(lectureTable.getId(), classId);

        // 提交成功后再异步打标签，避免事务未提交时关联写入失败。
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                CompletableFuture.runAsync(() -> bindLectureTagByAi(lectureTable), asyncPoolTaskExecutor)
                        .exceptionally(ex -> {
                            log.error("异步绑定讲座标签失败, lectureId={}, error={}", lectureTable.getId(), ex.getMessage(), ex);
                            return null;
                        });
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLecture(String lectureId, LectureDTO lectureDTO) {
        LectureTable existed = lectureTableService.getById(lectureId);
        if (existed == null) {
            throw new BusinessException(404, "讲座不存在");
        }

        boolean wasPublished = LectureStatusEnum.PUBLISHED.getStatus().equals(existed.getStatus());
        if (wasPublished && new Timestamp(System.currentTimeMillis()).compareTo(existed.getRegistrationStartsTime()) >= 0) {
            throw new BusinessException(400, "已通过讲座仅可在报名开始前重新编辑");
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
        if (LectureStatusEnum.REJECT.getStatus().equals(existed.getStatus()) || wasPublished) {
            existed.setStatus(LectureStatusEnum.PENDING.getStatus());
            existed.setReason("");
        } else if (StrUtil.isNotBlank(lectureDTO.getStatus())) {
            existed.setStatus(lectureDTO.getStatus());
        }
        existed.setReason(StrUtil.blankToDefault(lectureDTO.getReason(), ""));

        boolean updated = lectureTableService.updateById(existed);
        if (!updated) {
            throw new BusinessException(500, "修改讲座失败");
        }

        LectureClassTable relation = lectureClassTableService.lambdaQuery()
                .eq(LectureClassTable::getLectureId, lectureId)
                .one();

        if (relation == null) {
            saveLectureClassRelation(lectureId, classId);
            return;
        }

        if (StrUtil.equals(relation.getClassId(), classId)) {
            return;
        }

        relation.setClassId(classId);
        boolean relationUpdated = lectureClassTableService.updateById(relation);
        if (!relationUpdated) {
            throw new BusinessException(500, "修改讲座教室关联失败");
        }
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
    @Transactional(rollbackFor = Exception.class)
    public void approveLecture(String lectureId) {
        LectureTable existed = lectureTableService.getById(lectureId);
        if (existed == null) {
            throw new BusinessException(404, "讲座不存在");
        }

        String beforeStatus = existed.getStatus();

        existed.setStatus(LectureStatusEnum.PUBLISHED.getStatus());
        existed.setReason("");

        boolean updated = lectureTableService.updateById(existed);
        if (!updated) {
            throw new BusinessException(500, "讲座通过失败");
        }

        saveLectureAuditRecord(existed, "approve", beforeStatus, existed.getStatus(), "");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectLecture(String lectureId, String reason) {
        LectureTable existed = lectureTableService.getById(lectureId);
        if (existed == null) {
            throw new BusinessException(404, "讲座不存在");
        }
        if (StrUtil.isBlank(reason)) {
            throw new BusinessException(400, "驳回原因不能为空");
        }

        String beforeStatus = existed.getStatus();

        existed.setStatus(LectureStatusEnum.REJECT.getStatus());
        existed.setReason(StrUtil.trim(reason));

        boolean updated = lectureTableService.updateById(existed);
        if (!updated) {
            throw new BusinessException(500, "讲座驳回失败");
        }

        saveLectureAuditRecord(existed, "reject", beforeStatus, existed.getStatus(), existed.getReason());
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
        lectureBO.setTags(getLectureTagBOMap(List.of(lectureId)).getOrDefault(lectureId, List.of()));
        return lectureBO;
    }

    @Override
    public Page<LectureAuditBO> getLectureAuditList(String lectureId, Integer page, Integer size) {
        LectureTable lectureTable = lectureTableService.getById(lectureId);
        if (lectureTable == null) {
            throw new BusinessException(404, "讲座不存在");
        }

        Page<LectureAuditTable> tablePage = lectureAuditTableService.lambdaQuery()
                .eq(LectureAuditTable::getLectureId, lectureId)
                .orderByDesc(LectureAuditTable::getCreateTime)
                .page(new Page<>(page, size));

        return DataContainerConvertor.convertPage(tablePage, LectureAuditTable.class, LectureAuditBO.class);
    }

    @Override
    public Page<LectureBO> getLectureList(String title, String status, String teacherId, String tagId, Integer page, Integer size,
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

        if (StrUtil.isNotBlank(tagId)) {
            // 按标签过滤讲座列表：先通过关联表取讲座ID，再回填到主查询。
            List<String> lectureIds = lectureTagTableService.lambdaQuery()
                    .eq(LectureTagTable::getTagId, tagId)
                    .list()
                    .stream()
                    .map(LectureTagTable::getLectureId)
                    .filter(StrUtil::isNotBlank)
                    .distinct()
                    .toList();

            if (lectureIds.isEmpty()) {
                // 该标签下无讲座时，直接返回空分页，避免无意义主表扫描。
                Page<LectureBO> empty = new Page<>(page, size);
                empty.setTotal(0);
                empty.setRecords(List.of());
                return empty;
            }

            query.in(LectureTable::getId, lectureIds);
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

        Map<String, List<LectureTagBO>> lectureTagBOMap = getLectureTagBOMap(lectureIds);

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
            lectureBO.setTags(lectureTagBOMap.getOrDefault(lectureBO.getId(), List.of()));
        }

        return boPage;
    }

    @Override
    public List<LectureTagBO> getLectureTagList() {
        // 首页分类仅展示“讲座类型 + 启用状态”标签，避免混入用户标签等其他业务标签。
        List<TagTable> tables = tagTableService.lambdaQuery()
            .eq(TagTable::getTagType, "lecture")
            .eq(TagTable::getTagStatus, "active")
                .orderByAsc(TagTable::getCreateTime)
                .list();

        return DataContainerConvertor.convertList(tables, TagTable.class, LectureTagBO.class);
    }

    @Override
    public List<HotLectureBO> getHotLectureList(String tagId, Integer limit) {
        // 对limit做统一兜底，避免过大查询影响接口稳定性。
        int finalLimit = normalizeLimit(limit);
        String normalizedTagId = StrUtil.isBlank(tagId) ? null : tagId;

        // V1热门主查询：按报名数（排除取消）统计并排序。
        List<HotLectureBO> hotList = lectureTableMapper.selectHotLectureList(normalizedTagId, finalLimit);
        if (hotList.isEmpty()) {
            return hotList;
        }

        List<String> lectureIds = hotList.stream().map(HotLectureBO::getId).toList();
        // 批量查讲座标签关联，后续将标签名称回填到hot列表。
        List<LectureTagTable> relations = lectureTagTableService.lambdaQuery()
                .in(LectureTagTable::getLectureId, lectureIds)
                .list();

        if (relations.isEmpty()) {
            return hotList;
        }

        Map<String, List<LectureTagTable>> lectureTagMap = relations.stream()
                .collect(Collectors.groupingBy(LectureTagTable::getLectureId));

        Set<String> tagIds = relations.stream()
                .map(LectureTagTable::getTagId)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());

        Map<String, TagTable> tagMap = tagIds.isEmpty()
                ? Map.of()
                : tagTableService.listByIds(tagIds).stream()
                        .collect(Collectors.toMap(TagTable::getId, Function.identity(), (a, b) -> a));

        for (HotLectureBO hotLecture : hotList) {
            List<LectureTagTable> lectureTags = lectureTagMap.get(hotLecture.getId());
            if (lectureTags == null || lectureTags.isEmpty()) {
                continue;
            }

            // 一个讲座可能多个标签：优先选择sort最小的启用标签作为展示标签。
            TagTable selectedTag = lectureTags.stream()
                    .map(relation -> tagMap.get(relation.getTagId()))
                    .filter(tag -> tag != null
                        && "lecture".equals(tag.getTagType())
                        && "active".equals(tag.getTagStatus()))
                    .min(Comparator.comparing(TagTable::getCreateTime, Comparator.nullsLast(LocalDateTime::compareTo)))
                    .orElse(null);

            if (selectedTag != null) {
                hotLecture.setTag(StrUtil.blankToDefault(selectedTag.getTagName(), ""));
            }
        }

        return hotList;
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

    private void bindLectureTagByAi(LectureTable lectureTable) {
        List<TagTable> lectureTags = tagTableService.lambdaQuery()
                .eq(TagTable::getTagType, "lecture")
                .eq(TagTable::getTagStatus, "active")
                .list();

        if (lectureTags.isEmpty()) {
            throw new BusinessException(400, "当前未配置可用讲座标签，无法完成AI自动分类");
        }

        String candidateTagNames = lectureTags.stream()
                .map(TagTable::getTagName)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.joining("、"));

        String userPrompt = "讲座标题：" + StrUtil.blankToDefault(lectureTable.getTitle(), "") + "\n"
                + "讲座内容：" + StrUtil.blankToDefault(lectureTable.getDescription(), "") + "\n"
                + "可选标签：" + candidateTagNames + "\n"
                + "请严格从可选标签中选择最匹配的一项，并仅返回标签名称。";

        String aiResult;
        try {
            aiResult = aiChatService.classifyLectureTag(userPrompt);
        } catch (Exception e) {
            log.error("AI自动识别讲座标签失败, lectureId={}, error={}", lectureTable.getId(), e.getMessage(), e);
            throw new BusinessException(500, "AI自动识别讲座标签失败");
        }

        TagTable matchedTag = matchTagFromAiResult(aiResult, lectureTags);
        if (matchedTag == null) {
            throw new BusinessException(400, "AI返回的讲座标签无效: " + StrUtil.blankToDefault(aiResult, ""));
        }

        boolean exists = lectureTagTableService.lambdaQuery()
                .eq(LectureTagTable::getLectureId, lectureTable.getId())
                .eq(LectureTagTable::getTagId, matchedTag.getId())
                .exists();

        if (exists) {
            return;
        }

        LectureTagTable lectureTagTable = new LectureTagTable();
        lectureTagTable.setId(CommonUtil.generateUuidV7());
        lectureTagTable.setLectureId(lectureTable.getId());
        lectureTagTable.setTagId(matchedTag.getId());

        boolean saved = lectureTagTableService.save(lectureTagTable);
        if (!saved) {
            throw new BusinessException(500, "保存讲座标签关联失败");
        }
    }

    private TagTable matchTagFromAiResult(String aiResult, List<TagTable> lectureTags) {
        if (StrUtil.isBlank(aiResult) || lectureTags == null || lectureTags.isEmpty()) {
            return null;
        }

        String normalized = StrUtil.trim(aiResult)
                .replace("\r", "")
                .replace("\n", "")
                .replace("`", "")
                .replace("\"", "")
                .replace("'", "")
                .replace("。", "")
                .replace("，", "")
                .replace(",", "")
                .replace("：", "")
                .replace(":", "");

        TagTable exact = lectureTags.stream()
                .filter(tag -> StrUtil.equals(StrUtil.trim(tag.getTagName()), normalized))
                .findFirst()
                .orElse(null);
        if (exact != null) {
            return exact;
        }

        return lectureTags.stream()
                .filter(tag -> StrUtil.isNotBlank(tag.getTagName()) && StrUtil.contains(aiResult, tag.getTagName()))
                .findFirst()
                .orElse(null);
    }

     /**
     * 批量查询讲座标签并转换为返回对象，避免详情/列表接口出现N+1查询。
     */
    private Map<String, List<LectureTagBO>> getLectureTagBOMap(List<String> lectureIds) {
        if (lectureIds == null || lectureIds.isEmpty()) {
            return Map.of();
        }

        // 先查询讲座标签关联表，获取讲座-标签ID映射关系。
        List<LectureTagTable> relations = lectureTagTableService.lambdaQuery()
            .in(LectureTagTable::getLectureId, lectureIds)
            .list();

        if (relations.isEmpty()) {
            return Map.of();
        }

        // 预先批量查询标签信息，避免循环内查询。
        Set<String> tagIds = relations.stream()
            .map(LectureTagTable::getTagId)
            .filter(StrUtil::isNotBlank)
            .collect(Collectors.toSet());

        if (tagIds.isEmpty()) {
            return Map.of();
        }

        // 将标签ID映射到标签对象，方便后续回填标签信息。
        Map<String, TagTable> tagMap = tagTableService.listByIds(tagIds).stream()
            .collect(Collectors.toMap(TagTable::getId, Function.identity(), (a, b) -> a));

        // 构建讲座ID到标签列表的映射关系，并转换为返回对象；同时过滤掉非讲座标签和非启用标签。
        Map<String, List<LectureTagBO>> lectureTagBOMap = relations.stream()
            .collect(Collectors.groupingBy(LectureTagTable::getLectureId,
                Collectors.mapping(relation -> tagMap.get(relation.getTagId()), Collectors.toList())))
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey,
                entry -> entry.getValue().stream()
                    .filter(tag -> tag != null
                        && "lecture".equals(tag.getTagType())
                        && "active".equals(tag.getTagStatus()))
                    .sorted(Comparator.comparing(TagTable::getCreateTime,
                        Comparator.nullsLast(LocalDateTime::compareTo)))
                    .map(LectureTagBO::new)
                    .collect(Collectors.toList()),
                (a, b) -> a));

        return lectureTagBOMap;
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

    private void saveLectureAuditRecord(LectureTable lectureTable, String action, String beforeStatus,
            String afterStatus, String reason) {
        String operatorId = String.valueOf(StpUtil.getLoginId());
        UserTable operator = userTableService.getById(operatorId);

        LectureAuditTable auditTable = new LectureAuditTable();
        auditTable.setId(CommonUtil.generateUuidV7());
        auditTable.setLectureId(lectureTable.getId());
        auditTable.setLectureTitle(lectureTable.getTitle());
        auditTable.setAuditAction(action);
        auditTable.setBeforeStatus(beforeStatus);
        auditTable.setAfterStatus(afterStatus);
        auditTable.setReason(StrUtil.blankToDefault(reason, ""));
        auditTable.setAuditorId(operatorId);
        auditTable.setAuditorName(operator == null ? operatorId : StrUtil.blankToDefault(operator.getNickname(), operatorId));

        boolean saved = lectureAuditTableService.save(auditTable);
        if (!saved) {
            throw new BusinessException(500, "保存讲座审核记录失败");
        }
    }

    private int normalizeLimit(Integer limit) {
        // 默认返回6条，最多50条，防止滥用大分页参数。
        if (limit == null || limit <= 0) {
            return 6;
        }
        return Math.min(limit, 50);
    }
}
