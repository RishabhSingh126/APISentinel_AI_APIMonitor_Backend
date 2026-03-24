//package com.portfolio.apimonitor.scheduler;
//
//import com.portfolio.apimonitor.model.ApiEndpoint;
//import com.portfolio.apimonitor.model.ApiLog;
//import com.portfolio.apimonitor.repository.ApiEndpointRepository;
//import com.portfolio.apimonitor.repository.ApiLogRepository;
//import com.portfolio.apimonitor.service.AlertService;
//import com.portfolio.apimonitor.service.PingService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class MonitoringScheduler {
//
//    private final ApiEndpointRepository apiEndpointRepository;
//    private final ApiLogRepository apiLogRepository;
//    private final PingService pingService;
//    private final AlertService alertService;
//
//    @Scheduled(cron = "0 * * * * *")
//    public void runHealthChecks() {
//        log.info("⏰ Starting automated 1-minute health checks...");
//
//        List<ApiEndpoint> endpoints = apiEndpointRepository.findAll();
//
//        if (endpoints.isEmpty()) {
//            return;
//        }
//
//        for (ApiEndpoint api : endpoints) {
//
//            // FEATURE 8: Extract and save the SSL Expiry Date
//            if (api.getUrl().startsWith("https")) {
//                LocalDateTime sslExpiry = pingService.checkSslExpiryDate(api.getUrl());
//                if (sslExpiry != null) {
//                    api.setSslExpiresAt(sslExpiry);
//                    apiEndpointRepository.save(api);
//                }
//            }
//
//            PingService.PingResult result = pingService.handleRetry(api);
//
//            ApiLog apiLog = new ApiLog();
//            apiLog.setApiEndpoint(api);
//            apiLog.setStatusCode(result.getStatusCode());
//            apiLog.setLatencyMs((int) result.getLatencyMs());
//            apiLog.setCreatedAt(LocalDateTime.now());
//            apiLog.setAnomalyDetected(false); // Default
//
//            boolean isSuccess = (result.getStatusCode() >= 200 && result.getStatusCode() < 300) && result.isKeywordMatched();
//
//            if (!result.isKeywordMatched() && isSuccess) {
//                isSuccess = false;
//                apiLog.setErrorMessage("Keyword Assertion Failed: Keyword not found in response.");
//            }
//
//            // FEATURE 9: Anomaly Detection (Spike > 300% of average)
//            List<ApiLog> lastLogs = apiLogRepository.findTop10ByApiEndpointOrderByCreatedAtDesc(api);
//            if (lastLogs.size() >= 5) {
//                double avgLatency = lastLogs.stream().mapToInt(ApiLog::getLatencyMs).average().orElse(0.0);
//
//                if (result.getLatencyMs() > (avgLatency * 3) && avgLatency > 0) {
//                    apiLog.setAnomalyDetected(true);
//                    isSuccess = false; // Blueprint requirement: Fail the ping
//                    apiLog.setErrorMessage("Latency Anomaly: " + result.getLatencyMs() + "ms (Avg: " + Math.round(avgLatency) + "ms)");
//                    log.warn("🚨 ANOMALY DETECTED for {}: {}ms", api.getUrl(), result.getLatencyMs());
//                }
//            }
//
//            apiLog.setIsSuccess(isSuccess);
//
//            if (result.getErrorMessage() != null && apiLog.getErrorMessage() == null) {
//                apiLog.setErrorMessage(result.getErrorMessage());
//            }
//
//            apiLogRepository.save(apiLog);
//
//            if (!isSuccess) {
//                String advice = alertService.analyzeError(result.getStatusCode());
//                String alertMessage = String.format("🚨 **API DOWN/ANOMALY** 🚨\n**URL:** %s\n**Status:** %d\n**Fix:** %s",
//                        api.getUrl(), result.getStatusCode(), advice);
//
//                alertService.sendDiscordWebhook(null, alertMessage);
//            }
//        }
//        log.info("🏁 Finished health checks.");
//    }
//}





package com.portfolio.apimonitor.scheduler;

import com.portfolio.apimonitor.model.ApiEndpoint;
import com.portfolio.apimonitor.model.ApiLog;
import com.portfolio.apimonitor.repository.ApiEndpointRepository;
import com.portfolio.apimonitor.repository.ApiLogRepository;
import com.portfolio.apimonitor.service.AlertService;
import com.portfolio.apimonitor.service.PingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MonitoringScheduler {

    private final ApiEndpointRepository apiEndpointRepository;
    private final ApiLogRepository apiLogRepository;
    private final PingService pingService;
    private final AlertService alertService;

    @Scheduled(cron = "0 * * * * *")
    public void runHealthChecks() {
        log.info("⏰ Starting automated 1-minute health checks...");

        List<ApiEndpoint> endpoints = apiEndpointRepository.findAll();

        if (endpoints.isEmpty()) {
            return;
        }

        for (ApiEndpoint api : endpoints) {

            // FEATURE 8: Extract and save the SSL Expiry Date
            if (api.getUrl().startsWith("https")) {
                LocalDateTime sslExpiry = pingService.checkSslExpiryDate(api.getUrl());
                if (sslExpiry != null) {
                    api.setSslExpiresAt(sslExpiry);
                    apiEndpointRepository.save(api);
                }
            }

            PingService.PingResult result = pingService.handleRetry(api);

            ApiLog apiLog = new ApiLog();
            apiLog.setApiEndpoint(api);
            apiLog.setStatusCode(result.getStatusCode());
            apiLog.setLatencyMs((int) result.getLatencyMs());
            apiLog.setCreatedAt(LocalDateTime.now());
            apiLog.setAnomalyDetected(false); // Default

            // 1. Check HTTP Status first
            boolean isSuccess = (result.getStatusCode() >= 200 && result.getStatusCode() < 300);

            // 2. THE FIX: Override success if the keyword was expected but NOT matched
            if (isSuccess && !result.isKeywordMatched()) {
                isSuccess = false;
                apiLog.setErrorMessage("Keyword Assertion Failed: Keyword not found in response.");
                log.warn("❌ Keyword validation failed for {}", api.getUrl());
            }

            // FEATURE 9: Anomaly Detection (Spike > 300% of average)
            List<ApiLog> lastLogs = apiLogRepository.findTop10ByApiEndpointOrderByCreatedAtDesc(api);
            if (lastLogs.size() >= 5) {
                double avgLatency = lastLogs.stream().mapToInt(ApiLog::getLatencyMs).average().orElse(0.0);

                if (result.getLatencyMs() > (avgLatency * 3) && avgLatency > 0) {
                    apiLog.setAnomalyDetected(true);
                    isSuccess = false; // Blueprint requirement: Fail the ping
                    apiLog.setErrorMessage("Latency Anomaly: " + result.getLatencyMs() + "ms (Avg: " + Math.round(avgLatency) + "ms)");
                    log.warn("🚨 ANOMALY DETECTED for {}: {}ms", api.getUrl(), result.getLatencyMs());
                }
            }

            apiLog.setIsSuccess(isSuccess);

            if (result.getErrorMessage() != null && apiLog.getErrorMessage() == null) {
                apiLog.setErrorMessage(result.getErrorMessage());
            }

            apiLogRepository.save(apiLog);

            // 3. THE FIX: Actually update the API status so the Dashboard sees it!
            api.setStatus(isSuccess ? "Online" : "Error");
            apiEndpointRepository.save(api);

            if (!isSuccess) {
                String advice = alertService.analyzeError(result.getStatusCode());
                String alertMessage = String.format("🚨 **API DOWN/ANOMALY** 🚨\n**URL:** %s\n**Status:** %d\n**Fix:** %s",
                        api.getUrl(), result.getStatusCode(), advice);

                alertService.sendDiscordWebhook(null, alertMessage);
            }
        }
        log.info("🏁 Finished health checks.");
    }
}