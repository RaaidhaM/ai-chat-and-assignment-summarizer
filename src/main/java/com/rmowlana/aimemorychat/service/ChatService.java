package com.rmowlana.aimemorychat.service;

import org.springframework.stereotype.Service;

@Service
public class ChatService {
    private final AIService aiService;

    public ChatService(AIService aiService) {
        this.aiService = aiService;
    }

    public String chat(String prompt, String userId) {
        return aiService.chat(prompt, userId);
    }
}
