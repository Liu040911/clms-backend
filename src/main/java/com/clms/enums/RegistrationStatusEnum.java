package com.clms.enums;

import java.util.Arrays;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RegistrationStatusEnum {

    PENDING("pending", "已报名"),
    CANCELLED("cancelled", "已取消"),
    CHECKED_IN("checked_in", "已签到"),
    NOT_SIGNED_IN("not_signed_in", "未签到");

    private final String status;
    private final String description;

    public static boolean isValid(String status) {
        if (StrUtil.isBlank(status)) {
            return false;
        }
        return Arrays.stream(values()).anyMatch(item -> StrUtil.equals(item.status, status));
    }
}
