package com.clms.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clms.entity.bo.UserLectureAppointmentBO;
import com.clms.entity.po.RegistrationTable;
import com.clms.entity.po.UserTable;
import com.clms.enums.RegistrationStatusEnum;
import com.clms.exception.BusinessException;
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
@DisplayName("讲座报名服务测试")
class UserLectureRegistrationServiceImplTest {

    @Resource
    private IUserLectureRegistrationService registrationService;

    @Resource
    private IUserTableService userTableService;

    @Resource
    private ILectureTableService lectureTableService;

    @Resource
    private IRegistrationTableService registrationTableService;

    private String testUserId;
    private String testLectureId;

    @BeforeEach
    void setUp() {
        testUserId = "test-reg-user-" + CommonUtil.generateUuidV7().substring(0, 12);
        testLectureId = null;

        UserTable user = new UserTable();
        user.setId(testUserId);
        user.setNickname("reg-test-" + System.nanoTime());
        user.setPhone("160" + System.nanoTime() % 100000000L);
        user.setEmail("reg-" + System.nanoTime() + "@clms.local");
        user.setPassword(DigestUtil.md5Hex("Pass@123" + testUserId));
        user.setUserRoles(new cn.hutool.json.JSONArray());
        user.setUserPermissions(new cn.hutool.json.JSONArray());
        userTableService.save(user);
    }

    @AfterEach
    void tearDown() {
        registrationTableService.lambdaUpdate()
                .eq(RegistrationTable::getUserId, testUserId)
                .remove();
        if (testLectureId != null) {
            lectureTableService.removeById(testLectureId);
        }
        userTableService.removeById(testUserId);
    }

    @Test
    @DisplayName("getUserLectureAppointmentList: 空用户应返回空列表")
    void getUserLectureAppointmentList_shouldReturnEmptyForUntouchedUser() {
        Page<UserLectureAppointmentBO> result = registrationService
                .getUserLectureAppointmentList(testUserId, null, 1, 10);
        assertNotNull(result);
        assertTrue(result.getRecords().isEmpty());
    }

    @Test
    @DisplayName("getUserLectureAppointmentList: 按状态过滤")
    void getUserLectureAppointmentList_shouldFilterByStatus() {
        com.clms.entity.po.LectureTable lecture = new com.clms.entity.po.LectureTable();
        lecture.setId("lec-" + CommonUtil.generateUuidV7().substring(0, 28));
        lecture.setTitle("FK-" + System.nanoTime());
        lecture.setStatus("published");
        lecture.setTeacherId("tchr-" + CommonUtil.generateUuidV7().substring(0, 27));
        lecture.setTeacherName("T");
        java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
        lecture.setRegistrationStartsTime(now);
        lecture.setRegistrationEndsTime(now);
        lecture.setLectureStartTime(now);
        lecture.setLectureEndTime(new java.sql.Timestamp(System.currentTimeMillis() + 3600000));
        lecture.setRemaining(100);
        lectureTableService.save(lecture);
        try {
            RegistrationTable reg = new RegistrationTable();
            reg.setId(CommonUtil.generateUuidV7());
            reg.setUserId(testUserId);
            reg.setLectureId(lecture.getId());
            reg.setStatus(RegistrationStatusEnum.CANCELLED.getStatus());
            reg.setRegistrationTime(new java.sql.Timestamp(System.currentTimeMillis()));
            registrationTableService.save(reg);

            try {
                Page<UserLectureAppointmentBO> result = registrationService
                        .getUserLectureAppointmentList(testUserId, "cancelled", 1, 10);
                assertNotNull(result);
            } finally {
                registrationTableService.removeById(reg.getId());
            }
        } finally {
            lectureTableService.removeById(lecture.getId());
        }
    }

    @Test
    @DisplayName("getUserLectureAppointmentList: 页码小于1时使用默认值")
    void getUserLectureAppointmentList_shouldDefaultPageWhenInvalid() {
        Page<UserLectureAppointmentBO> result = registrationService
                .getUserLectureAppointmentList(testUserId, null, -1, 0);
        assertNotNull(result);
        assertEquals(1, result.getCurrent());
        assertEquals(10, result.getSize());
    }

    @Test
    @DisplayName("getLectureCheckInQrCode: 空讲座ID应抛400")
    void getLectureCheckInQrCode_shouldThrowWhenLectureIdBlank() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> registrationService.getLectureCheckInQrCode(testUserId, ""));
        assertEquals(400, ex.getCode());
    }

    @Test
    @DisplayName("getLectureCheckInQrCode: 不存在讲座应抛404")
    void getLectureCheckInQrCode_shouldThrowWhenLectureNotExists() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> registrationService.getLectureCheckInQrCode(testUserId, "nonexistent-lecture-id"));
        assertEquals(404, ex.getCode());
    }

    @Test
    @DisplayName("checkInByQrCode: 空token应抛400")
    void checkInByQrCode_shouldThrowWhenTokenBlank() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> registrationService.checkInByQrCode(testUserId, ""));
        assertEquals(400, ex.getCode());
    }

    @Test
    @DisplayName("checkInByQrCode: 无效或过期token应抛400")
    void checkInByQrCode_shouldThrowWhenTokenInvalid() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> registrationService.checkInByQrCode(testUserId, "invalid-expired-token-12345"));
        assertEquals(400, ex.getCode());
    }
}
