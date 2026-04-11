package com.clms.entity.bo;

import java.time.LocalDateTime;

import com.clms.entity.po.TagTable;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Data;

@Data
public class TagBO {

    private String id;

    private String tagName;

    private String tagDescription;

    private String tagType;

    private String tagStatus;

    private String creatorId;

    private String metaData;

    private String icon;

    private Boolean isSystem;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public TagBO(TagTable table) {
        this.id = table.getId();
        this.tagName = table.getTagName();
        this.tagDescription = table.getTagDescription();
        this.tagType = table.getTagType();
        this.tagStatus = table.getTagStatus();
        this.creatorId = table.getCreatorId();
        this.metaData = table.getMetaData();
        this.icon = extractIcon(table.getMetaData());
        this.isSystem = table.getIsSystem();
        this.createTime = table.getCreateTime();
        this.updateTime = table.getUpdateTime();
    }

    private String extractIcon(String metaData) {
        if (StrUtil.isBlank(metaData) || !JSONUtil.isTypeJSON(metaData)) {
            return "";
        }
        JSONObject obj = JSONUtil.parseObj(metaData);
        return obj.getStr("icon", "");
    }
}
