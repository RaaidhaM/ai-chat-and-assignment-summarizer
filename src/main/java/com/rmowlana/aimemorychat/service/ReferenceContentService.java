package com.rmowlana.aimemorychat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;

// Responsible for content management
@Service
public class ReferenceContentService {
    @Getter
    private String referenceContent = "";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void loadReferenceContent(String filePath) throws IOException {
        if (!StringUtils.hasText(filePath)) {
            throw new IllegalArgumentException("File path cannot be empty");
        }

        File file = new File(filePath);
        Object jsonData = objectMapper.readValue(file, Object.class);
        // Convert JSON to a string representation
        this.referenceContent = objectMapper.writeValueAsString(jsonData);
    }

}
