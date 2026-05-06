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
import com.clms.entity.bo.UserInfoBO;
import com.clms.entity.dto.UserAdminDTO;
import com.clms.entity.po.RoleTable;
import com.clms.entity.po.UserTable;
import com.clms.exception.BusinessException;
import com.clms.service.IUserAdminService;
import com.clms.service.data.IRoleTableService;
import com.clms.service.data.IUserTableService;
import com.clms.utils.CommonUtil;

import jakarta.annotation.Resource;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("管理员服务测试")
class UserAdminServiceImplTest {

    @Resource
    private IUserAdminService userAdminService;

    @Resource
    private IUserTableService userTableService;

    @Resource
    private IRoleTableService roleTableService;

    private String testUserId;
    private String testAdminRoleId;

    @BeforeEach
    void setUp() {
        testUserId = null;

        // 预置 "admin" 角色，addAdmin 方法依赖该角色存在
        testAdminRoleId = "test-ua-" + CommonUtil.generateUuidV7().substring(0, 24);
        RoleTable adminRole = new RoleTable();
        adminRole.setId(testAdminRoleId);
        adminRole.setRoleName("admin");
        adminRole.setRoleDescription("管理员");
        adminRole.setRoleStatus("enabled");
        adminRole.setDefaultRole(false);
        roleTableService.save(adminRole);
    }

    @AfterEach
    void tearDown() {
        if (testUserId != null) {
            userTableService.removeById(testUserId);
        }
        if (testAdminRoleId != null) {
            roleTableService.removeById(testAdminRoleId);
        }
    }

    @Test
    @DisplayName("addAdmin: 创建新管理员")
    void addAdmin_shouldCreateNewAdmin() {
        UserAdminDTO dto = new UserAdminDTO();
        dto.setPhone("151" + System.nanoTime() % 100000000L);
        dto.setEmail("new-admin-" + System.nanoTime() + "@clms.local");
        dto.setNickname("test-admin-" + System.nanoTime());
        dto.setPassword("Admin@123");
        dto.setGender("1");
        dto.setAvatarUrl("https://example.com/admin.png");

        userAdminService.addAdmin(dto);

        UserTable created = userTableService.lambdaQuery()
                .eq(UserTable::getPhone, dto.getPhone())
                .one();
        assertNotNull(created);
        testUserId = created.getId();
        assertEquals(dto.getNickname(), created.getNickname());
    }

    @Test
    @DisplayName("getAdminList: 分页查询管理员列表")
    void getAdminList_shouldReturnPagedAdmins() {
        Page<UserInfoBO> result = userAdminService.getAdminList(1L, 10L);
        assertNotNull(result);
        assertTrue(result.getTotal() >= 0);
    }

    @Test
    @DisplayName("disableAdminPermission: 空用户ID应抛400")
    void disableAdminPermission_shouldThrowWhenIdBlank() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> userAdminService.disableAdminPermission(""));
        assertEquals(400, ex.getCode());
    }

    @Test
    @DisplayName("disableAdminPermission: 不存在用户应抛404")
    void disableAdminPermission_shouldThrowWhenUserNotExists() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> userAdminService.disableAdminPermission("nonexistent-user-999"));
        assertEquals(404, ex.getCode());
    }

    @Test
    @DisplayName("enableAdminPermission: 空用户ID应抛400")
    void enableAdminPermission_shouldThrowWhenIdBlank() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> userAdminService.enableAdminPermission(""));
        assertEquals(400, ex.getCode());
    }

    @Test
    @DisplayName("enableAdminPermission: 不存在用户应抛404")
    void enableAdminPermission_shouldThrowWhenUserNotExists() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> userAdminService.enableAdminPermission("nonexistent-user-999"));
        assertEquals(404, ex.getCode());
    }
}
