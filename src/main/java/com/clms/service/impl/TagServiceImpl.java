package com.clms.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clms.entity.bo.TagBO;
import com.clms.entity.dto.TagDTO;
import com.clms.entity.po.TagTable;
import com.clms.exception.BusinessException;
import com.clms.service.ITagService;
import com.clms.service.data.ITagTableService;
import com.clms.utils.CommonUtil;
import com.clms.utils.DataContainerConvertor;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;

@Service
public class TagServiceImpl implements ITagService {

    @Resource
    private ITagTableService tagTableService;

    @Override
    public void createTag(TagDTO tagDTO) {
        // 名称做唯一校验，避免前端重复创建造成分类混乱。
        boolean exists = tagTableService.lambdaQuery()
                .eq(TagTable::getTagName, tagDTO.getTagName())
                .eq(TagTable::getTagType, tagDTO.getTagType())
                .exists();
        if (exists) {
            throw new BusinessException(400, "同类型下标签名称已存在");
        }

        TagTable tagTable = new TagTable();
        tagTable.setId(CommonUtil.generateUuidV7());
        tagTable.setTagName(StrUtil.trim(tagDTO.getTagName()));
        tagTable.setTagDescription(StrUtil.blankToDefault(StrUtil.trim(tagDTO.getTagDescription()), ""));
        String tagType = normalizeTagType(tagDTO.getTagType());
        tagTable.setTagType(tagType);
        tagTable.setTagStatus(normalizeTagStatus(tagDTO.getTagStatus()));
        tagTable.setCreatorId(String.valueOf(StpUtil.getLoginId()));
        // 讲座标签icon统一写入meta_data.icon，避免前后端解析分叉。
        tagTable.setMetaData(buildMetaData(tagType, tagDTO.getIcon(), tagDTO.getMetaData()));
        tagTable.setIsSystem(Boolean.TRUE.equals(tagDTO.getIsSystem()));

        boolean saved = tagTableService.save(tagTable);
        if (!saved) {
            throw new BusinessException(500, "创建标签失败");
        }
    }

    @Override
    public void updateTag(String tagId, TagDTO tagDTO) {
        TagTable existed = tagTableService.getById(tagId);
        if (existed == null) {
            throw new BusinessException(404, "标签不存在");
        }

        boolean duplicate = tagTableService.lambdaQuery()
                .eq(TagTable::getTagName, tagDTO.getTagName())
                .eq(TagTable::getTagType, tagDTO.getTagType())
                .ne(TagTable::getId, tagId)
                .exists();
        if (duplicate) {
            throw new BusinessException(400, "同类型下标签名称已存在");
        }

        existed.setTagName(StrUtil.trim(tagDTO.getTagName()));
        existed.setTagDescription(StrUtil.blankToDefault(StrUtil.trim(tagDTO.getTagDescription()), ""));
        String tagType = normalizeTagType(tagDTO.getTagType());
        existed.setTagType(tagType);
        existed.setTagStatus(normalizeTagStatus(tagDTO.getTagStatus()));
        // 更新时重建meta_data，确保讲座icon字段始终与标签类型一致。
        existed.setMetaData(buildMetaData(tagType, tagDTO.getIcon(), tagDTO.getMetaData()));
        existed.setIsSystem(Boolean.TRUE.equals(tagDTO.getIsSystem()));

        boolean updated = tagTableService.updateById(existed);
        if (!updated) {
            throw new BusinessException(500, "修改标签失败");
        }
    }

    @Override
    public void deleteTag(String tagId) {
        TagTable existed = tagTableService.getById(tagId);
        if (existed == null) {
            throw new BusinessException(404, "标签不存在");
        }

        boolean removed = tagTableService.removeById(tagId);
        if (!removed) {
            throw new BusinessException(500, "删除标签失败");
        }
    }

    @Override
    public TagBO getTagInfo(String tagId) {
        TagTable tagTable = tagTableService.getById(tagId);
        if (tagTable == null) {
            throw new BusinessException(404, "标签不存在");
        }
        return new TagBO(tagTable);
    }

    @Override
    public Page<TagBO> getTagList(String tagName, String tagType, String tagStatus, Integer page, Integer size, String sort, String order) {
        LambdaQueryChainWrapper<TagTable> query = tagTableService.lambdaQuery();

        if (StrUtil.isNotBlank(tagName)) {
            query.like(TagTable::getTagName, tagName);
        }
        if (StrUtil.isNotBlank(tagType)) {
            query.eq(TagTable::getTagType, tagType);
        }
        if (StrUtil.isNotBlank(tagStatus)) {
            query.eq(TagTable::getTagStatus, tagStatus);
        }

        // 支持前端字段排序，同时提供默认稳定排序。
        if (StrUtil.isNotBlank(sort) && StrUtil.isNotBlank(order)) {
            boolean isAsc = "asc".equalsIgnoreCase(order);
            if ("tagName".equals(sort)) {
                query = isAsc ? query.orderByAsc(TagTable::getTagName) : query.orderByDesc(TagTable::getTagName);
            } else if ("tagType".equals(sort)) {
                query = isAsc ? query.orderByAsc(TagTable::getTagType) : query.orderByDesc(TagTable::getTagType);
            } else if ("tagStatus".equals(sort)) {
                query = isAsc ? query.orderByAsc(TagTable::getTagStatus) : query.orderByDesc(TagTable::getTagStatus);
            } else if ("createTime".equals(sort)) {
                query = isAsc ? query.orderByAsc(TagTable::getCreateTime) : query.orderByDesc(TagTable::getCreateTime);
            } else if ("updateTime".equals(sort)) {
                query = isAsc ? query.orderByAsc(TagTable::getUpdateTime) : query.orderByDesc(TagTable::getUpdateTime);
            }
        } else {
            query = query.orderByDesc(TagTable::getCreateTime);
        }

        Page<TagTable> tablePage = query.page(new Page<>(page, size));
        return DataContainerConvertor.convertPage(tablePage, TagTable.class, TagBO.class);
    }

    private String normalizeTagType(String tagType) {
        if (StrUtil.isBlank(tagType)) {
            throw new BusinessException(400, "标签类型不能为空");
        }

        String normalized = StrUtil.trim(tagType);
        if (!"lecture".equals(normalized) && !"user".equals(normalized) && !"common".equals(normalized)) {
            throw new BusinessException(400, "标签类型非法");
        }
        return normalized;
    }

    private String normalizeTagStatus(String tagStatus) {
        if (StrUtil.isBlank(tagStatus)) {
            return "draft";
        }

        String normalized = StrUtil.trim(tagStatus);
        if (!"draft".equals(normalized) && !"active".equals(normalized) && !"inactive".equals(normalized)) {
            throw new BusinessException(400, "标签状态非法");
        }
        return normalized;
    }

    private String normalizeMetaData(String metaData) {
        if (StrUtil.isBlank(metaData)) {
            return null;
        }

        String normalized = StrUtil.trim(metaData);
        if (!JSONUtil.isTypeJSON(normalized)) {
            throw new BusinessException(400, "标签元数据必须是合法JSON");
        }
        return normalized;
    }

    private String buildMetaData(String tagType, String icon, String metaData) {
        String normalizedMetaData = normalizeMetaData(metaData);

        if (!"lecture".equals(tagType)) {
            // 非讲座标签不强制携带icon，避免污染其他业务标签语义。
            return normalizedMetaData;
        }

        if (StrUtil.isBlank(icon)) {
            throw new BusinessException(400, "讲座类型标签必须选择图标");
        }

        JSONObject json = StrUtil.isBlank(normalizedMetaData)
                ? JSONUtil.createObj()
                : JSONUtil.parseObj(normalizedMetaData);
        json.set("icon", StrUtil.trim(icon));
        return json.toString();
    }
}
