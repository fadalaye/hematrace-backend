package com.hematrace.hematrace.service;

import java.util.Map;

public interface StatistiquesService {
    
    /**
     * Récupère les statistiques globales des incidents transfusionnels
     */
    Map<String, Object> getStatistiquesIncidentsGlobale();
    
    /**
     * Récupère les statistiques par type de produit
     */
    Map<String, Long> getStatistiquesIncidentsParTypeProduit();
    
    /**
     * Récupère les statistiques par mois
     */
    Map<String, Long> getStatistiquesIncidentsParMois(int annee);
    
    /**
     * Récupère les statistiques de validation
     */
    Map<String, Long> getStatistiquesValidation();
}