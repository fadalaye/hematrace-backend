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
public class DashboardOverviewDto {
    private DashboardStatsDto stats;
    private List<DashboardAlertDto> alertes;
    private List<DashboardBloodGroupDto> stockParGroupe;
    private List<DashboardProductTypeDto> stockParType;
}