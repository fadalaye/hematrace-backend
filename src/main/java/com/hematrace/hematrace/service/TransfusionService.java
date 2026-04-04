package com.hematrace.hematrace.service;

import com.hematrace.hematrace.dto.CorrectionCliniqueTransfusionRequest;
import com.hematrace.hematrace.dto.CreerTransfusionDTO;
import com.hematrace.hematrace.dto.TransfusionWithSurveillancesDTO;
import com.hematrace.hematrace.entite.Transfusion;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface TransfusionService {
    
    // ========== CRÉATION DE TRANSFUSION ==========
    
    /**
     * Crée une transfusion à partir d'une entité existante
     */
    Transfusion creerTransfusion(Transfusion transfusion);
    
    /**
     * Crée une transfusion à partir d'un DTO simple (sans surveillances)
     */
    Transfusion creerTransfusion(CreerTransfusionDTO dto);
    
    /**
     * Crée une transfusion avec ses surveillances en une seule requête
     */
    Transfusion creerTransfusionAvecSurveillances(TransfusionWithSurveillancesDTO dto);
    
    // ========== RÉCUPÉRATION ==========
    
    List<Transfusion> getAllTransfusions();
    
    Optional<Transfusion> getTransfusionById(Long id);
    
    // ========== RECHERCHE PAR CRITÈRES ==========
    
    List<Transfusion> getTransfusionsByMedecin(Long medecinId);
    
    List<Transfusion> getTransfusionsByProduitSanguin(Long produitSanguinId);
    
    List<Transfusion> getTransfusionsByDate(LocalDate date);
    
    List<Transfusion> getTransfusionsByDateRange(LocalDate startDate, LocalDate endDate);
    
    List<Transfusion> getTransfusionsByGroupeSanguin(String groupeSanguin);
    
    List<Transfusion> getTransfusionsByTolerance(String tolerance);
    
    List<Transfusion> getTransfusionsAvecEffetsIndesirables();
    
    List<Transfusion> getTransfusionsByPatient(String nom, String prenom);
    
    List<Transfusion> getTransfusionsByNumDossier(String numDossier);
    
    // ========== STATISTIQUES ==========
    
    long countTransfusionsByTolerance(String tolerance);
    
    long countTransfusionsAvecEffetsIndesirables();
    
    // ========== MISE À JOUR ET SUPPRESSION ==========
    
    Transfusion updateTransfusion(Long id, Transfusion transfusionDetails);
    
    void deleteTransfusion(Long id);

    /**
     * Récupère les transfusions sans incident déclaré
     */
    List<Transfusion> getTransfusionsSansIncident();
    
    /**
     * Vérifie si une transfusion a déjà un incident
     */
    boolean hasIncident(Long transfusionId);
    
    /**
     * Récupère les transfusions compatibles pour déclarer un incident
     */
    List<Transfusion> getTransfusionsCompatiblesIncident();

    public Transfusion corrigerCliniquement(Long id, CorrectionCliniqueTransfusionRequest request);

}