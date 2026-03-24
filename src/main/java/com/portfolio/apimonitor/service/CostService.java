package com.portfolio.apimonitor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j // This gives us the "log" object for printing to the console
public class CostService {

    /**
     * Calculates the estimated monthly cost based on volume and a rate per 1,000 requests.
     * * @param totalRequests The total volume of requests for the month.
     * @param costPer1000Requests The specific cost rate per 1,000 requests (e.g., $1.50).
     * @return The estimated cost rounded to 2 decimal places.
     */
    public BigDecimal calculateMonthlyCost(long totalRequests, BigDecimal costPer1000Requests) {
        if (totalRequests <= 0 || costPer1000Requests == null) {
            return BigDecimal.ZERO;
        }

        // 1. Figure out how many "blocks" of 1000 requests we have
        // Example: 2500 requests / 1000 = 2.5 blocks
        BigDecimal blocksOfThousand = BigDecimal.valueOf(totalRequests)
                .divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP);

        // 2. Multiply the blocks by the rate
        // Example: 2.5 blocks * $1.50 = $3.75
        BigDecimal totalCost = blocksOfThousand.multiply(costPer1000Requests);

        // 3. Round to 2 decimal places (standard currency format)
        BigDecimal finalCost = totalCost.setScale(2, RoundingMode.HALF_UP);

        log.info("Calculated monthly cost for {} requests at ${}/1k: ${}",
                totalRequests, costPer1000Requests, finalCost);

        return finalCost;
    }
}