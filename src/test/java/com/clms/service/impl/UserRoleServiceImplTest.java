package com.clms.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clms.entity.bo.RoleBO;
import com.clms.entity.po.RoleTable;
import com.clms.entity.po.UserRoleTable;
import com.clms.entity.po.UserTable;
import com.clms.service.IUserRoleService;
import com.clms.service.data.IRoleTableService;
import com.clms.service.data.IUserRoleTableService;
import com.clms.service.data.IUserTableService;
import com.clms.utils.CommonUtil;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONArray;
import jakarta.annotation.Resource;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("用户角色服务测试")
class UserRoleServiceImplTest {

    @Resource
    private IUserRoleService userRoleService;

    @Resource
    private IUserRoleTableService userRoleTableService;

    @Resource
    private IRoleTableService roleTableService;

    @Resource
    private IUserTableService userTableService;

    private String testUserId;
    private String testRoleId;

    @BeforeEach
    void setUp() {
        testUserId = "test-ur-" + CommonUtil.generateUuidV7().substring(0, 24);
        testRoleId = "test-rb-" + CommonUtil.generateUuidV7().substring(0, 24);

        // 先创建 UserTable，满足 user_role_table 的外键约束
        UserTable user = new UserTable();
        user.setId(testUserId);
        user.setNickname("ur-test-" + System.nanoTime());
        user.setPhone("170" + System.nanoTime() % 100000000L);
        user.setEmail("ur-" + System.nanoTime() + "@clms.local");
        user.setPassword(DigestUtil.md5Hex("Pass@123" + testUserId));
        user.setUserRoles(new JSONArray());
        user.setUserPermissions(new JSONArray());
        userTableService.save(user);

        RoleTable role = new RoleTable();
        role.setId(testRoleId);
        role.setRoleName("test-bind-role-" + System.nanoTime());
        role.setRoleDescription("test role for binding");
        role.setRoleStatus("enabled");
        role.setDefaultRole(false);
        roleTableService.save(role);
    }

    @AfterEach
    void tearDown() {
        userRoleTableService.remove(new LambdaQueryWrapper<UserRoleTable>().eq(UserRoleTable::getUserId, testUserId));
        roleTableService.removeById(testRoleId);
        userTableService.removeById(testUserId);
    }

    @Test
    @DisplayName("bindRolesToUser: 应正确创建用户角色关联")
    void bindRolesToUser_shouldCreateAssociations() {
        userRoleService.bindRolesToUser(testUserId, List.of(testRoleId));

        List<UserRoleTable> result = userRoleTableService.lambdaQuery()
                .eq(UserRoleTable::getUserId, testUserId)
                .list();
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testRoleId, result.get(0).getRoleId());
    }

    @Test
    @DisplayName("bindRolesToUser: 重复绑定相同角色不应创建重复记录")
    void bindRolesToUser_shouldNotDuplicateExistingRoles() {
        userRoleService.bindRolesToUser(testUserId, List.of(testRoleId));
        userRoleService.bindRolesToUser(testUserId, List.of(testRoleId));

        List<UserRoleTable> result = userRoleTableService.lambdaQuery()
                .eq(UserRoleTable::getUserId, testUserId)
                .list();
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("getDefaultRoles: 应返回所有默认角色")
    void getDefaultRoles_shouldReturnDefaultRoles() {
        String defaultRoleId = "test-dr-" + CommonUtil.generateUuidV7().substring(0, 24);
        RoleTable defaultRole = new RoleTable();
        defaultRole.setId(defaultRoleId);
        defaultRole.setRoleName("test-default-" + System.nanoTime());
        defaultRole.setRoleDescription("default role for test");
        defaultRole.setRoleStatus("enabled");
        defaultRole.setDefaultRole(true);
        roleTableService.save(defaultRole);

        try {
            List<RoleTable> result = userRoleService.getDefaultRoles();
            assertNotNull(result);
            assertFalse(result.isEmpty());
        } finally {
            roleTableService.removeById(defaultRoleId);
        }
    }

    @Test
    @DisplayName("getUserRoles: 无角色用户应返回空列表")
    void getUserRoles_shouldReturnEmptyForUserWithoutRoles() {
        List<RoleBO> result = userRoleService.getUserRoles(testUserId);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
