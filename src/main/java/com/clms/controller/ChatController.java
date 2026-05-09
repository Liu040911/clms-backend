package com.clms.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.clms.entity.base.ResponseEntity;
import com.clms.entity.bo.ChatMessageBO;
import com.clms.entity.bo.ChatReplyResult;
import com.clms.entity.bo.ChatSessionBO;
import com.clms.entity.dto.ChatSendRequest;
import com.clms.service.IChatService;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@Tag(name = "AI 聊天接口")
@RequestMapping("/chat")
@SaCheckLogin
@Validated
public class ChatController {

    @Resource
    private IChatService chatService;

    @Operation(summary = "发送消息")
    @PostMapping("/send")
    public ResponseEntity<ChatReplyResult> sendMessage(@RequestBody @Valid ChatSendRequest request) {
        String userId = (String) StpUtil.getTokenInfo().getLoginId();
        ChatReplyResult result = chatService.sendMessage(userId, request.getSessionId(), request.getMessage());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "获取会话列表")
    @GetMapping("/sessions")
    public ResponseEntity<List<ChatSessionBO>> listSessions() {
        String userId = (String) StpUtil.getTokenInfo().getLoginId();
        return ResponseEntity.ok(chatService.listSessions(userId));
    }

    @Operation(summary = "获取会话消息")
    @GetMapping("/sessions/messages")
    public ResponseEntity<List<ChatMessageBO>> getMessages(@RequestParam @NotBlank String sessionId) {
        String userId = (String) StpUtil.getTokenInfo().getLoginId();
        return ResponseEntity.ok(chatService.getSessionMessages(userId, sessionId));
    }

    @Operation(summary = "删除会话")
    @PostMapping("/sessions/delete")
    public ResponseEntity<Void> deleteSession(@RequestParam @NotBlank String sessionId) {
        String userId = (String) StpUtil.getTokenInfo().getLoginId();
        chatService.deleteSession(userId, sessionId);
        return ResponseEntity.ok();
    }
}
