package com.clms.service.impl;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponseBody;
import com.aliyun.teaopenapi.models.Config;
import com.clms.service.ISmsService;
import com.clms.utils.RedisConstants;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 阿里云短信认证服务
 */
@Slf4j
@Service
public class AliYunSmsServiceImpl implements ISmsService {

    @Value("${aliyun.sms.access-key-id:}")
    private String accessKeyId;

    @Value("${aliyun.sms.access-key-secret:}")
    private String accessKeySecret;

    @Value("${aliyun.sms.region:cn-hangzhou}")
    private String region;

    @Value("${aliyun.sms.sign-name:}")
    private String signName;

    @Value("${aliyun.sms.template-code:}")
    private String templateCode;

    @Value("${aliyun.sms.country-code:86}")
    private String countryCode;

    @Value("${aliyun.sms.scheme-name:默认方案}")
    private String schemeName;

    @Value("${aliyun.sms.valid-time:300}")
    private Integer validTime;

    @Value("${aliyun.sms.interval:60}")
    private Integer interval;

    @Value("${aliyun.sms.code-length:4}")
    private Integer codeLength;

    @Value("${aliyun.sms.code-type:1}")
    private Integer codeType;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void sendSms(String phone, String code) throws Exception {
        if (!isValidPhoneNumber(phone)) {
            throw new IllegalArgumentException("Invalid phone number: " + phone);
        }

        if (isBlank(accessKeyId) || isBlank(accessKeySecret) || isBlank(signName) || isBlank(templateCode)) {
            throw new IllegalStateException("Aliyun SMS config is incomplete. Please check access-key/sign-name/template-code.");
        }

        String normalizedPhone = normalizePhoneNumber(phone);
        String templateParam = buildTemplateParam(code);

        log.info("准备发送阿里云短信验证码 - phone: {}, sign: {}, templateCode: {}",
                normalizedPhone, signName, templateCode);

        Client client = createClient();
        SendSmsVerifyCodeRequest request = new SendSmsVerifyCodeRequest()
                .setPhoneNumber(normalizedPhone)
                .setCountryCode(countryCode.replace("+", ""))
                .setSignName(signName)
                .setTemplateCode(templateCode)
                .setTemplateParam(templateParam)
                .setSchemeName(schemeName)
                .setCodeLength(codeLength.longValue())
                .setValidTime(validTime.longValue())
                .setInterval(interval.longValue())
            .setCodeType(codeType.longValue())
            .setReturnVerifyCode(true);

        SendSmsVerifyCodeResponse response = client.sendSmsVerifyCode(request);
        SendSmsVerifyCodeResponseBody body = response.getBody();

        boolean success = body != null && Boolean.TRUE.equals(body.getSuccess()) && "OK".equalsIgnoreCase(body.getCode());
        if (!success) {
            String errorCode = body == null ? "UNKNOWN" : body.getCode();
            String message = body == null ? "response body is null" : body.getMessage();
            log.error("阿里云短信发送失败 - phone: {}, code: {}, message: {}", normalizedPhone, errorCode, message);
            throw new RuntimeException("Aliyun SMS send failed: " + errorCode + " - " + message);
        }

        log.info("阿里云短信发送成功 - phone: {}, requestId: {}", normalizedPhone, body.getRequestId());
    }

    @Override
    public String getProviderName() {
        return "Aliyun";
    }

    private boolean isValidPhoneNumber(String phone) {
        if (isBlank(phone)) {
            return false;
        }
        String clean = phone.replaceAll("[^0-9]", "");
        if (clean.startsWith("86") && clean.length() == 13) {
            clean = clean.substring(2);
        }
        return clean.matches("^1[3-9]\\d{9}$");
    }

    private String normalizePhoneNumber(String phone) {
        String clean = phone.replaceAll("[^0-9]", "");
        if (clean.startsWith("86") && clean.length() == 13) {
            return clean.substring(2);
        }
        return clean;
    }

    private String buildTemplateParam(String code) {
        return "{\"code\":\"" + code + "\",\"min\":\"" + RedisConstants.AUTH_CODE_TTL + "\"}";
    }

    protected Client createClient() throws Exception {
        Config config = new Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret)
                .setEndpoint("dypnsapi.aliyuncs.com")
                .setRegionId(region);
        return new Client(config);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
