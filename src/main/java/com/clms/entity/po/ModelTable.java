package com.clms.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.clms.entity.base.BasePO;
import com.clms.handle.JSONObjectTypeHandler;

import cn.hutool.json.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 模块表
 * </p>
 *
 * @author Ling Bao
 * @since 2025-02-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName(value = "model_table", autoResultMap = true)
@Schema(name = "ModelTable对象", description = "模块表")
public class ModelTable extends BasePO {

    private static final long serialVersionUID = 12345345756L;

    /**
     * 模块名称
     */
    @Schema(description = "模块名称")
    private String modelName;

    /**
     * 模块描述
     */
    @Schema(description = "模块描述")
    private String modelDescription;

    /**
     * 模块元数据
     */
    @Schema(description = "模块元数据")
    @TableField(typeHandler = JSONObjectTypeHandler.class)
    private JSONObject metaData;
}
