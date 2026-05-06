package com.clms.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Timestamp;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clms.entity.bo.LectureBO;
import com.clms.entity.po.ClassTable;
import com.clms.entity.po.LectureTable;
import com.clms.entity.po.TagTable;
import com.clms.exception.BusinessException;
import com.clms.service.IAiChatService;
import com.clms.service.ILectureService;
import com.clms.service.data.IClassTableService;
import com.clms.service.data.ILectureTableService;
import com.clms.service.data.ITagTableService;
import com.clms.utils.CommonUtil;
import com.clms.enums.LectureStatusEnum;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import jakarta.annotation.Resource;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("讲座服务集成测试")
class LectureServiceImplTest {

    @Resource
    private ILectureService lectureService;

    @Resource
    private ILectureTableService lectureTableService;

    @Resource
    private IClassTableService classTableService;

    @Resource
    private ITagTableService tagTableService;

    @MockitoBean
    private IAiChatService aiChatService;

    private String testLectureId;
    private String testClassId;
    private String testTagId;
    private final String uniqueSuffix = String.valueOf(System.nanoTime());

    @BeforeEach
    void setUp() {
        testLectureId = null;
        testClassId = "test-class-" + CommonUtil.generateUuidV7().substring(0, 12);
        testTagId = "test-tag-" + CommonUtil.generateUuidV7().substring(0, 12);

        ClassTable cls = new ClassTable();
        cls.setId(testClassId);
        cls.setLocation("Lecture-Test-Room-" + uniqueSuffix);
        cls.setCapacity(100);
        cls.setStatus("active");
        classTableService.save(cls);

        TagTable tag = new TagTable();
        tag.setId(testTagId);
        tag.setTagName("test-" + uniqueSuffix);
        tag.setTagType("lecture");
        JSONObject meta = new JSONObject();
        meta.set("icon", "book");
        tag.setMetaData(meta.toString());
        tag.setCreatorId("lecture-test-admin");
        tag.setTagStatus("active");
        tagTableService.save(tag);
    }

    @AfterEach
    void tearDown() {
        if (testLectureId != null) {
            lectureTableService.removeById(testLectureId);
        }
        classTableService.removeById(testClassId);
        tagTableService.removeById(testTagId);
    }

    @Test
    @DisplayName("getLectureInfo: 存在讲座应返回BO")
    void getLectureInfo_shouldReturnBOWhenExists() {
        createPublishedTestLecture();

        LectureBO result = lectureService.getLectureInfo(testLectureId);
        assertNotNull(result);
        assertEquals("GetInfo-" + uniqueSuffix, result.getTitle());
    }

    @Test
    @DisplayName("getLectureInfo: 不存在应抛404")
    void getLectureInfo_shouldThrowWhenNotExists() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> lectureService.getLectureInfo("nonexistent-lecture-999"));
        assertEquals(404, ex.getCode());
    }

    @Test
    @DisplayName("getLectureList: 分页查询已发布讲座")
    void getLectureList_shouldReturnPagedPublishedLectures() {
        createPublishedTestLecture();

        Page<LectureBO> page = lectureService.getLectureList(
                null, null, null, null, 1, 10, null, null);
        assertNotNull(page);
        assertTrue(page.getTotal() >= 1);
    }

    @Test
    @DisplayName("getLectureList: 按标题过滤")
    void getLectureList_shouldFilterByTitle() {
        createPublishedTestLecture();

        Page<LectureBO> page = lectureService.getLectureList(
                "GetInfo-" + uniqueSuffix, null, null, null, 1, 10, null, null);
        assertTrue(page.getRecords().stream().allMatch(
                l -> l.getTitle().contains("GetInfo-" + uniqueSuffix)));
    }

    @Test
    @DisplayName("getLectureList: 按状态过滤")
    void getLectureList_shouldFilterByStatus() {
        createPublishedTestLecture();

        Page<LectureBO> page = lectureService.getLectureList(
                null, "published", null, null, 1, 10, null, null);
        assertTrue(page.getRecords().stream().allMatch(
                l -> "published".equals(l.getStatus())));
    }

    @Test
    @DisplayName("getHotLectureList: 应返回热门讲座列表")
    void getHotLectureList_shouldReturnList() {
        java.util.List<com.clms.entity.bo.HotLectureBO> result = lectureService.getHotLectureList(null, 5);
        assertNotNull(result);
    }

    private void createPublishedTestLecture() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Timestamp later = new Timestamp(System.currentTimeMillis() + 3600000);
        Timestamp muchLater = new Timestamp(System.currentTimeMillis() + 7200000);

        LectureTable lecture = new LectureTable();
        lecture.setId("test-lecture-" + CommonUtil.generateUuidV7().substring(0, 12));
        lecture.setTitle("GetInfo-" + uniqueSuffix);
        lecture.setDescription("Test lecture description");
        lecture.setStatus(LectureStatusEnum.PUBLISHED.getStatus());
        lecture.setTeacherId("teacher-ui-" + uniqueSuffix);
        lecture.setTeacherName("测试教师");
        lecture.setRegistrationStartsTime(now);
        lecture.setRegistrationEndsTime(later);
        lecture.setLectureStartTime(later);
        lecture.setLectureEndTime(muchLater);
        lecture.setRemaining(100);
        lectureTableService.save(lecture);
        testLectureId = lecture.getId();
    }
}
