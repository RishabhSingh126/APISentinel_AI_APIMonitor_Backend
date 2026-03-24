package com.portfolio.apimonitor.service;

import com.portfolio.apimonitor.model.ApiEndpoint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@Slf4j
public class PingService {

    private final RestClient restClient;

    public PingService() {
        this.restClient = RestClient.create();
    }

    @Data
    @AllArgsConstructor
    public static class PingResult {
        private int statusCode;
        private long latencyMs;
        private boolean keywordMatched;
        private String errorMessage;
    }

    // FEATURE 7: Keyword Assertion integrated into the ping
    public PingResult pingUrl(String url, String expectedKeyword) {
        Instant start = Instant.now();
        try {
            ResponseEntity<String> response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .toEntity(String.class);

            long latency = Duration.between(start, Instant.now()).toMillis();
            int statusCode = response.getStatusCode().value();

            boolean keywordMatched = validatePayload(response.getBody(), expectedKeyword);

            return new PingResult(statusCode, latency, keywordMatched, null);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            long latency = Duration.between(start, Instant.now()).toMillis();
            return new PingResult(e.getStatusCode().value(), latency, false, e.getMessage());
        } catch (Exception e) {
            long latency = Duration.between(start, Instant.now()).toMillis();
            return new PingResult(500, latency, false, "Connection Failed: " + e.getMessage());
        }
    }

    public boolean validatePayload(String responseBody, String expectedKeyword) {
        if (expectedKeyword == null || expectedKeyword.trim().isEmpty()) {
            return true;
        }
        if (responseBody == null) {
            return false;
        }
        return responseBody.contains(expectedKeyword);
    }

    // FEATURE 8: SSL Expiry Check returning the exact LocalDateTime
    public LocalDateTime checkSslExpiryDate(String urlString) {
        try {
            URL url = new URL(urlString);
            if (!url.getProtocol().equalsIgnoreCase("https")) {
                return null; // Not HTTPS
            }

            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.connect();
            Certificate[] certs = conn.getServerCertificates();

            if (certs.length > 0 && certs[0] instanceof X509Certificate x509) {
                Date expiryDate = x509.getNotAfter();
                return LocalDateTime.ofInstant(expiryDate.toInstant(), ZoneId.systemDefault());
            }
        } catch (Exception e) {
            log.error("Failed to check SSL for {}: {}", urlString, e.getMessage());
        }
        return null;
    }

    public PingResult handleRetry(ApiEndpoint api) {
        PingResult result = pingUrl(api.getUrl(), api.getExpectedKeyword());
        if (result.getStatusCode() >= 400 || !result.isKeywordMatched()) {
            log.warn("Ping failed for API: {}. Waiting 5 seconds to retry...", api.getUrl());
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            log.info("Retrying API: {}", api.getUrl());
            return pingUrl(api.getUrl(), api.getExpectedKeyword());
        }
        return result;
    }
}