package com.portfolio.apimonitor.controller;

import com.portfolio.apimonitor.model.ApiEndpoint;
import com.portfolio.apimonitor.model.ApiLog;
import com.portfolio.apimonitor.model.User;
import com.portfolio.apimonitor.repository.ApiEndpointRepository;
import com.portfolio.apimonitor.repository.ApiLogRepository;
import com.portfolio.apimonitor.repository.UserRepository;
import com.portfolio.apimonitor.service.CostService;
import com.portfolio.apimonitor.service.AiService;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final ApiEndpointRepository apiEndpointRepository;
    private final ApiLogRepository apiLogRepository;
    private final UserRepository userRepository;
    private final CostService costService;
    private final AiService aiService;

    @Data
    @Builder
    public static class MetricsResponse {
        private long totalVolume;
        private BigDecimal estimatedCost;
        private List<LatencyPoint> latencyGraph;

        // FEATURE 12: Pagination Metadata for React
        private int currentPage;
        private int totalPages;
        private boolean isLast;
    }

    @Data
    @Builder
    public static class LatencyPoint {
        private String time;
        private int latencyMs;
    }

    @GetMapping("/{apiId}")
    public ResponseEntity<MetricsResponse> getMetrics(
            @PathVariable Long apiId,
            @RequestParam(defaultValue = "0") int page, // Captures ?page= from URL
            @RequestParam(defaultValue = "50") int size, // Captures ?size= from URL
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        ApiEndpoint api = apiEndpointRepository.findById(apiId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "API not found"));

        if (!api.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        }

        long totalVolume = apiLogRepository.countByApiEndpoint(api);
        BigDecimal costRate = api.getCostPer1000Requests() != null ? api.getCostPer1000Requests() : BigDecimal.ZERO;
        BigDecimal estimatedCost = costService.calculateMonthlyCost(totalVolume, costRate);

        // FEATURE 12: Fetch data using Pageable
        Pageable pageable = PageRequest.of(page, size);
        Page<ApiLog> logPage = apiLogRepository.findByApiEndpointOrderByCreatedAtDesc(api, pageable);

        // Convert Page content to mutable list so we can reverse it for the chart
        List<ApiLog> pageLogs = new ArrayList<>(logPage.getContent());
        Collections.reverse(pageLogs);

        List<LatencyPoint> graphData = pageLogs.stream()
                .map(log -> LatencyPoint.builder()
                        .time(log.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                        .latencyMs(log.getLatencyMs() != null ? log.getLatencyMs() : 0)
                        .build())
                .collect(Collectors.toList());

        MetricsResponse response = MetricsResponse.builder()
                .totalVolume(totalVolume)
                .estimatedCost(estimatedCost)
                .latencyGraph(graphData)
                .currentPage(logPage.getNumber())
                .totalPages(logPage.getTotalPages())
                .isLast(logPage.isLast())
                .build();

        return ResponseEntity.ok(response);
    }

//    @GetMapping("/{apiId}/summary")
//    public ResponseEntity<Map<String, String>> getAiSummary(@PathVariable Long apiId, Authentication authentication) {
//        User user = userRepository.findByEmail(authentication.getName())
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
//
//        ApiEndpoint api = apiEndpointRepository.findById(apiId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "API not found"));
//
//        if (!api.getUser().getId().equals(user.getId())) {
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
//        }
//
//        List<ApiLog> recentLogs = apiLogRepository.findTop50ByApiEndpointOrderByCreatedAtDesc(api);
//        String summary = aiService.generateHealthSummary(recentLogs);
//
//        return ResponseEntity.ok(Map.of("summary", summary));
//    }

    // Replace this method at the bottom of MetricsController.java
    @GetMapping("/{apiId}/summary")
    public ResponseEntity<Map<String, String>> getAiSummary(
            @PathVariable Long apiId,
            @RequestHeader(value = "X-Gemini-Key", required = false) String geminiKey, // 👈 NEW: Catch the key from React
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        ApiEndpoint api = apiEndpointRepository.findById(apiId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "API not found"));

        if (!api.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        }

        List<ApiLog> recentLogs = apiLogRepository.findTop50ByApiEndpointOrderByCreatedAtDesc(api);

        // 👈 NEW: Pass the key to the AI Service
        String summary = aiService.generateHealthSummary(recentLogs, geminiKey);

        return ResponseEntity.ok(Map.of("summary", summary));
    }
}