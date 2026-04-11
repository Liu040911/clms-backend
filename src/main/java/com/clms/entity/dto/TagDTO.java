package com.clms.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TagDTO {

    @NotBlank(message = "标签名称不能为空")
    private String tagName;

    private String tagDescription;

    @NotBlank(message = "标签类型不能为空")
    private String tagType;

    private String tagStatus;

    // 讲座类型标签使用的图标标识或URL，最终会写入meta_data.icon。
    private String icon;

    // JSON字符串，可选。示例：{"color":"blue"}
    private String metaData;

    private Boolean isSystem;
}
