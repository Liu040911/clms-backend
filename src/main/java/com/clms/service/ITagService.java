package com.clms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clms.entity.bo.TagBO;
import com.clms.entity.dto.TagDTO;

public interface ITagService {

    void createTag(TagDTO tagDTO);

    void updateTag(String tagId, TagDTO tagDTO);

    void deleteTag(String tagId);

    TagBO getTagInfo(String tagId);

    Page<TagBO> getTagList(String tagName, String tagType, String tagStatus, Integer page, Integer size, String sort, String order);
}
