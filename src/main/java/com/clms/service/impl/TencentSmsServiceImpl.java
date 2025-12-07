package com.clms.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.clms.service.ISmsService;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import com.tencentcloudapi.sms.v20210111.models.SendStatus;

/**
 * 腾讯云短信服务
 */
@Slf4j
@Service
public class TencentSmsServiceImpl implements ISmsService{

    @Value("${tencent.cloud.sms.secret-id:AKIDxxxxxxxxxxxxxxxxxx}")
    private String secretId;

    @Value("${tencent.cloud.sms.secret-key:xxxxxxxxxxxxxxxxxxxx}")
    private String secretKey;

    @Value("${tencent.cloud.sms.app-id:1400000000}")
    private String appId;

    @Value("${tencent.cloud.sms.sign-name:校园讲座}")
    private String signName;

    @Value("${tencent.cloud.sms.template-id:123456}")
    private String templateId;

    @Value("${tencent.cloud.sms.region:ap-beijing}")
    private String region;

    @Value("${tencent.cloud.sms.country-code:+86}")
    private String countryCode;

    /**
     * 发送验证码短信
     *
     * @param credential 手机号或邮箱
     * @param code       验证码
     * @return 是否发送成功
     */
    @Override
    public void sendSms(String phone, String code) throws Exception {
        try {
            // 验证手机号格式
            if (!isValidPhoneNumber(phone)) {
                log.error("Invalid phone number: {}", phone);
                throw new IllegalArgumentException("Invalid phone number: " + phone);
            }

            // 格式化手机号
            String phoneNumber = formatPhoneNumber(phone);

            log.info("准备发送短信 - 手机号: {}, 验证码: {}", phoneNumber, code);
            log.info("使用的配置信息 - AppId: {}, SignName: {}, TemplateId: {}",
                    appId,
                    signName,
                    templateId);

            // 使用腾讯云 SDK 发送短信
            sendSmsWithTencentCloud(phoneNumber, code);

        } catch (Exception e) {
            log.error("发送短信异常 - phone: {}, error: {}", phone, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 验证手机号格式
     *
     * @param phoneNumber 手机号
     * @return 是否有效
     */
    private boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        // 中国手机号格式：11位数字，开头是1
        // 支持格式：13800000000、+8613800000000、0086-13800000000
        String cleanNumber = phoneNumber.replaceAll("[^0-9]", "");
        
        // 如果是国际格式，需要以86开头（中国）
        if (phoneNumber.contains("+") || phoneNumber.contains("00")) {
            return cleanNumber.endsWith("1") && cleanNumber.length() == 12;
        }
        
        // 国内格式，11位数字开头为1
        return cleanNumber.matches("^1[3-9]\\d{9}$") && cleanNumber.length() == 11;
    }

    /**
     * 格式化手机号为腾讯云要求的格式 (+86开头)
     *
     * @param phoneNumber 原始手机号
     * @return 格式化后的手机号
     */
    private String formatPhoneNumber(String phoneNumber) {
        // 移除所有非数字字符
        String cleanNumber = phoneNumber.replaceAll("[^0-9]", "");

        // 如果是11位数字（国内格式），加上 +86
        if (cleanNumber.length() == 11 && cleanNumber.startsWith("1")) {
            return "+86" + cleanNumber;
        }

        // 如果是12位数字且以86开头（国际格式），加上 +
        if (cleanNumber.length() == 12 && cleanNumber.startsWith("86")) {
            return "+" + cleanNumber;
        }

        // 其他情况返回原始格式
        return phoneNumber;
    }

    /**
     * 使用腾讯云 SDK 发送短信
     *
     * @param phoneNumber 手机号（已格式化为 +86 开头）
     * @param code        验证码
     * @return 是否发送成功
     */
    private boolean sendSmsWithTencentCloud(String phoneNumber, String code) {
        try {
            // 实例化一个认证对象，入参需要传入腾讯云账户secretId，secretKey
            Credential cred = new Credential(secretId, secretKey);
            
            // 实例化一个http选项，可选，没有特殊需求可以跳过
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("sms.tencentcloudapi.com");
            
            // 实例化一个client选项，可选，没有特殊需求可以跳过
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            
            // 实例化要请求产品的client对象,clientProfile是可选的
            SmsClient client = new SmsClient(cred, region, clientProfile);
            
            // 实例化一个请求对象,每个接口都会对应一个request类
            SendSmsRequest req = new SendSmsRequest();
            req.setSmsSdkAppId(appId);
            req.setSignName(signName);
            req.setTemplateId(templateId);
            req.setPhoneNumberSet(new String[]{phoneNumber});
            req.setTemplateParamSet(new String[]{code});
            
            // 返回的resp是一个SendSmsResponse的实例，与请求对象对应
            SendSmsResponse resp = client.SendSms(req);
            
            // 检查发送结果
            if (resp.getSendStatusSet() != null && resp.getSendStatusSet().length > 0) {
                SendStatus status = resp.getSendStatusSet()[0];
                if ("Ok".equals(status.getCode())) {
                    log.info("短信发送成功 - 手机号: {}, 序列号: {}", phoneNumber, status.getSerialNo());
                    return true;
                } else {
                    log.error("短信发送失败 - 手机号: {}, 错误码: {}, 错误信息: {}", 
                            phoneNumber, status.getCode(), status.getMessage());
                    return false;
                }
            } else {
                log.error("短信发送失败 - 未返回发送状态");
                return false;
            }
            
        } catch (Exception e) {
            log.error("腾讯云 SDK 发送短信异常: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String getProviderName() {
        return "Tencent SMS";
    }
}
