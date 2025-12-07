package com.clms.listener;

import org.springframework.amqp.rabbit.annotation.QueueBinding;

import java.util.concurrent.TimeUnit;

import org.apache.commons.mail.SimpleEmail;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;


import com.clms.config.RabbitMQConfig;
import com.clms.entity.bo.VerificationCodeBO;
import com.clms.utils.RedisConstants;

import cn.hutool.core.util.RandomUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EmailListener {
    
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private Environment environment;

    // 检查是否是测试环境
    private boolean isTestEnvironment() {
        String[] activeProfiles = environment.getActiveProfiles();
        for (String profile : activeProfiles) {
            if ("test".equals(profile)) {
                return true;
            }
        }
        return false;
    }

    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = RabbitMQConfig.EMAIL_QUEUE, durable = "true"),
        exchange = @Exchange(value = RabbitMQConfig.EMAIL_EXCHANGE, type = "direct"),
        key = RabbitMQConfig.EMAIL_ROUTING_KEY
    ))
    public void sendEmailCodeListener(VerificationCodeBO message) {
        // 处理接收到的邮件消息
        log.info("Received email verification code message: {}", message);
        
        if (isTestEnvironment()) {
            log.info("测试环境，跳过真实发送邮件逻辑 - code: {}", message.getCode());
            return;
        }

        // 在这里添加发送邮件的逻辑
        try {
            String credential = message.getCredential();
            String code = message.getCode();
            
            log.info("准备发送邮箱验证码 - credential: {}", credential);
            
            // 调用发送邮箱验证码方法
            sendEmailCode(credential, code);
            
            log.info("邮箱发送成功 - credential: {}", credential);
        } catch (Exception e) {
            log.error("邮箱发送失败 - 发生异常: {}", e.getMessage());
            // 异常会导致消息被拒绝，自动转到死信队列
            throw new RuntimeException("Failed to send email to: " + message.getCredential());
        }
    }

    private void sendEmailCode(String qqEmailStr, String code) {
        
        SimpleEmail email = new SimpleEmail();
        try {
            // 启用 TLSv1.2
            System.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");
            // 设置 SMTP 服务器和端口
            email.setHostName("smtp.qq.com");
            email.setSmtpPort(465);
            email.setCharset("utf-8");
            // 设置收件人
            email.addTo(qqEmailStr);
            // 设置发件人
            email.setFrom("1606294640@qq.com", "讲座通");
            // 设置授权码
            email.setAuthentication("1606294640@qq.com", "annfkllnmmajbadh");
            // 启用 SSL
            email.setSSLOnConnect(true);
            // 设置邮件主题
            email.setSubject("邮箱验证");
            // 设置邮件内容
            email.setMsg("尊敬的用户:你好!\n 验证码为:" + code + "(有效期为五分钟)\n如果本封邮件非您本人触发，请忽略。");
            email.send();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("发送邮箱失败");
        }
    }
}
