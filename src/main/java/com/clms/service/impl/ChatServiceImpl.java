package com.clms.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clms.entity.bo.ChatMessageBO;
import com.clms.entity.bo.ChatReplyResult;
import com.clms.entity.bo.ChatSessionBO;
import com.clms.entity.po.ChatMessageTable;
import com.clms.entity.po.ChatSessionTable;
import com.clms.exception.BusinessException;
import com.clms.service.IChatService;
import com.clms.service.ai.CampusLectureAgent;
import com.clms.service.data.IChatMessageTableService;
import com.clms.service.data.IChatSessionTableService;
import com.clms.utils.CommonUtil;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ChatServiceImpl implements IChatService {

    private static final int MAX_TITLE_LENGTH = 50;

    @Resource
    private IChatSessionTableService chatSessionTableService;

    @Resource
    private IChatMessageTableService chatMessageTableService;

    @Resource
    private CampusLectureAgent campusLectureAgent;

    @Override
    @Transactional
    public ChatReplyResult sendMessage(String userId, String sessionId, String message) {
        String effectiveSessionId = sessionId;
        if (StrUtil.isBlank(effectiveSessionId)) {
            effectiveSessionId = createNewSession(userId, message);
        } else {
            verifySessionOwnership(userId, effectiveSessionId);
        }

        String reply;
        try {
            reply = campusLectureAgent.chat(effectiveSessionId, message);
        } catch (Exception e) {
            log.error("AI chat failed for session={}, userId={}: {}", effectiveSessionId, userId, e.getMessage(), e);
            throw new BusinessException(500, "AI 服务暂时不可用，请稍后重试");
        }

        chatSessionTableService.lambdaUpdate()
                .eq(ChatSessionTable::getId, effectiveSessionId)
                .set(ChatSessionTable::getUpdateTime, LocalDateTime.now())
                .update();

        return new ChatReplyResult(effectiveSessionId, reply);
    }

    @Override
    public List<ChatSessionBO> listSessions(String userId) {
        List<ChatSessionTable> sessions = chatSessionTableService.lambdaQuery()
                .eq(ChatSessionTable::getUserId, userId)
                .orderByDesc(ChatSessionTable::getUpdateTime)
                .list();
        return sessions.stream().map(s -> {
            ChatSessionBO bo = new ChatSessionBO();
            bo.setId(s.getId());
            bo.setTitle(s.getTitle());
            bo.setCreateTime(s.getCreateTime());
            bo.setUpdateTime(s.getUpdateTime());
            return bo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ChatMessageBO> getSessionMessages(String userId, String sessionId) {
        verifySessionOwnership(userId, sessionId);
        List<ChatMessageTable> messages = chatMessageTableService.lambdaQuery()
                .eq(ChatMessageTable::getSessionId, sessionId)
                .in(ChatMessageTable::getRole, List.of("USER", "AI"))
                .orderByAsc(ChatMessageTable::getCreateTime)
                .list();
        return messages.stream().map(m -> new ChatMessageBO(
                m.getRole().equals("AI") ? "assistant" : "user",
                extractTextContent(m),
                m.getCreateTime()
        )).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteSession(String userId, String sessionId) {
        verifySessionOwnership(userId, sessionId);
        chatSessionTableService.removeById(sessionId);
        chatMessageTableService.lambdaUpdate()
                .eq(ChatMessageTable::getSessionId, sessionId)
                .remove();
    }

    private String createNewSession(String userId, String firstMessage) {
        ChatSessionTable session = new ChatSessionTable();
        session.setId(CommonUtil.generateUuidV7());
        session.setUserId(userId);
        String title = StrUtil.cleanBlank(firstMessage);
        if (title.length() > MAX_TITLE_LENGTH) {
            title = title.substring(0, MAX_TITLE_LENGTH);
        }
        session.setTitle(title);
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());

        boolean saved = chatSessionTableService.save(session);
        if (!saved) {
            throw new BusinessException(500, "创建聊天会话失败");
        }
        return session.getId();
    }

    private void verifySessionOwnership(String userId, String sessionId) {
        ChatSessionTable session = chatSessionTableService.getById(sessionId);
        if (session == null) {
            throw new BusinessException(404, "聊天会话不存在");
        }
        if (!userId.equals(session.getUserId())) {
            throw new BusinessException(403, "无权访问该聊天会话");
        }
    }

    private String extractTextContent(ChatMessageTable msg) {
        if (msg.getContent() == null || msg.getContent().isBlank()) {
            return "";
        }
        try {
            cn.hutool.json.JSONObject json = cn.hutool.json.JSONUtil.parseObj(msg.getContent());
            // AI/SYSTEM/TOOL_EXECUTION_RESULT: text is at top-level "text"
            String text = json.getStr("text");
            if (text != null && !text.isBlank()) {
                return text;
            }
            // USER: text is inside contents[0].text
            if (json.containsKey("contents")) {
                var contents = json.getJSONArray("contents");
                if (contents != null && !contents.isEmpty()) {
                    var first = (cn.hutool.json.JSONObject) contents.get(0);
                    return first.getStr("text", "");
                }
            }
            return "";
        } catch (Exception e) {
            return msg.getContent();
        }
    }
}
