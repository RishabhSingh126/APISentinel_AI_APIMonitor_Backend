package com.portfolio.apimonitor.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "apis")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiEndpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String url;

    @Column(name = "expected_keyword")
    private String expectedKeyword;

    @Column(name = "cost_per_1000_requests", precision = 10, scale = 4)
    private BigDecimal costPer1000Requests;

    @Column(nullable = false)
    private String status = "UP"; // 'UP', 'DOWN', 'DEGRADED'

    @Column(name = "ssl_expires_at")
    private LocalDateTime sslExpiresAt;

    // --- UNIQUE AI FEATURES ---
    @Column(name = "health_score")
    private Double healthScore;

    @Column(name = "ai_health_summary", columnDefinition = "TEXT")
    private String aiHealthSummary;

    // --- NEW: CASCADE DELETE RULE ---
    // This tells SQL: "If this API is deleted, delete all associated logs too!"
    @OneToMany(mappedBy = "apiEndpoint", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApiLog> logs;
}