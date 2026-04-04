package com.hematrace.hematrace.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDto {
    private long totalProduits;
    private long produitsDisponibles;
    private long produitsUtilises;
    private long produitsExpires;
    private long produitsDelivres;
    private long produitsProchesPeremption;

    private long totalDemandes;
    private long demandesEnAttente;
    private long demandesValidees;
    private long demandesRejetees;
    private long demandesDelivrees;
    private long demandesUrgentes;

    private long totalDelivrances;
    private long delivrancesAujourdhui;
    private long delivrancesCeMois;

    private long totalTransfusions;
    private long transfusionsAvecEffets;
    private long transfusionsSansEffets;

    private long totalIncidents;
    private long incidentsValides;
    private long incidentsNonValides;
}