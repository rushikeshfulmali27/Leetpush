package com.leethubai.analytics.controller;

import com.leethubai.analytics.service.AnalyticsService;
import com.leethubai.common.dto.ValidationDTOs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Validated
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ValidationDTOs.ApiResponse<Object>> getSummary() {
        log.info("Retrieving analytics summary");
        var summary = analyticsService.getSummary();
        return ResponseEntity.ok(ValidationDTOs.ApiResponse.success(summary, "Analytics summary retrieved"));
    }

    @GetMapping("/heatmap")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ValidationDTOs.ApiResponse<Object>> getHeatmap(
            @RequestParam(required = false) Integer year) {
        log.info("Retrieving heatmap: year={}", year);
        var heatmap = analyticsService.getHeatmap(year);
        return ResponseEntity.ok(ValidationDTOs.ApiResponse.success(heatmap, "Heatmap retrieved"));
    }

    @GetMapping("/languages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ValidationDTOs.ApiResponse<Object>> getLanguageDistribution() {
        log.info("Retrieving language distribution");
        var distribution = analyticsService.getLanguageDistribution();
        return ResponseEntity.ok(ValidationDTOs.ApiResponse.success(distribution, "Language distribution retrieved"));
    }

    @GetMapping("/topics")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ValidationDTOs.ApiResponse<Object>> getTopicPerformance() {
        log.info("Retrieving topic performance");
        var topics = analyticsService.getTopicPerformance();
        return ResponseEntity.ok(ValidationDTOs.ApiResponse.success(topics, "Topic performance retrieved"));
    }
}
