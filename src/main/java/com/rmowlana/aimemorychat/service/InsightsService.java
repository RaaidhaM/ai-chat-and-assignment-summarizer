package com.rmowlana.aimemorychat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
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
            // 1. Fetch Assessment details
            HttpRequest assessmentRequest = HttpRequest.newBuilder()
                    .uri(URI.create(ASSESSMENT_API + assessmentId))
                    .GET()
                    .build();

            HttpResponse<String> assessmentResponse = client.send(assessmentRequest, HttpResponse.BodyHandlers.ofString());
            Map<String, Object> assessmentData = mapper.readValue(assessmentResponse.body(), Map.class);

            // Extract itemCardIds
            List<Map<String, Object>> items = (List<Map<String, Object>>) assessmentData.get("items");

            // 2. Fetch QuestionCard details for each itemCardId
            List<Map<String, Object>> questionDetailsList = new ArrayList<>();
            for (Map<String, Object> item : items) {
                String itemCardId = (String) item.get("itemCardId");
                String questionUrl = "https://was-app-qa.aws.wiley.com/was-questioncard/private/questioncards/" + itemCardId;

                HttpRequest questionRequest = HttpRequest.newBuilder()
                        .uri(URI.create(questionUrl))
                        .GET()
                        .build();

                HttpResponse<String> questionResponse = client.send(questionRequest, HttpResponse.BodyHandlers.ofString());
                Map<String, Object> questionData = mapper.readValue(questionResponse.body(), Map.class);

                questionDetailsList.add(Map.of(
                        "id", questionData.get("id"),
                        "title", questionData.get("title"),
                        "difficulty", questionData.get("difficulty"),
                        "intent", questionData.get("intent")
                ));
            }

            // 3. Mock Student Progress (replace later with real API)
            String studentProgressJson = """
                    {
                        "usersAssessments": [
                            {"firstName": "Student15332","lastName":"std","loginName":"student15332@yopmail.com","score":0.1666,"timeSpent":3447879},
                            {"firstName": "student15261_1","lastName":"std","loginName":"student15261_1@yopmail.com","score":0.0,"timeSpent":0}
                        ],
                        "totalUserAssessments": 2
                    }
                    """;
            Map<String, Object> studentProgressData = mapper.readValue(studentProgressJson, Map.class);

            // 4. Prepare Enhanced Prompt for GPT-4
            String prompt = """
                    Analyze the following data and return ONLY JSON in this format:
                    {
                      "most_challenging_questions": [{"question": "", "reason": ""}],
                      "common_misconceptions": [{"pattern": "", "detail": ""}],
                      "areas_of_class_strength": [{"area": "", "detail": ""}],
                      "ai_suggested_next_steps": ["", ""]
                    }

                    Data:
                    Assessment Info: """ + mapper.writeValueAsString(assessmentData) + "\n"
                    + "Question Details: " + mapper.writeValueAsString(questionDetailsList) + "\n"
                    + "Student Progress: " + mapper.writeValueAsString(studentProgressData);

            // 5. Call GPT-4
            String gptResponse = callAzureGPT4(prompt);

            // 6. Parse GPT JSON
            Map<String, Object> insightsMap;
            try {
                String cleanJson = gptResponse
                        .replace("```json", "")
                        .replace("```", "")
                        .trim();
                insightsMap = mapper.readValue(cleanJson, Map.class);
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
