package com.hematrace.hematrace.repository;

import com.hematrace.hematrace.entite.Delivrance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RepositoryRestResource
public interface DelivranceRepository extends JpaRepository<Delivrance, Long>, JpaSpecificationExecutor<Delivrance> {

    // ========== MÉTHODES DE RECHERCHE ==========
    
    // Recherche par demande (OneToOne donc retourne un Optional)
    @Query("SELECT d FROM Delivrance d WHERE d.demande.id = :demandeId")
    Optional<Delivrance> findByDemandeId(@Param("demandeId") Long demandeId);
    
    // Vérifier si une demande a déjà une délivrance
    boolean existsByDemandeId(Long demandeId);
    
    // Recherche par personnel
    List<Delivrance> findByPersonnelId(Long personnelId);
    
    // Recherche par destination
    List<Delivrance> findByDestinationContainingIgnoreCase(String destination);
    
    // Recherche par date précise (CORRIGÉ : utiliser le bon nom de champ)
    @Query("SELECT d FROM Delivrance d WHERE DATE(d.dateHeureDelivrance) = :date")
    List<Delivrance> findByDate(@Param("date") LocalDate date);
    
    // Recherche dans un intervalle de dates (CORRIGÉ)
    List<Delivrance> findByDateHeureDelivranceBetween(LocalDateTime start, LocalDateTime end);
    
    // Recherche par produit sanguin
    @Query("SELECT d FROM Delivrance d JOIN d.produitsSanguins p WHERE p.id = :produitId")
    List<Delivrance> findByProduitSanguinId(@Param("produitId") Long produitId);
    
    // Compter par type de produit
    @Query("SELECT COUNT(DISTINCT d) FROM Delivrance d JOIN d.produitsSanguins p WHERE p.typeProduit = :typeProduit")
    long countByProduitsSanguinsTypeProduit(@Param("typeProduit") String typeProduit);
    
    // Compter par personnel
    long countByPersonnelId(Long personnelId);
    
    // Compter dans un intervalle de dates (CORRIGÉ)
    long countByDateHeureDelivranceBetween(LocalDateTime start, LocalDateTime end);
    
    // Recherches combinées
    @Query("SELECT d FROM Delivrance d WHERE d.personnel.id = :personnelId AND d.dateHeureDelivrance BETWEEN :start AND :end")
    List<Delivrance> findByPersonnelAndDateRange(@Param("personnelId") Long personnelId, 
                                                @Param("start") LocalDateTime start, 
                                                @Param("end") LocalDateTime end);
    
    // Charger une délivrance avec tous les détails
    @Query("SELECT d FROM Delivrance d " +
           "LEFT JOIN FETCH d.demande " +
           "LEFT JOIN FETCH d.produitsSanguins " +
           "LEFT JOIN FETCH d.personnel " +
           "WHERE d.id = :id")
    Optional<Delivrance> findByIdWithDetails(@Param("id") Long id);
    
    // Charger toutes les délivrances avec détails
    @Query("SELECT DISTINCT d FROM Delivrance d " +
           "LEFT JOIN FETCH d.demande " +
           "LEFT JOIN FETCH d.produitsSanguins " +
           "LEFT JOIN FETCH d.personnel")
    List<Delivrance> findAllWithDetails();
    
    // ========== NOUVELLES MÉTHODES POUR LA TRAÇABILITÉ ==========
    
    // Recherche par date pour les statistiques
    @Query("SELECT COUNT(d) FROM Delivrance d WHERE d.dateHeureDelivrance BETWEEN :start AND :end")
    long countDelivrancesByDateRange(@Param("start") LocalDateTime start, 
                                    @Param("end") LocalDateTime end);
    
    // Recherche par mois
    @Query("SELECT COUNT(d) FROM Delivrance d WHERE YEAR(d.dateHeureDelivrance) = :year AND MONTH(d.dateHeureDelivrance) = :month")
    long countByMonth(@Param("year") int year, @Param("month") int month);
    
    // Délivrances récentes
    @Query("SELECT d FROM Delivrance d WHERE d.dateHeureDelivrance >= :date ORDER BY d.dateHeureDelivrance DESC")
    List<Delivrance> findRecentDelivrances(@Param("date") LocalDateTime date);
    
    // Délivrances par destination et date
    @Query("SELECT d FROM Delivrance d WHERE d.destination LIKE %:destination% AND d.dateHeureDelivrance BETWEEN :start AND :end")
    List<Delivrance> findByDestinationAndDateRange(@Param("destination") String destination,
                                                  @Param("start") LocalDateTime start,
                                                  @Param("end") LocalDateTime end);
    
    // Délivrances sans produits
    @Query("SELECT d FROM Delivrance d WHERE SIZE(d.produitsSanguins) = 0")
    List<Delivrance> findWithoutProduits();
    
    // Délivrances avec plus de X produits
    @Query("SELECT d FROM Delivrance d WHERE SIZE(d.produitsSanguins) > :minProduits")
    List<Delivrance> findByMinProduits(@Param("minProduits") int minProduits);
    
    // Statistiques par destination
    @Query("SELECT d.destination, COUNT(d) as count FROM Delivrance d GROUP BY d.destination ORDER BY count DESC")
    List<Object[]> countByDestination();
    
    // Statistiques par personnel
    @Query("SELECT p.nom, p.prenom, COUNT(d) as count FROM Delivrance d JOIN d.personnel p GROUP BY p.id ORDER BY count DESC")
    List<Object[]> countByPersonnel();
    
    // Délivrances triées par date (plus récentes en premier)
    List<Delivrance> findAllByOrderByDateHeureDelivranceDesc();
    
    // Délivrances triées par date (plus anciennes en premier)
    List<Delivrance> findAllByOrderByDateHeureDelivranceAsc();
    
    // Délivrances urgentes (liées à des demandes urgentes)
    @Query("SELECT d FROM Delivrance d JOIN d.demande dem WHERE dem.urgence = true")
    List<Delivrance> findUrgentDelivrances();
    
    // Délivrances par mode de transport
    List<Delivrance> findByModeTransport(String modeTransport);
    
    // Délivrances par mode de transport (insensible à la casse)
    List<Delivrance> findByModeTransportIgnoreCase(String modeTransport);
    
    // Compter par mode de transport
    long countByModeTransport(String modeTransport);
    
    // Délivrances avec observations
    @Query("SELECT d FROM Delivrance d WHERE d.observations IS NOT NULL AND LENGTH(TRIM(d.observations)) > 0")
    List<Delivrance> findWithObservations();
    
    // Délivrances sans observations
    @Query("SELECT d FROM Delivrance d WHERE d.observations IS NULL OR LENGTH(TRIM(d.observations)) = 0")
    List<Delivrance> findWithoutObservations();

    List<Delivrance> findTop10ByOrderByDateHeureDelivranceDesc();
}