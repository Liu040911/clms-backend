package com.clms.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.clms.entity.base.BasePO;
import com.clms.handle.JSONObjectTypeHandler;

import cn.hutool.json.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "app_config_table", autoResultMap = true)
@Schema(name = "AppConfigTable对象", description = "应用配置表")
public class AppConfigTable extends BasePO {

    @Schema(description = "配置数据")
    @TableField(typeHandler = JSONObjectTypeHandler.class)
    private JSONObject configData;
}
