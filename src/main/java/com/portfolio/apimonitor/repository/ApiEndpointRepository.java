package com.portfolio.apimonitor.repository;

import com.portfolio.apimonitor.model.ApiEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApiEndpointRepository extends JpaRepository<ApiEndpoint, Long> {

    // When a user logs in, this fetches ONLY their specific APIs (Multi-Tenancy feature)
    List<ApiEndpoint> findByUser_Id(Long userId);
}