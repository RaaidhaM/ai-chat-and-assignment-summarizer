package com.rmowlana.aimemorychat.dto;

import lombok.Data;

@Data
public class ChatRequest {
    private String prompt;
    private String userId;
}
