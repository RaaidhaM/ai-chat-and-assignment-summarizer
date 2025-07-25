package com.rmowlana.aimemorychat.service;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Responsible for memory management
@Service
public class ChatMemoryService {
    private final Map<String, ChatMemory> userMemories = new ConcurrentHashMap<>();

    public ChatMemory getOrCreateMemory(String userId) {
        return userMemories.computeIfAbsent(userId, id ->
                MessageWindowChatMemory.builder().maxMessages(20).build()
        );
    }

}
