package com.hematrace.hematrace.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardRecentActivityDto {
    private Long id;
    private String type;          // DEMANDE, DELIVRANCE, TRANSFUSION
    private String titre;
    private String description;
    private String statut;
    private LocalDateTime dateHeure;
}