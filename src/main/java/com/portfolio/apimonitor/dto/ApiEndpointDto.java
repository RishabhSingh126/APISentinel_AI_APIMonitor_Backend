package com.portfolio.apimonitor.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ApiEndpointDto {
    private Long id;
    private String name;
    private String url;
    private String expectedKeyword;
    private BigDecimal costPer1000Requests;
    private String status;
    private LocalDateTime sslExpiresAt;

    // AI features included for later!
    private Double healthScore;
    private String aiHealthSummary;
}