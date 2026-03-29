package com.hematrace.hematrace.service;

import com.hematrace.hematrace.entite.Demande;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DemandeService {
    // CRUD de base
    Demande creerDemande(Demande demande);
    List<Demande> getAllDemandes();
    Optional<Demande> getDemandeById(Long id);
    Demande updateDemande(Long id, Demande demandeDetails);
    void deleteDemande(Long id);
    
    // Gestion des statuts
    void updateStatutDemande(Long id, String nouveauStatut);
    Demande validerDemande(Long demandeId, Long personnelId);
    void annulerDemande(Long demandeId);
    
    // Recherches et filtres
    List<Demande> getDemandesByMedecin(Long medecinId);
    List<Demande> getDemandesByPersonnel(Long personnelId);
    List<Demande> getDemandesByStatut(String statut);
    List<Demande> getDemandesByDate(LocalDate date);
    List<Demande> getDemandesByService(String service);
    List<Demande> getDemandesByGroupeSanguin(String groupeSanguin);
    List<Demande> getDemandesByTypeProduit(String typeProduitDemande);
    List<Demande> getDemandesUrgentes();
    List<Demande> getDemandesNonUrgentes();
    List<Demande> getDemandesByStatutAndUrgence(String statut, Boolean urgence);
    List<Demande> getDemandesByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    // Validation métier
    boolean peutEtreValidee(Long demandeId);
    boolean verifierStocksSuffisants(Long demandeId);
    
    // Statistiques
    long countDemandesByStatut(String statut);
    long countDemandesUrgentes();
    Map<String, Long> getStatistiquesDemandes();
}