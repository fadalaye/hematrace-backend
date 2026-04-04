package com.hematrace.hematrace.repository;

import com.hematrace.hematrace.entite.Demande;
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
public interface DemandeRepository extends JpaRepository<Demande, Long>, JpaSpecificationExecutor<Demande> {

    // ========== MÉTHODES DE BASE ==========
    
    List<Demande> findByMedecinId(Long medecinId);
    
    List<Demande> findByStatut(String statut);
    
    List<Demande> findByServiceDemandeur(String service);
    
    List<Demande> findByTypeProduitDemande(String typeProduitDemande);
    
    List<Demande> findByPatientNomContaining(String nom);
    
    // CORRIGÉ : Utiliser la bonne casse
    long countByStatut(String statut);
    
    // ========== RECHERCHE PAR URGENCE ==========
    
    List<Demande> findByUrgence(Boolean urgence);
    
    List<Demande> findByStatutAndUrgence(String statut, Boolean urgence);
    
    // ========== RECHERCHE PAR DATE ==========
    
    @Query("SELECT d FROM Demande d WHERE d.dateHeureDemande BETWEEN :start AND :end")
    List<Demande> findByDate(@Param("start") LocalDateTime start,
                            @Param("end") LocalDateTime end);
    
    List<Demande> findByPersonnelId(Long personnelId);
    
    List<Demande> findByGroupeSanguinPatient(String groupeSanguin);
    
    // CORRIGÉ : Utiliser le bon nom de champ
    List<Demande> findByDateHeureDemandeBetween(LocalDateTime start, LocalDateTime end);
    
    // ========== RECHERCHE PAR PATIENT ==========
    
    List<Demande> findByPatientNomContainingIgnoreCase(String nom);
    
    // ========== MÉTHODES AVEC RELATIONS ==========
    
    @Query("SELECT DISTINCT d FROM Demande d " +
           "LEFT JOIN FETCH d.medecin m " +
           "LEFT JOIN FETCH d.personnel p " +
           "LEFT JOIN FETCH d.delivrance del " +
           "ORDER BY d.dateHeureDemande DESC")
    List<Demande> findAllWithRelations();
    
    @Query("SELECT d FROM Demande d " +
           "LEFT JOIN FETCH d.medecin m " +
           "LEFT JOIN FETCH d.personnel p " +
           "LEFT JOIN FETCH d.delivrance del " +
           "WHERE d.id = :id")
    Optional<Demande> findByIdWithRelations(@Param("id") Long id);
    
    // ========== MÉTHODES DE COMPTAGE ==========
    
    // CORRIGÉ : Utiliser la bonne casse
    @Query("SELECT COUNT(d) FROM Demande d WHERE d.dateHeureDemande BETWEEN :start AND :end")
    long countByDateRange(@Param("start") LocalDateTime start, 
                         @Param("end") LocalDateTime end);
    
    // Alternative avec query method (sensible à la casse)
    long countByDateHeureDemandeBetween(LocalDateTime start, LocalDateTime end);
    
    // ========== NOUVELLES MÉTHODES POUR LA TRAÇABILITÉ ==========
    
    // Statistiques par statut
    @Query("SELECT d.statut, COUNT(d) FROM Demande d GROUP BY d.statut")
    List<Object[]> countByAllStatuts();
    
    // Statistiques par type de produit
    @Query("SELECT d.typeProduitDemande, COUNT(d) FROM Demande d GROUP BY d.typeProduitDemande")
    List<Object[]> countByTypeProduit();
    
    // Statistiques par service
    @Query("SELECT d.serviceDemandeur, COUNT(d) FROM Demande d GROUP BY d.serviceDemandeur ORDER BY COUNT(d) DESC")
    List<Object[]> countByServiceDemandeur();
    
    // Demandes récentes
    @Query("SELECT d FROM Demande d WHERE d.dateHeureDemande >= :date ORDER BY d.dateHeureDemande DESC")
    List<Demande> findRecentDemandes(@Param("date") LocalDateTime date);
    
    // Demandes en attente (non traitées)
    @Query("SELECT d FROM Demande d WHERE d.statut = 'EN ATTENTE' ORDER BY d.dateHeureDemande ASC")
    List<Demande> findPendingDemandes();
    
    // Demandes validées
       @Query("SELECT d FROM Demande d WHERE d.statut = 'VALIDÉE' ORDER BY d.dateHeureDemande DESC")
       List<Demande> findValidatedDemandes();
    
    // Demandes urgentes
    List<Demande> findByUrgenceTrue();
    
    // Demandes non urgentes
    List<Demande> findByUrgenceFalse();
    
    // Demandes par groupe sanguin et type de produit
    @Query("SELECT d FROM Demande d WHERE d.groupeSanguinPatient = :groupeSanguin AND d.typeProduitDemande = :typeProduit")
    List<Demande> findByGroupeSanguinAndTypeProduit(@Param("groupeSanguin") String groupeSanguin,
                                                   @Param("typeProduit") String typeProduit);
    
    // Demandes sans délivrance
    @Query("SELECT d FROM Demande d WHERE d.delivrance IS NULL")
    List<Demande> findWithoutDelivrance();
    
    // Demandes avec délivrance
    @Query("SELECT d FROM Demande d WHERE d.delivrance IS NOT NULL")
    List<Demande> findWithDelivrance();
    
    // Demandes triées par date (plus récentes en premier)
    List<Demande> findAllByOrderByDateHeureDemandeDesc();
    
    // Demandes triées par date (plus anciennes en premier)
    List<Demande> findAllByOrderByDateHeureDemandeAsc();
    
    // Demandes par date de naissance du patient
    List<Demande> findByPatientDateNaissance(LocalDate dateNaissance);
    
    List<Demande> findByPatientDateNaissanceBetween(LocalDate startDate, LocalDate endDate);
    
    // Recherche avancée
    @Query("SELECT d FROM Demande d WHERE " +
           "(:medecinId IS NULL OR d.medecin.id = :medecinId) AND " +
           "(:personnelId IS NULL OR d.personnel.id = :personnelId) AND " +
           "(:statut IS NULL OR d.statut = :statut) AND " +
           "(:urgence IS NULL OR d.urgence = :urgence) AND " +
           "(:startDate IS NULL OR d.dateHeureDemande >= :startDate) AND " +
           "(:endDate IS NULL OR d.dateHeureDemande <= :endDate)")
    List<Demande> rechercheAvancee(@Param("medecinId") Long medecinId,
                                  @Param("personnelId") Long personnelId,
                                  @Param("statut") String statut,
                                  @Param("urgence") Boolean urgence,
                                  @Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);
    
    // Statistiques par jour
    @Query("SELECT DATE(d.dateHeureDemande) as jour, COUNT(d) as nombre " +
           "FROM Demande d " +
           "WHERE d.dateHeureDemande BETWEEN :start AND :end " +
           "GROUP BY DATE(d.dateHeureDemande) " +
           "ORDER BY jour")
    List<Object[]> countByDay(@Param("start") LocalDateTime start,
                             @Param("end") LocalDateTime end);
    
    // Statistiques par mois
    @Query("SELECT FUNCTION('YEAR', d.dateHeureDemande) as annee, " +
           "FUNCTION('MONTH', d.dateHeureDemande) as mois, " +
           "COUNT(d) as nombre " +
           "FROM Demande d " +
           "WHERE d.dateHeureDemande BETWEEN :start AND :end " +
           "GROUP BY FUNCTION('YEAR', d.dateHeureDemande), FUNCTION('MONTH', d.dateHeureDemande) " +
           "ORDER BY annee, mois")
    List<Object[]> countByMonth(@Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end);


long countByUrgenceTrue();

@Query("SELECT COUNT(d) FROM Demande d WHERE d.delivrance IS NOT NULL")
long countDemandesAvecDelivrance();

@Query("SELECT COUNT(d) FROM Demande d WHERE d.delivrance IS NULL AND UPPER(d.statut) = 'VALIDÉE'")
long countDemandesValideesSansDelivrance();
}