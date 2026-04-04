package com.hematrace.hematrace.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardProductTypeDto {
    private String type;        // CGR, PFC, CP...
    private long total;
    private long disponibles;
}