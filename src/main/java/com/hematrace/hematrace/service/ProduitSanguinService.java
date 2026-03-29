package com.hematrace.hematrace.service;

import com.hematrace.hematrace.entite.ProduitSanguin;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ProduitSanguinService {
    // Création métier avec validation
    ProduitSanguin ajouterProduitAuStock(ProduitSanguin produitSanguin);
    List<ProduitSanguin> ajouterProduitsEnLot(List<ProduitSanguin> produits);
    
    // CRUD de base
    List<ProduitSanguin> getAllProduitsSanguins();
    Optional<ProduitSanguin> getProduitSanguinById(Long id);
    ProduitSanguin getProduitSanguinByCode(String codeProduit);
    ProduitSanguin updateProduitSanguin(Long id, ProduitSanguin produitSanguinDetails);
    void deleteProduitSanguin(Long id);
    
    // Gestion des états
    void updateEtatProduitSanguin(Long id, String nouvelEtat);
    void marquerCommeExpire(Long produitId);
    void marquerCommeUtilise(Long produitId);
    void verifierEtMarquerProduitsExpires();
    
    // Recherches et filtres
    List<ProduitSanguin> getProduitsSanguinsByType(String typeProduit);
    List<ProduitSanguin> getProduitsSanguinsByGroupeSanguin(String groupeSanguin);
    List<ProduitSanguin> getProduitsSanguinsByRhesus(String rhesus);
    List<ProduitSanguin> getProduitsSanguinsByEtat(String etat);
    List<ProduitSanguin> getProduitsSanguinsDisponibles();
    List<ProduitSanguin> getProduitsSanguinsProchesPeremption(int joursRestants);
    List<ProduitSanguin> getProduitsSanguinsExpires();
    List<ProduitSanguin> getProduitsSanguinsByDelivrance(Long delivranceId);
    List<ProduitSanguin> getProduitsSanguinsByTransfusion(Long transfusionId);
    
    // Recherches combinées pour optimisation
    List<ProduitSanguin> getProduitsDisponiblesByGroupe(String groupeSanguin);
    List<ProduitSanguin> getProduitsDisponiblesByType(String typeProduit);
    List<ProduitSanguin> getProduitsDisponiblesByGroupeAndType(String groupeSanguin, String typeProduit);
    List<ProduitSanguin> getProduitsCompatibles(String groupeSanguinPatient, String typeProduit);
    
    // Validation métier
    boolean estCompatible(Long produitId, String groupeSanguinPatient);
    boolean estUtilisable(Long produitId);
    
    // Statistiques
    long countProduitsSanguinsByType(String typeProduit);
    long countProduitsSanguinsByGroupeSanguin(String groupeSanguin);
    long countProduitsSanguinsDisponibles();
    Map<String, Long> getStatistiquesStock();
    Map<String, Long> getAlertesStock();
}