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

import com.clms.entity.bo.UserInfoBO;
import com.clms.entity.po.UserTable;
import com.clms.exception.BusinessException;
import com.clms.service.IUserAccountService;
import com.clms.service.data.IUserTableService;
import com.clms.utils.CommonUtil;

import cn.hutool.crypto.digest.DigestUtil;
import jakarta.annotation.Resource;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("用户账户服务测试")
class UserAccountServiceImplTest {

    @Resource
    private IUserAccountService userAccountService;

    @Resource
    private IUserTableService userTableService;

    private String testUserId;

    @BeforeEach
    void setUp() {
        testUserId = "test-account-" + CommonUtil.generateUuidV7().substring(0, 12);
        UserTable user = new UserTable();
        user.setId(testUserId);
        user.setNickname("test-nick-" + System.nanoTime());
        user.setPhone("140" + System.nanoTime() % 100000000L);
        user.setEmail("test-" + System.nanoTime() + "@clms.local");
        user.setPassword(DigestUtil.md5Hex("Pass@123" + testUserId));
        user.setAvatarUrl("https://example.com/old-avatar.png");
        user.setUserRoles(new cn.hutool.json.JSONArray());
        user.setUserPermissions(new cn.hutool.json.JSONArray());
        userTableService.save(user);
    }

    @AfterEach
    void tearDown() {
        userTableService.removeById(testUserId);
    }

    @Test
    @DisplayName("getCurrentUserInfo: 存在用户应返回完整信息")
    void getCurrentUserInfo_shouldReturnUserInfoWhenExists() {
        UserInfoBO result = userAccountService.getCurrentUserInfo(testUserId);
        assertNotNull(result);
        assertEquals(testUserId, result.getId());
        assertTrue(result.getNickname().startsWith("test-nick-"));
    }

    @Test
    @DisplayName("getCurrentUserInfo: 不存在用户应抛404")
    void getCurrentUserInfo_shouldThrowWhenUserNotExists() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> userAccountService.getCurrentUserInfo("nonexistent-user-" + System.nanoTime()));
        assertEquals(404, ex.getCode());
    }

    @Test
    @DisplayName("updateUserAvatar: 正常更新头像")
    void updateUserAvatar_shouldUpdateSuccessfully() {
        String newAvatar = "https://example.com/new-avatar.png";
        userAccountService.updateUserAvatar(testUserId, newAvatar);

        UserTable updated = userTableService.getById(testUserId);
        assertEquals(newAvatar, updated.getAvatarUrl());
    }

    @Test
    @DisplayName("updateUserAvatar: 不存在用户应抛404")
    void updateUserAvatar_shouldThrowWhenUserNotExists() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> userAccountService.updateUserAvatar("nonexistent-user-" + System.nanoTime(), "url"));
        assertEquals(404, ex.getCode());
    }
}
