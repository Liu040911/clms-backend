package com.clms.handle;

import org.apache.ibatis.type.BaseTypeHandler;

import cn.hutool.json.JSONObject;

/**
 * JSON 对象类型处理器
 * 
 * 用于 MyBatis-Plus 数据库字段和 Java 对象属性的类型转换，
 * 将数据库中的 JSON 字符串自动转换为 Hutool JSONObject 对象，
 * 反之亦然。
 * 
 * 使用方式：在实体类属性上使用 @TableField 注解指定此处理器
 * 例：@TableField(typeHandler = JSONObjectTypeHandler.class)
 * 
 * @author Liu
 * @since 1.0.0
 */
public class JSONObjectTypeHandler extends BaseTypeHandler<JSONObject> {

    @Override
    public void setNonNullParameter(java.sql.PreparedStatement ps, int i,
            JSONObject parameter,
            org.apache.ibatis.type.JdbcType jdbcType)
            throws java.sql.SQLException {
        ps.setString(i, parameter.toJSONString(0));
    }

    @Override
    public JSONObject getNullableResult(java.sql.ResultSet rs, String columnName) throws java.sql.SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : new JSONObject(value);
    }

    @Override
    public JSONObject getNullableResult(java.sql.ResultSet rs, int columnIndex) throws java.sql.SQLException {
        String value = rs.getString(columnIndex);
        return value == null ? null : new JSONObject(value);
    }

    @Override
    public JSONObject getNullableResult(java.sql.CallableStatement cs, int columnIndex) throws java.sql.SQLException {
        String value = cs.getString(columnIndex);
        return value == null ? null : new JSONObject(value);
    }

}
