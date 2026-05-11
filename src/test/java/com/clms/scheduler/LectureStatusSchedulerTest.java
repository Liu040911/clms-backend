package com.clms.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Timestamp;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.clms.entity.po.LectureTable;
import com.clms.entity.po.RegistrationTable;
import com.clms.entity.po.UserTable;
import com.clms.enums.LectureStatusEnum;
import com.clms.enums.RegistrationStatusEnum;
import com.clms.service.IUserLectureRegistrationService;
import com.clms.service.data.ILectureTableService;
import com.clms.service.data.IRegistrationTableService;
import com.clms.service.data.IUserTableService;
import com.clms.utils.CommonUtil;

import cn.hutool.crypto.digest.DigestUtil;
import jakarta.annotation.Resource;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("讲座状态定时任务集成测试")
class LectureStatusSchedulerTest {

    @Resource
    private LectureStatusScheduler scheduler;

    @Resource
    private ILectureTableService lectureTableService;

    @Resource
    private IRegistrationTableService registrationTableService;

    @Resource
    private IUserTableService userTableService;

    @Resource
    private IUserLectureRegistrationService userLectureRegistrationService;

    private String testLectureId;
    private String testUserAPrefix;
    private String testUserBPrefix;

    @BeforeEach
    void setUp() {
        testLectureId = null;
        testUserAPrefix = "sched-abs-a-" + CommonUtil.generateUuidV7().substring(0, 12);
        testUserBPrefix = "sched-abs-b-" + CommonUtil.generateUuidV7().substring(0, 12);
    }

    @AfterEach
    void tearDown() {
        if (testLectureId != null) {
            registrationTableService.lambdaUpdate()
                    .likeRight(RegistrationTable::getUserId, "sched-abs-")
                    .remove();
            lectureTableService.removeById(testLectureId);
        }
        userTableService.lambdaUpdate()
                .likeRight(UserTable::getId, "sched-abs-")
                .remove();
    }

    private LectureTable createLecture(String status, Timestamp registrationStartsTime,
            Timestamp registrationEndsTime, Timestamp lectureStartTime, Timestamp lectureEndTime) {
        LectureTable lecture = new LectureTable();
        lecture.setId(CommonUtil.generateUuidV7());
        lecture.setTitle("scheduler-test-" + System.nanoTime());
        lecture.setDescription("test");
        lecture.setCoverImageUrl("");
        lecture.setTeacherId("tchr-" + CommonUtil.generateUuidV7().substring(0, 27));
        lecture.setTeacherName("TestTeacher");
        lecture.setStatus(status);
        lecture.setRemaining(50);

        Timestamp now = new Timestamp(System.currentTimeMillis());
        lecture.setRegistrationStartsTime(registrationStartsTime != null ? registrationStartsTime : now);
        lecture.setRegistrationEndsTime(registrationEndsTime != null ? registrationEndsTime
                : new Timestamp(System.currentTimeMillis() + 7200000));
        lecture.setLectureStartTime(lectureStartTime != null ? lectureStartTime
                : new Timestamp(System.currentTimeMillis() + 3600000));
        lecture.setLectureEndTime(
                lectureEndTime != null ? lectureEndTime : new Timestamp(System.currentTimeMillis() + 7200000));

        lectureTableService.save(lecture);
        testLectureId = lecture.getId();
        return lecture;
    }

    @Test
    @DisplayName("PUBLISHED 且 registrationStartsTime 已到 → REGISTERING")
    void shouldTransitionPublishedToRegisteringWhenTimeReached() {
        Timestamp past = new Timestamp(System.currentTimeMillis() - 60000);
        createLecture(LectureStatusEnum.PUBLISHED.getStatus(), past, null, null, null);

        scheduler.checkAndUpdateLectureStatuses();

        LectureTable updated = lectureTableService.getById(testLectureId);
        assertEquals(LectureStatusEnum.REGISTERING.getStatus(), updated.getStatus());
    }

    @Test
    @DisplayName("PUBLISHED 且 registrationStartsTime 未到 → 保持 PUBLISHED")
    void shouldNotTransitionPublishedWhenTimeNotReached() {
        Timestamp future = new Timestamp(System.currentTimeMillis() + 3600000);
        createLecture(LectureStatusEnum.PUBLISHED.getStatus(), future, null, null, null);

        scheduler.checkAndUpdateLectureStatuses();

        LectureTable updated = lectureTableService.getById(testLectureId);
        assertEquals(LectureStatusEnum.PUBLISHED.getStatus(), updated.getStatus());
    }

    @Test
    @DisplayName("REGISTERING 且 registrationEndsTime 已到 → READY")
    void shouldTransitionRegisteringToReadyWhenRegistrationEnds() {
        Timestamp pastRegStart = new Timestamp(System.currentTimeMillis() - 120000);
        Timestamp pastRegEnd = new Timestamp(System.currentTimeMillis() - 60000);
        createLecture(LectureStatusEnum.REGISTERING.getStatus(),
                pastRegStart, pastRegEnd, null, null);

        scheduler.checkAndUpdateLectureStatuses();

        LectureTable updated = lectureTableService.getById(testLectureId);
        assertEquals(LectureStatusEnum.READY.getStatus(), updated.getStatus());
    }

    @Test
    @DisplayName("REGISTERING 且 registrationEndsTime 未到 → 保持 REGISTERING")
    void shouldNotTransitionRegisteringWhenRegistrationEndsNotReached() {
        Timestamp futureRegEnd = new Timestamp(System.currentTimeMillis() + 3600000);
        createLecture(LectureStatusEnum.REGISTERING.getStatus(),
                new Timestamp(System.currentTimeMillis() - 120000), futureRegEnd, null, null);

        scheduler.checkAndUpdateLectureStatuses();

        LectureTable updated = lectureTableService.getById(testLectureId);
        assertEquals(LectureStatusEnum.REGISTERING.getStatus(), updated.getStatus());
    }

    @Test
    @DisplayName("READY 且 lectureStartTime 已到 → ONGOING")
    void shouldTransitionReadyToOngoingWhenTimeReached() {
        Timestamp pastStart = new Timestamp(System.currentTimeMillis() - 60000);
        createLecture(LectureStatusEnum.READY.getStatus(),
                new Timestamp(System.currentTimeMillis() - 180000),
                new Timestamp(System.currentTimeMillis() - 120000),
                pastStart, null);

        scheduler.checkAndUpdateLectureStatuses();

        LectureTable updated = lectureTableService.getById(testLectureId);
        assertEquals(LectureStatusEnum.ONGOING.getStatus(), updated.getStatus());
    }

    @Test
    @DisplayName("READY 且 lectureStartTime 未到 → 保持 READY")
    void shouldNotTransitionReadyWhenLectureStartNotReached() {
        Timestamp futureStart = new Timestamp(System.currentTimeMillis() + 3600000);
        createLecture(LectureStatusEnum.READY.getStatus(),
                new Timestamp(System.currentTimeMillis() - 180000),
                new Timestamp(System.currentTimeMillis() - 120000),
                futureStart,
                new Timestamp(System.currentTimeMillis() + 7200000));

        scheduler.checkAndUpdateLectureStatuses();

        LectureTable updated = lectureTableService.getById(testLectureId);
        assertEquals(LectureStatusEnum.READY.getStatus(), updated.getStatus());
    }

    @Test
    @DisplayName("ONGOING 且 lectureEndTime 已到 → FINISHED")
    void shouldTransitionOngoingToFinishedWhenTimeReached() {
        Timestamp pastStart = new Timestamp(System.currentTimeMillis() - 120000);
        Timestamp pastEnd = new Timestamp(System.currentTimeMillis() - 60000);
        createLecture(LectureStatusEnum.ONGOING.getStatus(),
                new Timestamp(System.currentTimeMillis() - 180000), null, pastStart, pastEnd);

        scheduler.checkAndUpdateLectureStatuses();

        LectureTable updated = lectureTableService.getById(testLectureId);
        assertEquals(LectureStatusEnum.FINISHED.getStatus(), updated.getStatus());
    }

    @Test
    @DisplayName("幂等性验证：连续两次调用无重复变更")
    void shouldBeIdempotent() {
        Timestamp past = new Timestamp(System.currentTimeMillis() - 60000);
        createLecture(LectureStatusEnum.PUBLISHED.getStatus(), past, null, null, null);

        scheduler.checkAndUpdateLectureStatuses();
        scheduler.checkAndUpdateLectureStatuses();

        LectureTable updated = lectureTableService.getById(testLectureId);
        assertEquals(LectureStatusEnum.REGISTERING.getStatus(), updated.getStatus());
    }

    @Test
    @DisplayName("多种状态混合：各自按条件正确转换")
    void shouldHandleMixedStatuses() {
        Timestamp past = new Timestamp(System.currentTimeMillis() - 60000);
        Timestamp future = new Timestamp(System.currentTimeMillis() + 3600000);

        createLecture(LectureStatusEnum.PUBLISHED.getStatus(), past, null, null, null);
        String publishedId = testLectureId;

        createLecture(LectureStatusEnum.PUBLISHED.getStatus(), future, null, null, null);
        String futurePublishedId = testLectureId;

        scheduler.checkAndUpdateLectureStatuses();

        assertEquals(LectureStatusEnum.REGISTERING.getStatus(),
                lectureTableService.getById(publishedId).getStatus());
        assertEquals(LectureStatusEnum.PUBLISHED.getStatus(),
                lectureTableService.getById(futurePublishedId).getStatus());
    }

    @Test
    @DisplayName("完整生命周期：REGISTERING → READY → ONGOING")
    void shouldTransitionFullLifecycle() {
        Timestamp pastRegStart = new Timestamp(System.currentTimeMillis() - 300000);
        Timestamp pastRegEnd = new Timestamp(System.currentTimeMillis() - 180000);
        Timestamp pastLectureStart = new Timestamp(System.currentTimeMillis() - 60000);
        Timestamp futureLectureEnd = new Timestamp(System.currentTimeMillis() + 3600000);

        createLecture(LectureStatusEnum.REGISTERING.getStatus(),
                pastRegStart, pastRegEnd, pastLectureStart, futureLectureEnd);

        scheduler.checkAndUpdateLectureStatuses();

        LectureTable updated = lectureTableService.getById(testLectureId);
        assertEquals(LectureStatusEnum.ONGOING.getStatus(), updated.getStatus());
    }

    private UserTable createTestUser(String userId) {
        UserTable user = new UserTable();
        user.setId(userId);
        user.setNickname("sched-" + System.nanoTime());
        user.setPhone("170" + System.nanoTime() % 100000000L);
        user.setEmail("sched-" + System.nanoTime() + "@clms.local");
        user.setPassword(DigestUtil.md5Hex("Pass@123" + userId));
        user.setUserRoles(new cn.hutool.json.JSONArray());
        user.setUserPermissions(new cn.hutool.json.JSONArray());
        userTableService.save(user);
        return user;
    }

    private void createRegistration(String lectureId, String userId, String status) {
        RegistrationTable reg = new RegistrationTable();
        reg.setId(CommonUtil.generateUuidV7());
        reg.setUserId(userId);
        reg.setLectureId(lectureId);
        reg.setStatus(status);
        reg.setRegistrationTime(new Timestamp(System.currentTimeMillis()));
        registrationTableService.save(reg);
    }

    @Test
    @DisplayName("ONGOING→FINISHED：讲座状态正确转换，标记逻辑分离验证")
    void shouldTransitionOngoingToFinishedAndMarkSync() {
        Timestamp pastStart = new Timestamp(System.currentTimeMillis() - 120000);
        Timestamp pastEnd = new Timestamp(System.currentTimeMillis() - 60000);
        LectureTable lecture = createLecture(LectureStatusEnum.ONGOING.getStatus(),
                new Timestamp(System.currentTimeMillis() - 180000), null, pastStart, pastEnd);

        String userA = testUserAPrefix;
        String userB = testUserBPrefix;
        createTestUser(userA);
        createTestUser(userB);
        createRegistration(lecture.getId(), userA, RegistrationStatusEnum.PENDING.getStatus());
        createRegistration(lecture.getId(), userB, RegistrationStatusEnum.CHECKED_IN.getStatus());

        scheduler.checkAndUpdateLectureStatuses();

        LectureTable updated = lectureTableService.getById(lecture.getId());
        assertEquals(LectureStatusEnum.FINISHED.getStatus(), updated.getStatus());

        userLectureRegistrationService.markAbsentRegistrations(lecture.getId());

        RegistrationTable regA = registrationTableService.lambdaQuery()
                .eq(RegistrationTable::getLectureId, lecture.getId())
                .eq(RegistrationTable::getUserId, userA)
                .one();
        RegistrationTable regB = registrationTableService.lambdaQuery()
                .eq(RegistrationTable::getLectureId, lecture.getId())
                .eq(RegistrationTable::getUserId, userB)
                .one();
        assertEquals(RegistrationStatusEnum.NOT_SIGNED_IN.getStatus(), regA.getStatus());
        assertEquals(RegistrationStatusEnum.CHECKED_IN.getStatus(), regB.getStatus());
    }

    @Test
    @DisplayName("ONGOING→FINISHED幂等：二次标记不重复变更")
    void shouldNotReMarkAbsentIfAlreadyMarked() {
        Timestamp pastStart = new Timestamp(System.currentTimeMillis() - 120000);
        Timestamp pastEnd = new Timestamp(System.currentTimeMillis() - 60000);
        LectureTable lecture = createLecture(LectureStatusEnum.ONGOING.getStatus(),
                new Timestamp(System.currentTimeMillis() - 180000), null, pastStart, pastEnd);

        String userId = testUserAPrefix;
        createTestUser(userId);
        createRegistration(lecture.getId(), userId, RegistrationStatusEnum.PENDING.getStatus());

        scheduler.checkAndUpdateLectureStatuses();
        userLectureRegistrationService.markAbsentRegistrations(lecture.getId());
        userLectureRegistrationService.markAbsentRegistrations(lecture.getId());

        RegistrationTable reg = registrationTableService.lambdaQuery()
                .eq(RegistrationTable::getLectureId, lecture.getId())
                .eq(RegistrationTable::getUserId, userId)
                .one();
        assertEquals(RegistrationStatusEnum.NOT_SIGNED_IN.getStatus(), reg.getStatus());
    }
}
