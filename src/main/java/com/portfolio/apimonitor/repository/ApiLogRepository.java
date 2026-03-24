package com.portfolio.apimonitor.repository;

import com.portfolio.apimonitor.model.ApiEndpoint;
import com.portfolio.apimonitor.model.ApiLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApiLogRepository extends JpaRepository<ApiLog, Long> {

    @Query("SELECT AVG(a.latencyMs) FROM ApiLog a WHERE a.apiEndpoint.id = :apiId AND a.isSuccess = true")
    Double calculateAverageLatency(@Param("apiId") Long apiId);

    @Query("SELECT COUNT(a) FROM ApiLog a WHERE a.apiEndpoint.id = :apiId")
    Long countTotalRequests(@Param("apiId") Long apiId);

    @Query("SELECT a FROM ApiLog a WHERE a.apiEndpoint.id = :apiId AND a.anomalyDetected = true ORDER BY a.createdAt DESC LIMIT 5")
    List<ApiLog> findRecentAnomalies(@Param("apiId") Long apiId);

    long countByApiEndpoint(ApiEndpoint apiEndpoint);

    // Kept for the AI Summary generation (we always want the last 50 for the AI)
    List<ApiLog> findTop50ByApiEndpointOrderByCreatedAtDesc(ApiEndpoint apiEndpoint);

    List<ApiLog> findTop10ByApiEndpointOrderByCreatedAtDesc(ApiEndpoint apiEndpoint);

    // FEATURE 12: Pagination Method! Returns a Page object instead of a List
    Page<ApiLog> findByApiEndpointOrderByCreatedAtDesc(ApiEndpoint apiEndpoint, Pageable pageable);
}