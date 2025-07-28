package com.rmowlana.aimemorychat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rmowlana.aimemorychat.dto.ChatResponse;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class AIService {

    private final ChatClient.Builder builder;
    private final ReferenceContentService referenceContentService;
    private final ChatMemoryService chatMemoryService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ai.system.prompt.template}")
    private String systemPrompt;

    public AIService(ChatClient.Builder builder,
                     ReferenceContentService referenceContentService,
                     ChatMemoryService chatMemoryService) {
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

            // Combine system prompt and reference content
            String finalPrompt = systemPrompt + referenceContentService.getReferenceContent();

            // Get AI response content
            String rawContent = chatClient.prompt()
                    .system(finalPrompt)
                    .user(prompt)
                    .call()
                    .content();

            // Remove markdown bold "**" if present
            String plainContent = rawContent.replace("**", "");

            // Wrap it into JSON format: {"response": "..."}
            Map<String, String> jsonResponse = new HashMap<>();
            jsonResponse.put("response", plainContent);

            return objectMapper.writeValueAsString(jsonResponse);

        } catch (Exception e) {
            log.error("Error during chat processing", e);
            return "{\"response\": \"Sorry, I encountered an error processing your request.\"}";
        }
    }
}
