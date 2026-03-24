package com.portfolio.apimonitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@Slf4j
public class AlertService {

    private final RestClient restClient;

    // We initialize Spring's modern RestClient to send HTTP requests to Discord
    public AlertService() {
        this.restClient = RestClient.create();
    }

    /**
     * Requirement: analyzeError(int statusCode) -> Returns suggested fixes.
     */
    public String analyzeError(int statusCode) {
        return switch (statusCode) {
            case 400 -> "Bad Request: Check the payload or URL parameters being sent to the API.";
            case 401 -> "Unauthorized: API Key or Token is missing/expired. Please update credentials.";
            case 403 -> "Forbidden: The provided credentials do not have permission to access this endpoint.";
            case 404 -> "Not Found: The API endpoint URL might have changed or is incorrect.";
            case 429 -> "Too Many Requests: Rate limit exceeded. Consider upgrading the API plan or slowing down pings.";
            case 500 -> "Internal Server Error: The target API server crashed. Check their status page.";
            case 502 -> "Bad Gateway: The target API's proxy/load balancer is failing.";
            case 503 -> "Service Unavailable: The target API is down for maintenance or overloaded.";
            case 504 -> "Gateway Timeout: The target API took too long to respond.";
            default -> "Unknown Error: Received HTTP " + statusCode + ". Manual investigation required.";
        };
    }

    /**
     * Requirement: sendDiscordWebhook(String url, String message) -> Fires off the alert.
     */
    public void sendDiscordWebhook(String webhookUrl, String message) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("Discord Webhook URL is missing. Skipping alert.");
            return;
        }

        try {
            // Discord webhooks expect a JSON payload in the format: {"content": "Your message here"}
            Map<String, String> body = Map.of("content", message);

            restClient.post()
                    .uri(webhookUrl)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity(); // We just send it and don't care about the response body

            log.info("Successfully fired Discord webhook alert!");
        } catch (Exception e) {
            log.error("Failed to send Discord webhook: {}", e.getMessage());
        }
    }
}