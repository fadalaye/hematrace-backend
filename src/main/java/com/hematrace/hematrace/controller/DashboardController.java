package com.hematrace.hematrace.controller;

import com.hematrace.hematrace.dto.dashboard.DashboardOverviewDto;
import com.hematrace.hematrace.dto.dashboard.DashboardRecentActivitiesDto;
import com.hematrace.hematrace.dto.dashboard.DashboardTrendsDto;
import com.hematrace.hematrace.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/overview")
    public ResponseEntity<DashboardOverviewDto> getOverview() {
        return ResponseEntity.ok(dashboardService.getOverview());
    }

    @GetMapping("/recent-activities")
    public ResponseEntity<DashboardRecentActivitiesDto> getRecentActivities(
            @RequestParam(defaultValue = "10") int limit) {

        if (limit <= 0) {
            limit = 10;
        }

        return ResponseEntity.ok(dashboardService.getRecentActivities(limit));
    }

    @GetMapping("/trends")
    public ResponseEntity<DashboardTrendsDto> getTrends(
            @RequestParam(defaultValue = "7") int days) {

        if (days <= 0) {
            days = 7;
        }

        return ResponseEntity.ok(dashboardService.getTrends(days));
    }
}