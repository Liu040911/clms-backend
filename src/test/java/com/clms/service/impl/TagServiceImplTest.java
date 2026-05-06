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
import com.clms.entity.bo.TagBO;
import com.clms.entity.dto.TagDTO;
import com.clms.entity.po.TagTable;
import com.clms.exception.BusinessException;
import com.clms.service.ITagService;
import com.clms.service.data.ITagTableService;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONObject;
import jakarta.annotation.Resource;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("标签服务集成测试")
class TagServiceImplTest {

    @Resource
    private ITagService tagService;

    @Resource
    private ITagTableService tagTableService;

    private String testTagId;
    private final String uniqueSuffix = String.valueOf(System.nanoTime());

    @BeforeEach
    void setUp() {
        testTagId = null;
        StpUtil.login("tag-tester-" + System.nanoTime());
    }

    @AfterEach
    void tearDown() {
        StpUtil.logout();
        if (testTagId != null) {
            tagTableService.removeById(testTagId);
        }
    }

    @Test
    @DisplayName("createTag: 正常创建标签")
    void createTag_shouldCreateSuccessfully() {
        TagDTO dto = buildTagDTO("test-lecture-" + uniqueSuffix, "lecture", "active");
        dto.setIcon("book");
        tagService.createTag(dto);

        TagTable created = tagTableService.lambdaQuery()
                .eq(TagTable::getTagName, dto.getTagName())
                .one();
        assertNotNull(created);
        testTagId = created.getId();
        assertEquals("lecture", created.getTagType());
        assertEquals("active", created.getTagStatus());
        String metaDataStr = created.getMetaData();
        assertNotNull(metaDataStr);
        JSONObject parsed = new JSONObject(metaDataStr);
        assertEquals("book", parsed.getStr("icon"));
    }

    @Test
    @DisplayName("createTag: 同类型下重名应抛异常")
    void createTag_shouldThrowWhenDuplicateName() {
        TagDTO first = buildTagDTO("dup-lecture-" + uniqueSuffix, "lecture", "draft");
        first.setIcon("book");
        tagService.createTag(first);
        TagTable saved = tagTableService.lambdaQuery()
                .eq(TagTable::getTagName, first.getTagName())
                .one();
        testTagId = saved != null ? saved.getId() : null;

        TagDTO second = buildTagDTO(first.getTagName(), "lecture", "draft");
        BusinessException ex = assertThrows(BusinessException.class, () -> tagService.createTag(second));
        assertEquals(400, ex.getCode());
    }

    @Test
    @DisplayName("createTag: 讲座标签缺少icon应抛异常")
    void createTag_shouldThrowWhenLectureTagMissingIcon() {
        TagDTO dto = buildTagDTO("no-icon-lecture-" + uniqueSuffix, "lecture", "draft");
        BusinessException ex = assertThrows(BusinessException.class, () -> tagService.createTag(dto));
        assertEquals(400, ex.getCode());
    }

    @Test
    @DisplayName("createTag: 非法标签类型应抛异常")
    void createTag_shouldThrowWhenInvalidType() {
        TagDTO dto = buildTagDTO("bad-type-" + uniqueSuffix, "invalid", "draft");
        BusinessException ex = assertThrows(BusinessException.class, () -> tagService.createTag(dto));
        assertEquals(400, ex.getCode());
    }

    @Test
    @DisplayName("createTag: 标签类型为空应抛异常")
    void createTag_shouldThrowWhenEmptyType() {
        TagDTO dto = buildTagDTO("empty-type-" + uniqueSuffix, "", "draft");
        BusinessException ex = assertThrows(BusinessException.class, () -> tagService.createTag(dto));
        assertEquals(400, ex.getCode());
    }

    @Test
    @DisplayName("createTag: 非法标签状态应抛异常")
    void createTag_shouldThrowWhenInvalidStatus() {
        TagDTO dto = buildTagDTO("bad-status-" + uniqueSuffix, "common", "deleted");
        BusinessException ex = assertThrows(BusinessException.class, () -> tagService.createTag(dto));
        assertEquals(400, ex.getCode());
    }

    @Test
    @DisplayName("createTag: 空白状态默认draft")
    void createTag_shouldDefaultToDraftWhenStatusBlank() {
        TagDTO dto = buildTagDTO("blank-status-" + uniqueSuffix, "common", null);
        tagService.createTag(dto);

        TagTable created = tagTableService.lambdaQuery()
                .eq(TagTable::getTagName, dto.getTagName())
                .one();
        assertNotNull(created);
        testTagId = created.getId();
        assertEquals("draft", created.getTagStatus());
    }

    @Test
    @DisplayName("updateTag: 正常更新标签")
    void updateTag_shouldUpdateSuccessfully() {
        TagDTO createDto = buildTagDTO("update-src-" + uniqueSuffix, "common", "draft");
        tagService.createTag(createDto);
        TagTable saved = tagTableService.lambdaQuery()
                .eq(TagTable::getTagName, createDto.getTagName())
                .one();
        testTagId = saved.getId();

        TagDTO updateDto = buildTagDTO("update-dst-" + uniqueSuffix, "common", "active");
        tagService.updateTag(testTagId, updateDto);

        TagTable updated = tagTableService.getById(testTagId);
        assertEquals(updateDto.getTagName(), updated.getTagName());
        assertEquals("active", updated.getTagStatus());
    }

    @Test
    @DisplayName("updateTag: 不存在的ID应抛404")
    void updateTag_shouldThrowWhenNotExists() {
        TagDTO dto = buildTagDTO("ghost-" + uniqueSuffix, "common", "draft");
        BusinessException ex = assertThrows(BusinessException.class,
                () -> tagService.updateTag("nonexistent-id-999", dto));
        assertEquals(404, ex.getCode());
    }

    @Test
    @DisplayName("deleteTag: 正常删除标签")
    void deleteTag_shouldDeleteSuccessfully() {
        TagDTO dto = buildTagDTO("delete-me-" + uniqueSuffix, "common", "draft");
        tagService.createTag(dto);
        TagTable saved = tagTableService.lambdaQuery()
                .eq(TagTable::getTagName, dto.getTagName())
                .one();
        testTagId = saved.getId();

        tagService.deleteTag(testTagId);

        TagTable deleted = tagTableService.getById(testTagId);
        assertEquals(null, deleted);
    }

    @Test
    @DisplayName("deleteTag: 不存在的ID应抛404")
    void deleteTag_shouldThrowWhenNotExists() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> tagService.deleteTag("nonexistent-id-999"));
        assertEquals(404, ex.getCode());
    }

    @Test
    @DisplayName("getTagInfo: 存在的标签返回BO")
    void getTagInfo_shouldReturnBOWhenExists() {
        TagDTO dto = buildTagDTO("get-me-" + uniqueSuffix, "common", "draft");
        tagService.createTag(dto);
        TagTable saved = tagTableService.lambdaQuery()
                .eq(TagTable::getTagName, dto.getTagName())
                .one();
        testTagId = saved.getId();

        TagBO result = tagService.getTagInfo(testTagId);
        assertNotNull(result);
        assertEquals(dto.getTagName(), result.getTagName());
    }

    @Test
    @DisplayName("getTagInfo: 不存在的标签应抛404")
    void getTagInfo_shouldThrowWhenNotExists() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> tagService.getTagInfo("nonexistent-id-999"));
        assertEquals(404, ex.getCode());
    }

    @Test
    @DisplayName("getTagList: 分页查询")
    void getTagList_shouldReturnPage() {
        TagDTO dto = buildTagDTO("page-test-" + uniqueSuffix, "common", "active");
        tagService.createTag(dto);
        TagTable saved = tagTableService.lambdaQuery()
                .eq(TagTable::getTagName, dto.getTagName())
                .one();
        testTagId = saved.getId();

        Page<TagBO> page = tagService.getTagList(null, null, null, 1, 10, null, null);
        assertNotNull(page);
        assertTrue(page.getTotal() >= 1);
    }

    @Test
    @DisplayName("getTagList: 按类型过滤")
    void getTagList_shouldFilterByType() {
        TagDTO dto = buildTagDTO("type-filter-" + uniqueSuffix, "user", "active");
        tagService.createTag(dto);
        TagTable saved = tagTableService.lambdaQuery()
                .eq(TagTable::getTagName, dto.getTagName())
                .one();
        testTagId = saved.getId();

        Page<TagBO> page = tagService.getTagList(null, "user", null, 1, 10, null, null);
        assertNotNull(page);
        assertTrue(page.getRecords().stream().allMatch(t -> "user".equals(t.getTagType())));
    }

    private TagDTO buildTagDTO(String name, String type, String status) {
        TagDTO dto = new TagDTO();
        dto.setTagName(name);
        dto.setTagType(type);
        dto.setTagStatus(status);
        dto.setTagDescription("test desc");
        return dto;
    }
}
