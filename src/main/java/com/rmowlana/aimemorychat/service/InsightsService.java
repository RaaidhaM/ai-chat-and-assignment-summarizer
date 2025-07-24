package com.rmowlana.aimemorychat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InsightsService {

    @Value("${spring.ai.azure.openai.endpoint}")
    private String apiBase;

    @Value("${spring.ai.azure.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.azure.openai.chat.options.deployment-name}")
    private String deploymentName;

    @Value("${spring.ai.azure.openai.version}")
    private String apiVersion;


    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String ASSESSMENT_API = "https://was-assessment-qa.aws.wiley.com/was-assessment-master/private/master/assessments/";
    private static final String STUDENT_PROGRESS_API = "mock"; // Will mock for now

    public Map<String, Object> generateInsights(String assessmentId) {
        try {
            // 1. Fetch assessment details
            HttpRequest assessmentRequest = HttpRequest.newBuilder()
                    .uri(URI.create(ASSESSMENT_API + assessmentId))
                    .GET()
                    .build();

            HttpResponse<String> assessmentResponse = client.send(assessmentRequest, HttpResponse.BodyHandlers.ofString());
            Map<String, Object> assessmentData = mapper.readValue(assessmentResponse.body(), Map.class);

            // 2. Mock Student Progress response
            String studentProgressJson = """
            {
                "usersAssessments": [
                    {
                        "firstName": "Student15332",
                        "lastName": "std",
                        "loginName": "student15332@yopmail.com",
                        "score": 0.1666666666666667,
                        "timeSpent": 3447879
                    },
                    {
                        "firstName": "student15261_1",
                        "lastName": "std",
                        "loginName": "student15261_1@yopmail.com",
                        "score": 0.1766666666666667,
                        "timeSpent": 0
                    },
                    {
                        "firstName": "student15261_1",
                        "lastName": "std",
                        "loginName": "student15261_1@yopmail.com",
                        "score": 0.6766666666666667,
                        "timeSpent": 0
                    },
                    {
                        "firstName": "student15261_1",
                        "lastName": "std",
                        "loginName": "student15261_1@yopmail.com",
                        "score": 0.9766666666666667,
                        "timeSpent": 0
                    }
                ],
                "totalUserAssessments": 4
            }
            """;
            Map<String, Object> studentProgressData = mapper.readValue(studentProgressJson, Map.class);

            // 3. Prepare strict GPT-4 prompt to force JSON response
            String prompt = """
            Analyze the following assessment and student progress data and return ONLY JSON in this exact format:
            {
              "most_challenging_questions": [{"question": "", "reason": ""}],
              "common_misconceptions": [{"pattern": "", "detail": ""}],
              "areas_of_class_strength": [{"area": "", "detail": ""}],
              "ai_suggested_next_steps": ["", ""]
            }

            Do not include any explanation, text, or markdown outside this JSON.

            Data:
            """ + mapper.writeValueAsString(assessmentData) + "\n"
                    + mapper.writeValueAsString(studentProgressData);

            // 4. Call Azure GPT-4 API
            String gptResponse = callAzureGPT4(prompt);

            // 5. Parse GPT-4 JSON response
            Map<String, Object> insightsMap;
            try {
                insightsMap = mapper.readValue(gptResponse, Map.class);
            } catch (Exception e) {
                insightsMap = Map.of("error", "Failed to parse GPT response", "rawResponse", gptResponse);
            }

            return insightsMap;

        } catch (Exception e) {
            throw new RuntimeException("Error generating insights", e);
        }
    }

    private String callAzureGPT4(String prompt) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("messages", List.of(
                Map.of("role", "system", "content", "You are an AI education analyst."),
                Map.of("role", "user", "content", prompt)
        ));
        payload.put("temperature", 0.3);
        payload.put("max_tokens", 700);

        String jsonPayload = mapper.writeValueAsString(payload);
        String url = apiBase + "openai/deployments/" + deploymentName + "/chat/completions?api-version=" + apiVersion;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Map<String, Object> result = mapper.readValue(response.body(), Map.class);
        Map<String, Object> choice = ((List<Map<String, Object>>) result.get("choices")).get(0);
        Map<String, String> message = (Map<String, String>) choice.get("message");

        return message.get("content");
    }

}
