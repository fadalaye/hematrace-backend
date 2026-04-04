package com.hematrace.hematrace.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardAlertDto {
    private String niveau;   // INFO, WARNING, DANGER
    private String code;     // PRODUITS_EXPIRES, STOCK_CRITIQUE_O_NEG, etc.
    private String titre;
    private String message;
    private Long valeur;
}