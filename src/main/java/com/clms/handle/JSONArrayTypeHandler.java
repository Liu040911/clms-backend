package com.clms.handle;

import org.apache.ibatis.type.BaseTypeHandler;

import cn.hutool.json.JSONArray;

/**
 * JSON 数组类型处理器
 * 
 * 用于 MyBatis-Plus 数据库字段和 Java 对象属性的类型转换，
 * 将数据库中的 JSON 字符串自动转换为 Hutool JSONArray 对象，
 * 反之亦然。
 * 
 * 使用方式：在实体类属性上使用 @TableField 注解指定此处理器
 * 例：@TableField(typeHandler = JSONArrayTypeHandler.class)
 * 
 * @author Liu
 * @since 1.0.0
 */
public class JSONArrayTypeHandler extends BaseTypeHandler<JSONArray> {

    @Override
    public void setNonNullParameter(java.sql.PreparedStatement ps, int i, JSONArray parameter,
            org.apache.ibatis.type.JdbcType jdbcType)
            throws java.sql.SQLException {
        ps.setString(i, parameter.toString());
    }

    @Override
    public JSONArray getNullableResult(java.sql.ResultSet rs, String columnName) throws java.sql.SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : new JSONArray(value);
    }

    @Override
    public JSONArray getNullableResult(java.sql.ResultSet rs, int columnIndex) throws java.sql.SQLException {
        String value = rs.getString(columnIndex);
        return value == null ? null : new JSONArray(value);
    }

    @Override
    public JSONArray getNullableResult(java.sql.CallableStatement cs, int columnIndex) throws java.sql.SQLException {
        String value = cs.getString(columnIndex);
        return value == null ? null : new JSONArray(value);
    }

}
