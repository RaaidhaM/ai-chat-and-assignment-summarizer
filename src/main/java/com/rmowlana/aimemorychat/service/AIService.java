package com.rmowlana.aimemorychat.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rmowlana.aimemorychat.dto.ChatResponse;

@Service
@Slf4j
public class AIService {
    private final ChatClient.Builder builder;
    private final ReferenceContentService referenceContentService;
    private final ChatMemoryService chatMemoryService;

    @Value("${ai.system.prompt.template}")
    private String systemPrompt;

    public AIService(ChatClient.Builder builder, ReferenceContentService referenceContentService, ChatMemoryService chatMemoryService) {
        this.builder = builder;
        this.referenceContentService = referenceContentService;
        this.chatMemoryService = chatMemoryService;
    }

    public String chat(String prompt, String userId) {
        try {
            ChatMemory memory = chatMemoryService.getOrCreateMemory(userId);
            ChatClient chatClient = builder
                    .defaultAdvisors(MessageChatMemoryAdvisor.builder(memory).build())
                    .build();

            // Create system message with instructions to only use reference content
            String finalPrompt = systemPrompt + referenceContentService.getReferenceContent();

            // Use call with system message and user prompt
            return chatClient.prompt()
                    .system(finalPrompt)
                    .user(prompt)
                    .call()
                    .content();
//                    .replaceAll("\\r?\\n", "");

        }catch (Exception e) {
            log.error("Error during chat processing", e);
            return "Sorry, I encountered an error processing your request.";
        }
    }
}
