package com.hematrace.hematrace.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardTrendPointDto {
    private String label;   // 2026-04-01
    private long demandes;
    private long delivrances;
    private long transfusions;
    private long incidents;
}