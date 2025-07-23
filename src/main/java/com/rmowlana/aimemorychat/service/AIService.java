package com.rmowlana.aimemorychat.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AIService {
    private final ChatClient.Builder builder;
    private final Map<String, ChatMemory> userMemories = new ConcurrentHashMap<>();

    public AIService(ChatClient.Builder builder) {
        this.builder = builder;
    }

    public ChatMemory getOrCreateMemory(String userId) {
        return userMemories.computeIfAbsent(userId, id ->
                MessageWindowChatMemory.builder().maxMessages(20).build()
        );
    }

    public String chat(String prompt, String userId) {
        ChatMemory memory = getOrCreateMemory(userId);
        ChatClient chatClient = builder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(memory).build())
                .build();
        return chatClient.prompt(prompt).call().content();
    }
}
