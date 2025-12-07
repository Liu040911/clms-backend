package com.clms.entity.bo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VerificationCodeBO {
    // 凭证：邮箱或手机号
    private String credential;
    // 验证码
    private String code;
}
