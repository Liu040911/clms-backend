package com.clms.service.ai;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;

@Configuration
public class LangChain4jConfig {

    private static final int MAX_MESSAGES = 20;
    private static final int MAX_SEQUENTIAL_TOOLS_INVOCATIONS = 5;

    @Bean
    public ChatMemoryProvider chatMemoryProvider(ChatMemoryStore chatMemoryStore) {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .chatMemoryStore(chatMemoryStore)
                .maxMessages(MAX_MESSAGES)
                .build();
    }

    @Bean
    public CampusLectureAgent campusLectureAgent(
            ChatModel chatModel,
            ChatMemoryProvider chatMemoryProvider,
            LectureTools lectureTools) {
        return AiServices.builder(CampusLectureAgent.class)
                .chatModel(chatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .tools(lectureTools)
                .maxSequentialToolsInvocations(MAX_SEQUENTIAL_TOOLS_INVOCATIONS)
                .build();
    }
}
