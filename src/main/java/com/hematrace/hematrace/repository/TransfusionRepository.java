package com.hematrace.hematrace.repository;

import com.hematrace.hematrace.entite.Transfusion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RepositoryRestResource
public interface TransfusionRepository extends JpaRepository<Transfusion, Long>, JpaSpecificationExecutor<Transfusion> {
    // ========== MÉTHODES DE BASE ==========
    
    @Override
    Optional<Transfusion> findById(Long id);
    
    @Override
    List<Transfusion> findAll();
    
    @Override
    <S extends Transfusion> S save(S entity);
    
    @Override
    void delete(Transfusion entity);
    
    @Override
    void deleteById(Long id);
    
    // ========== MÉTHODES DE RECHERCHE PAR RELATIONS ==========
    
    List<Transfusion> findByMedecinId(Long medecinId);
    
    List<Transfusion> findByProduitSanguinId(Long produitSanguinId);
    
    // ========== MÉTHODES DE RECHERCHE PAR PATIENT ==========
    
    List<Transfusion> findByGroupeSanguinPatient(String groupeSanguinPatient);
    
    List<Transfusion> findByGroupeSanguinPatientIgnoreCase(String groupeSanguinPatient);
    
    @Query("SELECT t FROM Transfusion t WHERE LOWER(t.patientNom) = LOWER(:nom) AND LOWER(t.patientPrenom) = LOWER(:prenom)")
    List<Transfusion> findByPatientNomIgnoreCaseAndPatientPrenomIgnoreCase(
            @Param("nom") String nom, 
            @Param("prenom") String prenom);
    
    List<Transfusion> findByPatientNumDossier(String patientNumDossier);
    
    List<Transfusion> findByPatientNumDossierIgnoreCase(String patientNumDossier);
    
    // ========== MÉTHODES DE RECHERCHE PAR DATES ==========
    
    List<Transfusion> findByDateTransfusion(LocalDate dateTransfusion);
    
    List<Transfusion> findByDateTransfusionBetween(LocalDate startDate, LocalDate endDate);
    
    List<Transfusion> findByPatientDateNaissance(LocalDate patientDateNaissance);
    
    List<Transfusion> findByPatientDateNaissanceBetween(LocalDate startDate, LocalDate endDate);
    
    // ========== MÉTHODES DE RECHERCHE PAR CARACTÉRISTIQUES ==========
    
    List<Transfusion> findByTolerance(String tolerance);
    
    List<Transfusion> findByToleranceIgnoreCase(String tolerance);
    
    List<Transfusion> findByEffetsIndesirables(Boolean effetsIndesirables);
    
    // ========== MÉTHODES DE RECHERCHE PAR DÉCLARANT ==========
    
    @Query("SELECT t FROM Transfusion t WHERE LOWER(t.nomDeclarant) = LOWER(:nom) AND LOWER(t.prenomDeclarant) = LOWER(:prenom)")
    List<Transfusion> findByDeclarant(@Param("nom") String nom, @Param("prenom") String prenom);
    
    // ========== MÉTHODES DE RECHERCHE PAR EFFETS ==========
    
    @Query("SELECT t FROM Transfusion t WHERE t.typeEffet LIKE %:typeEffet%")
    List<Transfusion> findByTypeEffetContaining(@Param("typeEffet") String typeEffet);
    
    // CORRECTION : Supprimer les méthodes qui utilisent 'graviteEffetIgnoreCase' si le champ n'existe pas
    // List<Transfusion> findByGraviteEffetIgnoreCase(String graviteEffet); // ← Supprimer si le champ n'existe pas
    
    // ========== MÉTHODES DE RECHERCHE PAR VOLUME ==========
    
    List<Transfusion> findByVolumeMl(Integer volumeMl);
    
    List<Transfusion> findByVolumeMlGreaterThan(Integer volume);
    
    List<Transfusion> findByVolumeMlLessThan(Integer volume);
    
    List<Transfusion> findByVolumeMlBetween(Integer minVolume, Integer maxVolume);
    
    // ========== MÉTHODES STATISTIQUES ==========
    
    long countByTolerance(String tolerance);
    
    long countByEffetsIndesirables(Boolean effetsIndesirables);
    
    long countByGroupeSanguinPatient(String groupeSanguin);
    
    long countByDateTransfusion(LocalDate date);
    
    // ========== MÉTHODES AVANCÉES ==========
    
    @Query("SELECT t FROM Transfusion t WHERE " +
           "(:medecinId IS NULL OR t.medecin.id = :medecinId) AND " +
           "(:produitSanguinId IS NULL OR t.produitSanguin.id = :produitSanguinId) AND " +
           "(:groupeSanguin IS NULL OR LOWER(t.groupeSanguinPatient) = LOWER(:groupeSanguin)) AND " +
           "(:tolerance IS NULL OR LOWER(t.tolerance) = LOWER(:tolerance)) AND " +
           "(:startDate IS NULL OR t.dateTransfusion >= :startDate) AND " +
           "(:endDate IS NULL OR t.dateTransfusion <= :endDate)")
    List<Transfusion> rechercheAvancee(
            @Param("medecinId") Long medecinId,
            @Param("produitSanguinId") Long produitSanguinId,
            @Param("groupeSanguin") String groupeSanguin,
            @Param("tolerance") String tolerance,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT DISTINCT t.groupeSanguinPatient FROM Transfusion t ORDER BY t.groupeSanguinPatient")
    List<String> findDistinctGroupesSanguins();
    
    @Query("SELECT DISTINCT t.tolerance FROM Transfusion t ORDER BY t.tolerance")
    List<String> findDistinctTolerances();
    
    @Query("SELECT t FROM Transfusion t WHERE t.surveillances IS EMPTY")
    List<Transfusion> findWithoutSurveillances();
    
    @Query("SELECT t FROM Transfusion t WHERE SIZE(t.surveillances) > 0")
    List<Transfusion> findWithSurveillances();
    
    @Query("SELECT t FROM Transfusion t WHERE t.incidentTransfusionnel IS NOT NULL")
    List<Transfusion> findWithIncidents();
    
    // ========== NOUVELLE MÉTHODE : Recherche par délivrance ==========
    // Via le produit sanguin qui est lié à la délivrance
    @Query("SELECT t FROM Transfusion t JOIN t.produitSanguin ps WHERE ps.delivrance.id = :delivranceId")
    List<Transfusion> findByDelivranceId(@Param("delivranceId") Long delivranceId);
    
    // ========== MÉTHODES DE TRI ==========
    
    List<Transfusion> findAllByOrderByDateTransfusionDesc();
    
    List<Transfusion> findAllByOrderByDateTransfusionAsc();
    
    @Query("SELECT t FROM Transfusion t WHERE t.dateTransfusion >= :date ORDER BY t.dateTransfusion DESC")
    List<Transfusion> findRecentTransfusions(@Param("date") LocalDate date);

    // ========== MÉTHODES POUR INCIDENTS ==========
    
    @Query("SELECT t FROM Transfusion t WHERE t.incidentTransfusionnel IS NULL")
    List<Transfusion> findTransfusionsSansIncident();
    
    @Query("SELECT t FROM Transfusion t WHERE t.incidentTransfusionnel IS NOT NULL")
    List<Transfusion> findTransfusionsAvecIncident();
    
    // ========== AUTRES MÉTHODES UTILES ==========
    
    @Query("SELECT t FROM Transfusion t WHERE t.etatPatientApres LIKE %:etat%")
    List<Transfusion> findByEtatPatientApresContaining(@Param("etat") String etat);
    
    @Query("SELECT t FROM Transfusion t WHERE t.notes IS NOT NULL AND LENGTH(TRIM(t.notes)) > 0")
    List<Transfusion> findWithNotes();
    
    @Query("SELECT t FROM Transfusion t WHERE t.notes IS NULL OR LENGTH(TRIM(t.notes)) = 0")
    List<Transfusion> findWithoutNotes();
    
    // Transfusions par période
    @Query("SELECT COUNT(t) FROM Transfusion t WHERE t.dateTransfusion BETWEEN :start AND :end")
    long countByDateTransfusionBetween(@Param("start") LocalDate start, 
                                      @Param("end") LocalDate end);
    
    // Statistiques par mois
    @Query("SELECT FUNCTION('YEAR', t.dateTransfusion) as annee, " +
           "FUNCTION('MONTH', t.dateTransfusion) as mois, " +
           "COUNT(t) as nombre " +
           "FROM Transfusion t " +
           "WHERE t.dateTransfusion BETWEEN :start AND :end " +
           "GROUP BY FUNCTION('YEAR', t.dateTransfusion), FUNCTION('MONTH', t.dateTransfusion) " +
           "ORDER BY annee, mois")
    List<Object[]> countByMonth(@Param("start") LocalDate start,
                               @Param("end") LocalDate end);
    
    // Transfusions avec surveillance récente
    @Query("SELECT DISTINCT t FROM Transfusion t JOIN t.surveillances s WHERE s.temperature > :temperature")
    List<Transfusion> findWithHighTemperature(@Param("temperature") Double temperature);
    
    // Recherche par symptômes
    @Query("SELECT t FROM Transfusion t WHERE LOWER(t.typeEffet) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Transfusion> findByTypeEffetKeyword(@Param("keyword") String keyword);


long countByToleranceIgnoreCase(String tolerance);
long countByEffetsIndesirablesTrue();
List<Transfusion> findByEffetsIndesirablesTrue();

List<Transfusion> findTop10ByOrderByDateTransfusionDesc();
}