package com.hematrace.hematrace.repository;

import com.hematrace.hematrace.entite.IncidentTransfusionnel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RepositoryRestResource
public interface IncidentTransfusionnelRepository extends JpaRepository<IncidentTransfusionnel, Long>, JpaSpecificationExecutor<IncidentTransfusionnel> {

    IncidentTransfusionnel findByTransfusionId(Long transfusionId);
    
    List<IncidentTransfusionnel> findByDateIncident(LocalDate dateIncident);
    
    List<IncidentTransfusionnel> findByPatientNom(String patientNom);
    
    List<IncidentTransfusionnel> findByPatientNumDossier(String patientNumDossier);
    
    List<IncidentTransfusionnel> findByTypeProduitTransfuse(String typeProduitTransfuse);
    
    List<IncidentTransfusionnel> findByNomDeclarant(String nomDeclarant);
    
    List<IncidentTransfusionnel> findByDateValidationIsNull();
    
    List<IncidentTransfusionnel> findByDateValidationIsNotNull();

    
    
    @Query("SELECT i FROM IncidentTransfusionnel i WHERE i.dateIncident BETWEEN :startDate AND :endDate")
    List<IncidentTransfusionnel> findByDateIncidentBetween(@Param("startDate") LocalDate startDate, 
                                                          @Param("endDate") LocalDate endDate);
    
    @Query("SELECT i FROM IncidentTransfusionnel i WHERE i.patientNom = :nom AND i.patientPrenom = :prenom")
    List<IncidentTransfusionnel> findByPatientNomAndPrenom(@Param("nom") String nom, @Param("prenom") String prenom);
    
    // CORRECTION : Utiliser CAST pour convertir le CLOB en VARCHAR
    @Query("SELECT i FROM IncidentTransfusionnel i WHERE CAST(i.descriptionIncident AS string) LIKE %:keyword%")
    List<IncidentTransfusionnel> findByDescriptionIncidentContaining(@Param("keyword") String keyword);
    
    // CORRECTION : Nom d'attribut corrigé (registreHemovigilance au lieu de registreHemovigilance)
    @Query("SELECT i FROM IncidentTransfusionnel i WHERE i.registreHemovigilance = :registre")
    List<IncidentTransfusionnel> findByRegistreHemovigilance(@Param("registre") String registre);
    
    @Query("SELECT i FROM IncidentTransfusionnel i WHERE i.signes LIKE %:keyword%")
    List<IncidentTransfusionnel> findBySignesContaining(@Param("keyword") String keyword);
    
    @Query("SELECT i FROM IncidentTransfusionnel i WHERE i.symptomes LIKE %:keyword%")
    List<IncidentTransfusionnel> findBySymptomesContaining(@Param("keyword") String keyword);
    
    @Query("SELECT COUNT(i) FROM IncidentTransfusionnel i WHERE i.dateValidation IS NULL")
    long countByNonValides();
    
    @Query("SELECT COUNT(i) FROM IncidentTransfusionnel i WHERE i.dateValidation IS NOT NULL")
    long countByValides();
    
    @Query("SELECT i FROM IncidentTransfusionnel i WHERE i.numeroLotProduit = :numeroLot")
    List<IncidentTransfusionnel> findByNumeroLotProduit(@Param("numeroLot") String numeroLot);
    
    // CORRECTION : Nom d'attribut corrigé ici aussi si nécessaire
    // @Query("SELECT i FROM IncidentTransfusionnel i WHERE i.registreHemovigilance = :registre")
    // List<IncidentTransfusionnel> findByRegistreHemovigilance(@Param("registre") String registre);

        long countByDateValidationIsNull();
    long countByDateValidationIsNotNull();
    long countByDateIncidentBetween(LocalDate start, LocalDate end);
}