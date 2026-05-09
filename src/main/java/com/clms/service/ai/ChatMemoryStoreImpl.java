package com.clms.service.ai;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.clms.entity.po.ChatMessageTable;
import com.clms.service.data.IChatMessageTableService;
import com.clms.utils.CommonUtil;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.annotation.Resource;

@Component
public class ChatMemoryStoreImpl implements ChatMemoryStore {

    @Resource
    private IChatMessageTableService chatMessageTableService;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        List<ChatMessageTable> rows = chatMessageTableService.lambdaQuery()
                .eq(ChatMessageTable::getSessionId, memoryId.toString())
                .orderByAsc(ChatMessageTable::getCreateTime)
                .list();
        return rows.stream()
                .map(row -> ChatMessageDeserializer.messageFromJson(row.getContent()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String sessionId = memoryId.toString();
        chatMessageTableService.lambdaUpdate()
                .eq(ChatMessageTable::getSessionId, sessionId)
                .remove();
        List<ChatMessageTable> rows = new ArrayList<>();
        for (ChatMessage msg : messages) {
            rows.add(toRow(msg, sessionId));
        }
        chatMessageTableService.saveBatch(rows);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        chatMessageTableService.lambdaUpdate()
                .eq(ChatMessageTable::getSessionId, memoryId.toString())
                .remove();
    }

    private ChatMessageTable toRow(ChatMessage msg, String sessionId) {
        ChatMessageTable row = new ChatMessageTable();
        row.setId(CommonUtil.generateUuidV7());
        row.setSessionId(sessionId);
        row.setCreateTime(LocalDateTime.now());
        row.setRole(msg.type().name());
        row.setContent(ChatMessageSerializer.messageToJson(msg));

        if (msg instanceof ToolExecutionResultMessage tm) {
            row.setToolName(tm.toolName());
            row.setToolCallId(tm.id());
        }

        return row;
    }
}
