package com.rmowlana.aimemorychat.controller;

import com.rmowlana.aimemorychat.dto.ChatRequest;
import com.rmowlana.aimemorychat.dto.ChatResponse;
import com.rmowlana.aimemorychat.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest chatRequest) {
        ChatResponse response =  chatService.chat(chatRequest.getPrompt(), chatRequest.getUserId());
        return ResponseEntity.ok(response);
    }
}
