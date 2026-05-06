package com.clms.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.clms.entity.bo.PermissionBO;
import com.clms.entity.po.PermissionTable;
import com.clms.service.IPermissionService;
import com.clms.service.data.IPermissionTableService;
import com.clms.utils.CommonUtil;

import jakarta.annotation.Resource;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("权限服务测试")
class PermissionServiceImplTest {

    @Resource
    private IPermissionService permissionService;

    @Resource
    private IPermissionTableService permissionTableService;

    private String testPermissionId;

    @BeforeEach
    void setUp() {
        testPermissionId = "test-perm-" + CommonUtil.generateUuidV7().substring(0, 12);
        PermissionTable perm = new PermissionTable();
        perm.setId(testPermissionId);
        perm.setPermissionName("test:read:" + CommonUtil.generateUuidV7().substring(0, 8));
        perm.setPermissionString("test:read");
        permissionTableService.save(perm);
    }

    @AfterEach
    void tearDown() {
        permissionTableService.removeById(testPermissionId);
    }

    @Test
    @DisplayName("getPermissionById: 存在时应返回对应BO")
    void getPermissionById_shouldReturnBOWhenExists() {
        PermissionBO result = permissionService.getPermissionById(testPermissionId);
        assertNotNull(result);
        assertEquals(testPermissionId, result.getId());
    }

    @Test
    @DisplayName("getPermissionById: 不存在时应返回null")
    void getPermissionById_shouldReturnNullWhenNotExists() {
        PermissionBO result = permissionService.getPermissionById("nonexistent-id");
        assertNull(result);
    }

    @Test
    @DisplayName("getAllPermission: 应返回列表")
    void getAllPermission_shouldReturnList() {
        List<PermissionBO> result = permissionService.getAllPermission();
        assertNotNull(result);
        assertTrue(result.stream().anyMatch(p -> testPermissionId.equals(p.getId())));
    }

    @Test
    @DisplayName("getBatchPermissions: 空列表应返回空Map")
    void getBatchPermissions_shouldReturnEmptyMapForEmptyInput() {
        Map<String, PermissionBO> result = permissionService.getBatchPermissions(List.of());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getBatchPermissions: null应返回空Map")
    void getBatchPermissions_shouldReturnEmptyMapForNull() {
        Map<String, PermissionBO> result = permissionService.getBatchPermissions(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getBatchPermissions: 有效ID应返回对应BO")
    void getBatchPermissions_shouldReturnBOsForValidIds() {
        Map<String, PermissionBO> result = permissionService.getBatchPermissions(List.of(testPermissionId));
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNotNull(result.get(testPermissionId));
    }

    @Test
    @DisplayName("addPermission: 应创建并返回新ID")
    void addPermission_shouldCreateAndReturnId() {
        PermissionTable template = new PermissionTable();
        template.setPermissionString("test:write");
        template.setPermissionName("test:write:perm");
        PermissionBO bo = new PermissionBO(template);

        String newId = permissionService.addPermission(bo);

        assertNotNull(newId);
        PermissionTable saved = permissionTableService.getById(newId);
        assertNotNull(saved);
        assertEquals("test:write", saved.getPermissionString());
        permissionTableService.removeById(newId);
    }
}
