package com.clms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
    @ConditionalOnProperty(name = "ai.enabled", havingValue = "true")
    @ConditionalOnBean(ChatModel.class)
    public IAiChatService aiChatService(ChatModel qWenChatModel) {
        return AiServices.create(IAiChatService.class, qWenChatModel);
    }

    // 当测试环境或本地未启用 AI 时提供兜底实现，避免 ApplicationContext 启动失败。
    @Bean
    @ConditionalOnMissingBean(IAiChatService.class)
    public IAiChatService fallbackAiChatService() {
        return userMessage -> "";
    }

}
