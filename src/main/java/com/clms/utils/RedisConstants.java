package com.clms.utils;

public class RedisConstants {

    // 认证验证码
    public static final String AUTH_CODE = "auth:code:";
    // 认证验证码存活时间
    public static final Long AUTH_CODE_TTL = 5L;

    // 发送频率限制
    public static final String SEND_CODE_LIMIT = "send:code:limit:";
    // 发送频率限制存活时间
    public static final Long SEND_CODE_LIMIT_TTL = 1L;

    // 分步验证状态：主要用于在多步骤认证过程中存储用户的验证状态token
    public static final String STEP_TOKEN = "step:token:";
    // 分步验证状态存活时间
    public static final Long STEP_TOKEN_TTL = 10L;
}
