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
import com.clms.entity.bo.ClassBO;
import com.clms.entity.dto.ClassDTO;
import com.clms.entity.po.ClassTable;
import com.clms.exception.BusinessException;
import com.clms.service.IClassService;
import com.clms.service.data.IClassTableService;

import jakarta.annotation.Resource;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("班级服务集成测试")
class ClassServiceImplTest {

    @Resource
    private IClassService classService;

    @Resource
    private IClassTableService classTableService;

    private String testClassId;
    private final String uniqueSuffix = String.valueOf(System.nanoTime());

    @BeforeEach
    void setUp() {
        testClassId = null;
    }

    @AfterEach
    void tearDown() {
        if (testClassId != null) {
            classTableService.removeById(testClassId);
        }
    }

    @Test
    @DisplayName("createClass: 正常创建班级")
    void createClass_shouldCreateSuccessfully() {
        ClassDTO dto = new ClassDTO();
        dto.setLocation("Room-" + uniqueSuffix);
        dto.setCapacity(30);
        dto.setStatus("active");

        classService.createClass(dto);

        ClassTable created = classTableService.lambdaQuery()
                .eq(ClassTable::getLocation, dto.getLocation())
                .one();
        assertNotNull(created);
        testClassId = created.getId();
        assertEquals(30, created.getCapacity());
        assertEquals("active", created.getStatus());
    }

    @Test
    @DisplayName("createClass: 空白状态默认active")
    void createClass_shouldDefaultStatusToActive() {
        ClassDTO dto = new ClassDTO();
        dto.setLocation("Default-" + uniqueSuffix);
        dto.setCapacity(50);

        classService.createClass(dto);
        ClassTable created = classTableService.lambdaQuery()
                .eq(ClassTable::getLocation, dto.getLocation())
                .one();
        assertNotNull(created);
        testClassId = created.getId();
        assertEquals("active", created.getStatus());
    }

    @Test
    @DisplayName("updateClass: 正常更新")
    void updateClass_shouldUpdateSuccessfully() {
        createTestClass("Update-" + uniqueSuffix, 20, "active");

        ClassDTO updateDto = new ClassDTO();
        updateDto.setLocation("Updated-" + uniqueSuffix);
        updateDto.setCapacity(40);
        updateDto.setStatus("inactive");

        classService.updateClass(testClassId, updateDto);

        ClassTable updated = classTableService.getById(testClassId);
        assertEquals(updateDto.getLocation(), updated.getLocation());
        assertEquals(40, updated.getCapacity());
        assertEquals("inactive", updated.getStatus());
    }

    @Test
    @DisplayName("updateClass: 不存在的ID应抛404")
    void updateClass_shouldThrowWhenNotExists() {
        ClassDTO dto = new ClassDTO();
        dto.setLocation("Ghost");
        dto.setCapacity(10);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> classService.updateClass("nonexistent-class", dto));
        assertEquals(404, ex.getCode());
    }

    @Test
    @DisplayName("deleteClass: 正常删除")
    void deleteClass_shouldDeleteSuccessfully() {
        createTestClass("Delete-" + uniqueSuffix, 15, "active");

        classService.deleteClass(testClassId);

        ClassTable deleted = classTableService.getById(testClassId);
        assertEquals(null, deleted);
    }

    @Test
    @DisplayName("deleteClass: 不存在的ID应抛404")
    void deleteClass_shouldThrowWhenNotExists() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> classService.deleteClass("nonexistent-class"));
        assertEquals(404, ex.getCode());
    }

    @Test
    @DisplayName("getClassInfo: 存在时返回BO")
    void getClassInfo_shouldReturnBOWhenExists() {
        createTestClass("Info-" + uniqueSuffix, 25, "active");

        ClassBO result = classService.getClassInfo(testClassId);
        assertNotNull(result);
        assertEquals("Info-" + uniqueSuffix, result.getLocation());
    }

    @Test
    @DisplayName("getClassInfo: 不存在时抛404")
    void getClassInfo_shouldThrowWhenNotExists() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> classService.getClassInfo("nonexistent-class"));
        assertEquals(404, ex.getCode());
    }

    @Test
    @DisplayName("getClassList: 分页查询")
    void getClassList_shouldReturnPage() {
        createTestClass("Page-" + uniqueSuffix, 10, "active");

        Page<ClassBO> page = classService.getClassList(null, null, 1, 10, null, null);
        assertNotNull(page);
        assertTrue(page.getTotal() >= 1);
    }

    @Test
    @DisplayName("getClassList: 按状态过滤")
    void getClassList_shouldFilterByStatus() {
        createTestClass("Active-" + uniqueSuffix, 10, "active");

        Page<ClassBO> page = classService.getClassList(null, "active", 1, 10, null, null);
        assertTrue(page.getRecords().stream().allMatch(c -> "active".equals(c.getStatus())));
    }

    @Test
    @DisplayName("getAvailableClassList: 应返回可用教室列表")
    void getAvailableClassList_shouldReturnAvailableClasses() {
        createTestClass("Avail-" + uniqueSuffix, 10, "active");

        java.util.List<ClassBO> result = classService.getAvailableClassList();
        assertNotNull(result);
        boolean found = false;
        for (ClassBO c : result) {
            if (("Avail-" + uniqueSuffix).equals(c.getLocation())) { found = true; break; }
        }
        assertTrue(found);
    }

    private void createTestClass(String location, int capacity, String status) {
        ClassDTO dto = new ClassDTO();
        dto.setLocation(location);
        dto.setCapacity(capacity);
        dto.setStatus(status);
        classService.createClass(dto);
        ClassTable saved = classTableService.lambdaQuery()
                .eq(ClassTable::getLocation, location)
                .one();
        testClassId = saved.getId();
    }
}
