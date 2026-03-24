package com.portfolio.apimonitor.service;

import com.portfolio.apimonitor.model.ApiLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AiService {

    private final RestClient restClient;

    // Pulls your API key from application.properties securely (Fallback)
    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    public AiService() {
        this.restClient = RestClient.create();
    }

    /**
     * Requirement: generateHealthSummary(List<ApiLog>) -> Uses Gemini LLM to analyze logs.
     * UPDATED: Now accepts the clientKey passed from the React frontend!
     */
    public String generateHealthSummary(List<ApiLog> recentLogs, String clientKey) {

        // 👈 NEW: Use the React key if it exists, otherwise fall back to the properties file
        String effectiveKey = (clientKey != null && !clientKey.trim().isEmpty()) ? clientKey : geminiApiKey;

        if (effectiveKey == null || effectiveKey.trim().isEmpty()) {
            return "AI analysis is currently disabled. Please add a Gemini API key to your settings.";
        }
        if (recentLogs == null || recentLogs.isEmpty()) {
            return "Not enough ping data to generate a meaningful AI summary.";
        }

        // 1. Flatten the data for the AI to read easily
        String logData = recentLogs.stream()
                .map(log -> "Status:" + log.getStatusCode() + " Latency:" + log.getLatencyMs() + "ms")
                .collect(Collectors.joining(" | "));

        String prompt = "You are a highly skilled DevOps AI. Analyze these API ping logs and provide a short, 2-sentence human-readable summary of the API's health and stability. Do not use markdown. Logs: " + logData;

        // 2. Call the Gemini AI API
        try {
            // 👈 NEW: Use the effectiveKey in the URL
            String geminiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + effectiveKey;

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt)
                            ))
                    )
            );

            String response = restClient.post()
                    .uri(geminiUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            // 3. Extract the text from Gemini's JSON response
            if (response != null && response.contains("\"text\":")) {
                return response.split("\"text\": \"")[1].split("\"")[0].replace("\\n", " ");
            }
            return "AI successfully analyzed the data, but formatting failed.";

        } catch (Exception e) {
            log.error("AI Generation failed: {}", e.getMessage());
            return "Failed to generate AI summary due to an external service error.";
        }
    }
}