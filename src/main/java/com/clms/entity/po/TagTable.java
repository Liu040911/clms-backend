package com.clms.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.clms.entity.base.BasePO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tag_table", autoResultMap = true)
public class TagTable extends BasePO {
    private String id;

    private String tagName;

    private String tagDescription;

    private String tagType;

    private String tagStatus;

    private String creatorId;

    private String metaData;

    private Boolean isSystem;
}
