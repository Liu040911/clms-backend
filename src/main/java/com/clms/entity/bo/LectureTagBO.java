package com.clms.entity.bo;

import com.clms.entity.po.TagTable;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Data;

@Data
public class LectureTagBO {
    private String id;

    private String name;

    private String description;

    private String type;

    private String icon;

    public LectureTagBO(TagTable table) {
        this.id = table.getId();
        this.name = table.getTagName();
        this.description = table.getTagDescription();
        this.type = table.getTagType();
        this.icon = extractIcon(table.getMetaData());
    }

    private String extractIcon(String metaData) {
        if (StrUtil.isBlank(metaData) || !JSONUtil.isTypeJSON(metaData)) {
            return "";
        }
        JSONObject obj = JSONUtil.parseObj(metaData);
        return obj.getStr("icon", "");
    }
}
