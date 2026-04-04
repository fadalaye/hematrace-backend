package com.hematrace.hematrace.service;

import com.hematrace.hematrace.entite.Delivrance;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DelivranceService {
    
    // Création métier avec vérifications
    Delivrance creerDelivranceAvecProduits(Long demandeId, List<Long> produitIds, 
                                          Long personnelId, String destination, 
                                          String modeTransport, String observations);
    
    // CRUD de base
    List<Delivrance> getAllDelivrances();
    List<Delivrance> getAllDelivrancesWithDetails();
    Optional<Delivrance> getDelivranceById(Long id);
    Optional<Delivrance> getDelivranceByIdWithDetails(Long id);
    Delivrance updateDelivrance(Long id, Delivrance delivranceDetails);
    void deleteDelivrance(Long id);
    void annulerDelivrance(Long delivranceId);
    
    // Recherches et filtres
    List<Delivrance> getDelivrancesByPersonnel(Long personnelId);
    Optional<Delivrance> getDelivranceByDemande(Long demandeId);
    List<Delivrance> getDelivrancesByDate(LocalDate date);
    List<Delivrance> getDelivrancesByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    List<Delivrance> getDelivrancesByTypeProduit(String typeProduit);
    List<Delivrance> getDelivrancesByGroupeSanguin(String groupeSanguin);
    List<Delivrance> getDelivrancesByDestination(String destination);
    List<Delivrance> getDelivrancesByProduitSanguin(Long produitId);
    
    // Vérifications métier
    boolean peutDelivrerDemande(Long demandeId);
    boolean sontProduitsDisponibles(List<Long> produitIds);
    boolean estDemandeValidee(Long demandeId);
    
    // Gestion des produits
    List<Delivrance> getDelivrancesProchesPeremption(int joursRestants);
    void ajouterProduitADelivrance(Long delivranceId, Long produitId);
    void retirerProduitDeDelivrance(Long delivranceId, Long produitId);
    
    // Statistiques
    long countDelivrancesByTypeProduit(String typeProduit);
    long countDelivrancesByPersonnel(Long personnelId);
    Map<String, Long> getStatistiquesDelivrances();
    Map<String, Long> getDelivrancesParMois(int annee);
    long getTotalDelivrances();
    
    // Méthode pour récupérer les délivrances avec produits disponibles
    List<Delivrance> getAllDelivrancesWithAvailableProducts();

    // Méthode pour récupérer une délivrance spécifique avec produits disponibles
    Optional<Delivrance> getDelivranceByIdWithAvailableProducts(Long id);

    // Méthode pour vérifier si un produit est disponible
    boolean estProduitDisponible(Long produitId);

    Delivrance modifierDelivranceComplete(Long id, List<Long> nouveauxProduitIds, String destination,
            String modeTransport, String observations);
}