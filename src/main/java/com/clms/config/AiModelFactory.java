package com.clms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.clms.service.IAiChatService;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;

@Configuration
@ConditionalOnProperty(name = "ai.enabled", havingValue = "true", matchIfMissing = true)
public class AiModelFactory {
    
    @Resource
    private ChatModel qWenChatModel;

    // 通过反射 + 代理模式动态生成 IAiChatService 实现类，底层调用 LangChain4j 进行对话交互
    @Bean
    public IAiChatService aiChatService() {
        return AiServices.create(IAiChatService.class, qWenChatModel);
    }

}
