package com.clms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * 返回值枚举
 */
@Getter
@AllArgsConstructor
public enum ResponseCode {

    /**
     * 状态码
     */
    SUCCESS(200, "SUCCESS"),
    FAILURE(999, "FAILURE"),

    UNAUTHORIZED(401, "尚未登录，不能访问"),
    FORBIDDEN(403, "没有权限，禁止访问"),
    NOT_FOUND(404, "数据不存在"),
    PARAMETER_ERROR(405, "参数错误"),
    LOGIN_TIMEOUT(408, "登录失效，请重新登录"),

    SERVER_ERROR(500, "服务器异常，请稍后再试"),
    FORCE_UPDATE(501, "请更新至最新版本"),
    PROCESSING(502, "处理中，请稍后"),
    DATA_EXCEPTION(503, "数据异常，请联系客服处理"),


    ;

    Integer code;
    String message;

    public static ResponseCode findByCode(Integer code) {
        if (code == null){
            return null;
        }
        for (ResponseCode responseCode : ResponseCode.values()){
            if (Objects.equals(responseCode.code, code)){
                return responseCode;
            }
        }
        return null;
    }
}