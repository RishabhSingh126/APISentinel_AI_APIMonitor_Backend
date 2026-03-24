package com.portfolio.apimonitor.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_id", nullable = false)
    private ApiEndpoint apiEndpoint;

    @Column(name = "alert_message", columnDefinition = "TEXT")
    private String alertMessage;

    @Column(name = "suggested_fix", columnDefinition = "TEXT")
    private String suggestedFix;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // --- UNIQUE AI FEATURE ---
    @Column(name = "is_ai_generated_fix")
    private Boolean isAiGeneratedFix = false; // Did standard logic or the AI write this fix?
}