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
import com.clms.entity.po.RegistrationTable;
import com.clms.entity.po.TagTable;
import com.clms.entity.po.UserTable;
import com.clms.enums.RegistrationStatusEnum;
import com.clms.exception.BusinessException;
import com.clms.service.IAiChatService;
import com.clms.service.ILectureService;
import com.clms.service.IUserLectureRegistrationService;
import com.clms.service.data.IClassTableService;
import com.clms.service.data.ILectureTableService;
import com.clms.service.data.IRegistrationTableService;
import com.clms.service.data.ITagTableService;
import com.clms.service.data.IUserTableService;
import com.clms.utils.CommonUtil;
import com.clms.enums.LectureStatusEnum;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.DigestUtil;
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

    @Resource
    private IRegistrationTableService registrationTableService;

    @Resource
    private IUserTableService userTableService;

    @Resource
    private IUserLectureRegistrationService userLectureRegistrationService;

    @MockitoBean
    private IAiChatService aiChatService;

    private String testLectureId;
    private String testClassId;
    private String testTagId;
    private String testTeacherId;
    private String testRegUserId;
    private final String uniqueSuffix = String.valueOf(System.nanoTime());

    @BeforeEach
    void setUp() {
        testTeacherId = "endlec-teacher-" + CommonUtil.generateUuidV7().substring(0, 12);
        testRegUserId = "endlec-user-" + CommonUtil.generateUuidV7().substring(0, 12);
        StpUtil.login(testTeacherId);
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
        StpUtil.logout();
        if (testLectureId != null) {
            registrationTableService.lambdaUpdate()
                    .eq(RegistrationTable::getUserId, testRegUserId)
                    .remove();
            lectureTableService.removeById(testLectureId);
        }
        userTableService.lambdaUpdate()
                .eq(UserTable::getId, testRegUserId)
                .remove();
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

    private LectureTable createOngoingTestLecture() {
        Timestamp past = new Timestamp(System.currentTimeMillis() - 120000);
        Timestamp future = new Timestamp(System.currentTimeMillis() + 3600000);

        LectureTable lecture = new LectureTable();
        lecture.setId("endlec-" + CommonUtil.generateUuidV7().substring(0, 12));
        lecture.setTitle("EndLecture-" + uniqueSuffix);
        lecture.setDescription("End lecture test");
        lecture.setStatus(LectureStatusEnum.ONGOING.getStatus());
        lecture.setTeacherId(testTeacherId);
        lecture.setTeacherName("结束测试教师");
        lecture.setRegistrationStartsTime(new Timestamp(System.currentTimeMillis() - 180000));
        lecture.setRegistrationEndsTime(past);
        lecture.setLectureStartTime(past);
        lecture.setLectureEndTime(future);
        lecture.setRemaining(100);
        lectureTableService.save(lecture);
        testLectureId = lecture.getId();
        return lecture;
    }

    @Test
    @DisplayName("endLecture: 不存在讲座应抛404")
    void endLecture_shouldThrowWhenLectureNotExists() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> lectureService.endLecture(testTeacherId, "nonexistent-lecture-999"));
        assertEquals(404, ex.getCode());
    }

    @Test
    @DisplayName("endLecture: 非ONGOING状态应抛400")
    void endLecture_shouldRejectNonOngoingLecture() {
        createPublishedTestLecture();

        BusinessException ex = assertThrows(BusinessException.class,
                () -> lectureService.endLecture(testTeacherId, testLectureId));
        assertEquals(400, ex.getCode());
    }

    @Test
    @DisplayName("endLecture: 非讲座教师且非管理员应抛403")
    void endLecture_shouldRejectNonTeacherNonAdmin() {
        LectureTable lecture = createOngoingTestLecture();

        BusinessException ex = assertThrows(BusinessException.class,
                () -> lectureService.endLecture("some-other-user-id", lecture.getId()));
        assertEquals(403, ex.getCode());
    }

    @Test
    @DisplayName("endLecture: 讲座所属教师可结束，标记逻辑分离验证")
    void endLecture_shouldAllowTeacherToEnd() {
        LectureTable lecture = createOngoingTestLecture();

        UserTable user = new UserTable();
        user.setId(testRegUserId);
        user.setNickname("endlec-" + System.nanoTime());
        user.setPhone("180" + System.nanoTime() % 100000000L);
        user.setEmail("endlec-" + System.nanoTime() + "@clms.local");
        user.setPassword(DigestUtil.md5Hex("Pass@123" + testRegUserId));
        user.setUserRoles(new JSONArray());
        user.setUserPermissions(new JSONArray());
        userTableService.save(user);

        RegistrationTable reg = new RegistrationTable();
        reg.setId(CommonUtil.generateUuidV7());
        reg.setUserId(testRegUserId);
        reg.setLectureId(lecture.getId());
        reg.setStatus(RegistrationStatusEnum.PENDING.getStatus());
        reg.setRegistrationTime(new Timestamp(System.currentTimeMillis()));
        registrationTableService.save(reg);

        lectureService.endLecture(testTeacherId, lecture.getId());

        LectureTable updated = lectureTableService.getById(lecture.getId());
        assertEquals(LectureStatusEnum.FINISHED.getStatus(), updated.getStatus());

        userLectureRegistrationService.markAbsentRegistrations(lecture.getId());

        RegistrationTable updatedReg = registrationTableService.getById(reg.getId());
        assertEquals(RegistrationStatusEnum.NOT_SIGNED_IN.getStatus(), updatedReg.getStatus());
    }

    @Test
    @DisplayName("endLecture: 已结束讲座拒绝重复操作")
    void endLecture_shouldRejectAlreadyFinished() {
        Timestamp past = new Timestamp(System.currentTimeMillis() - 3600000);

        LectureTable lecture = new LectureTable();
        lecture.setId("endlec-fin-" + CommonUtil.generateUuidV7().substring(0, 12));
        lecture.setTitle("AlreadyFinished-" + uniqueSuffix);
        lecture.setStatus(LectureStatusEnum.FINISHED.getStatus());
        lecture.setTeacherId(testTeacherId);
        lecture.setTeacherName("已结束教师");
        lecture.setRegistrationStartsTime(new Timestamp(past.getTime() - 3600000));
        lecture.setRegistrationEndsTime(new Timestamp(past.getTime() - 1800000));
        lecture.setLectureStartTime(past);
        lecture.setLectureEndTime(new Timestamp(past.getTime() + 1800000));
        lecture.setRemaining(100);
        lectureTableService.save(lecture);
        testLectureId = lecture.getId();

        BusinessException ex = assertThrows(BusinessException.class,
                () -> lectureService.endLecture(testTeacherId, testLectureId));
        assertEquals(400, ex.getCode());
        assertTrue(ex.getMessage().contains("已结束"));
    }
}
