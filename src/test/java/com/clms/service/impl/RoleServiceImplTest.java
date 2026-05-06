package com.clms.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clms.entity.bo.RoleBO;
import com.clms.entity.dto.RoleDTO;
import com.clms.entity.po.RolePermissionTable;
import com.clms.entity.po.RoleTable;
import com.clms.exception.BusinessException;
import com.clms.service.IRoleService;
import com.clms.service.data.IRolePermissionTableService;
import com.clms.service.data.IRoleTableService;

import jakarta.annotation.Resource;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("角色服务集成测试")
class RoleServiceImplTest {

    @Resource
    private IRoleService roleService;

    @Resource
    private IRoleTableService roleTableService;

    @Resource
    private IRolePermissionTableService rolePermissionTableService;

    private String testRoleId;
    private final String uniqueSuffix = String.valueOf(System.nanoTime());

    @BeforeEach
    void setUp() {
        testRoleId = null;
    }

    @AfterEach
    void tearDown() {
        if (testRoleId != null) {
            rolePermissionTableService.remove(
                    new LambdaQueryWrapper<RolePermissionTable>().eq(RolePermissionTable::getRoleId, testRoleId));
            roleTableService.removeById(testRoleId);
        }
    }

    @Test
    @DisplayName("createRole: 正常创建角色")
    void createRole_shouldCreateSuccessfully() {
        RoleDTO dto = buildRoleDTO("test-create-" + uniqueSuffix);
        roleService.createRole(dto);

        RoleTable created = roleTableService.lambdaQuery()
                .eq(RoleTable::getRoleName, dto.getRoleName())
                .one();
        assertNotNull(created);
        testRoleId = created.getId();
        assertEquals("test create desc", created.getRoleDescription());
        assertEquals("active", created.getRoleStatus());
    }

    @Test
    @DisplayName("createRole: 重名应抛400")
    void createRole_shouldThrowWhenNameDuplicate() {
        RoleDTO first = buildRoleDTO("dup-" + uniqueSuffix);
        roleService.createRole(first);
        RoleTable saved = roleTableService.lambdaQuery()
                .eq(RoleTable::getRoleName, first.getRoleName())
                .one();
        testRoleId = saved.getId();

        BusinessException ex = assertThrows(BusinessException.class,
                () -> roleService.createRole(first));
        assertEquals(400, ex.getCode());
    }

    @Test
    @DisplayName("updateRole: 正常更新")
    void updateRole_shouldUpdateSuccessfully() {
        createTestRole("update-src-" + uniqueSuffix);

        RoleDTO dto = buildRoleDTO("update-dst-" + uniqueSuffix);
        roleService.updateRole(testRoleId, dto);

        RoleTable updated = roleTableService.getById(testRoleId);
        assertEquals("update-dst-" + uniqueSuffix, updated.getRoleName());
    }

    @Test
    @DisplayName("updateRole: 不存在应抛404")
    void updateRole_shouldThrowWhenNotExists() {
        RoleDTO dto = buildRoleDTO("ghost-" + uniqueSuffix);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> roleService.updateRole("nonexistent-role-id", dto));
        assertEquals(404, ex.getCode());
    }

    @Test
    @DisplayName("deleteRole: 正常删除")
    void deleteRole_shouldDeleteSuccessfully() {
        createTestRole("delete-me-" + uniqueSuffix);

        roleService.deleteRole(testRoleId);

        RoleTable deleted = roleTableService.getById(testRoleId);
        assertEquals(null, deleted);
    }

    @Test
    @DisplayName("deleteRole: 不存在应抛404")
    void deleteRole_shouldThrowWhenNotExists() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> roleService.deleteRole("nonexistent-role-id"));
        assertEquals(404, ex.getCode());
    }

    @Test
    @DisplayName("getRole: 存在时返回BO")
    void getRole_shouldReturnBOWhenExists() {
        createTestRole("get-me-" + uniqueSuffix);

        RoleBO result = roleService.getRole(testRoleId);
        assertNotNull(result);
        assertEquals("get-me-" + uniqueSuffix, result.getRoleName());
    }

    @Test
    @DisplayName("getRole: 不存在应抛404")
    void getRole_shouldThrowWhenNotExists() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> roleService.getRole("nonexistent-role-id"));
        assertEquals(404, ex.getCode());
    }

    @Test
    @DisplayName("getRoleList: 分页查询")
    void getRoleList_shouldReturnPage() {
        createTestRole("page-test-" + uniqueSuffix);

        Page<RoleBO> page = roleService.getRoleList(null, 1, 10, null, null);
        assertNotNull(page);
        assertTrue(page.getTotal() >= 1);
    }

    @Test
    @DisplayName("getPermissionList: 无权限的角色应返回空列表")
    void getPermissionList_shouldReturnEmptyForRoleWithoutPermissions() {
        createTestRole("no-perm-" + uniqueSuffix);

        List<?> permissions = roleService.getPermissionList(testRoleId);
        assertNotNull(permissions);
        assertTrue(permissions.isEmpty());
    }

    @Test
    @DisplayName("getPermissionModules: 无权限的角色应返回空集合")
    void getPermissionModules_shouldReturnEmptySetForRoleWithoutPermissions() {
        createTestRole("no-module-" + uniqueSuffix);

        Set<String> modules = roleService.getPermissionModules(testRoleId);
        assertNotNull(modules);
        assertTrue(modules.isEmpty());
    }

    private void createTestRole(String roleName) {
        RoleDTO dto = buildRoleDTO(roleName);
        roleService.createRole(dto);
        RoleTable saved = roleTableService.lambdaQuery()
                .eq(RoleTable::getRoleName, roleName)
                .one();
        testRoleId = saved.getId();
    }

    private RoleDTO buildRoleDTO(String roleName) {
        RoleDTO dto = new RoleDTO();
        dto.setRoleName(roleName);
        dto.setRoleDescription("test create desc");
        dto.setRoleStatus("active");
        dto.setDefaultRole(false);
        return dto;
    }
}
