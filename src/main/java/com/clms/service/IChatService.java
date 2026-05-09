package com.clms.service;

import java.util.List;

import com.clms.entity.bo.ChatMessageBO;
import com.clms.entity.bo.ChatReplyResult;
import com.clms.entity.bo.ChatSessionBO;

public interface IChatService {

    ChatReplyResult sendMessage(String userId, String sessionId, String message);

    List<ChatSessionBO> listSessions(String userId);

    List<ChatMessageBO> getSessionMessages(String userId, String sessionId);

    void deleteSession(String userId, String sessionId);
}
