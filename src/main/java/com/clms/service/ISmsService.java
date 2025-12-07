package com.clms.service;

/**
 * 短信发送服务接口
 * 
 * 定义短信发送的通用接口，支持多种短信服务提供商的实现
 * 
 * @author System
 */
public interface ISmsService {
    
    /**
     * 发送短信验证码
     * 
     * @param phone 手机号码
     * @param code 验证码
     * @throws Exception 当短信发送失败时抛出异常
     */
    void sendSms(String phone, String code) throws Exception;
    
    /**
     * 获取服务提供商名称
     * 
     * @return 服务提供商名称
     */
    String getProviderName();
}
