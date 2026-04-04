package com.hematrace.hematrace.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardRecentActivitiesDto {
    private List<DashboardRecentActivityDto> dernieresDemandes;
    private List<DashboardRecentActivityDto> dernieresDelivrances;
    private List<DashboardRecentActivityDto> dernieresTransfusions;
}