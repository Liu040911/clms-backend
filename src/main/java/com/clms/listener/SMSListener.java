package com.clms.listener;

import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.clms.config.RabbitMQConfig;
import com.clms.entity.bo.VerificationCodeBO;
import com.clms.service.ISmsService;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SMSListener {

    @Resource
    private ISmsService smsService;

    @Resource
    private Environment environment;

    // 检查是否是测试环境
    private boolean isTestEnvironment() {
        String[] activeProfiles = environment.getActiveProfiles();
        for (String profile : activeProfiles) {
            if ("dev".equals(profile)) {
                return true;
            }
        }
        return false;
    }
    
    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = RabbitMQConfig.SMS_QUEUE, durable = "true"),
        exchange = @Exchange(value = RabbitMQConfig.SMS_EXCHANGE, type = "direct"),
        key = RabbitMQConfig.SMS_ROUTING_KEY
    ))
    public void sendSmsCode(VerificationCodeBO message) {
        // 处理接收到的短信消息
        log.info("Received SMS verification code message: {}", message);
        if (isTestEnvironment()) {
            log.info("测试环境，跳过真实发送短信逻辑 - code: {}", message.getCode());
            return;
        }
        try {
            String credential = message.getCredential();
            String code = message.getCode();
            
            log.info("准备发送短信验证码 - credential: {}", credential);
            
            // 调用短信服务发送验证码
            smsService.sendSms(credential, code);
            
            log.info("短信发送成功 - credential: {}", credential);
        } catch (Exception e) {
            log.error("短信发送失败 - credential: {}", message.getCredential(), e);
            // 异常会导致消息被拒绝，自动转到死信队列
            throw new RuntimeException("Failed to send SMS to: " + message.getCredential(), e);
        }
    }
}
