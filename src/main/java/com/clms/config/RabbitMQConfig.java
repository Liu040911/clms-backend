package com.clms.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    // 邮箱验证码队列配置
    public static final String EMAIL_EXCHANGE = "email_exchange";
    public static final String EMAIL_QUEUE = "email_queue";
    public static final String EMAIL_ROUTING_KEY = "email_routing_key";
    // 手机验证码队列配置
    public static final String SMS_EXCHANGE = "sms_exchange";
    public static final String SMS_QUEUE = "sms_queue";
    public static final String SMS_ROUTING_KEY = "sms_routing_key";


    // 邮箱验证码死信队列配置
    public static final String EMAIL_DLX_EXCHANGE = "email_dlx_exchange";
    public static final String EMAIL_DLQ_QUEUE = "email_dlq_queue";
    public static final String EMAIL_DLQ_ROUTING_KEY = "email_dlq_routing_key";
    
    // 短信验证码死信队列配置
    public static final String SMS_DLX_EXCHANGE = "sms_dlx_exchange";
    public static final String SMS_DLQ_QUEUE = "sms_dlq_queue";
    public static final String SMS_DLQ_ROUTING_KEY = "sms_dlq_routing_key";


    // ==================== RabbitTemplate 和消息转换器 ====================
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setEncoding("UTF-8");
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setPrefetchCount(100); // Set prefetch limit for notification consumer
        return factory;
    }
}
