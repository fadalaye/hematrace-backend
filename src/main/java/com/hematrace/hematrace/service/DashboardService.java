package com.hematrace.hematrace.service;

import com.hematrace.hematrace.dto.dashboard.DashboardOverviewDto;
import com.hematrace.hematrace.dto.dashboard.DashboardRecentActivitiesDto;
import com.hematrace.hematrace.dto.dashboard.DashboardTrendsDto;

public interface DashboardService {
    DashboardOverviewDto getOverview();
    DashboardRecentActivitiesDto getRecentActivities(int limit);
    DashboardTrendsDto getTrends(int days);
}