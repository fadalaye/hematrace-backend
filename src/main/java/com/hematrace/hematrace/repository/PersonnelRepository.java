package com.hematrace.hematrace.repository;

import com.hematrace.hematrace.entite.Personnel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource
public interface PersonnelRepository extends JpaRepository<Personnel, Long> {
    
    Optional<Personnel> findByMatricule(String matricule);
    
    Optional<Personnel> findByEmail(String email);
    
    @Query("SELECT p FROM Personnel p WHERE p.fonction = :fonction")
    List<Personnel> findByFonction(@Param("fonction") String fonction);
    
    @Query("SELECT p FROM Personnel p WHERE p.statut = :statut")
    List<Personnel> findByStatut(@Param("statut") String statut);
    
    @Query("SELECT p FROM Personnel p WHERE p.fonction LIKE %:keyword%")
    List<Personnel> findByFonctionContaining(@Param("keyword") String keyword);
    
    @Query("SELECT COUNT(p) FROM Personnel p WHERE p.fonction = :fonction")
    long countByFonction(@Param("fonction") String fonction);
    
    @Query("SELECT p FROM Personnel p WHERE p.dateEmbauche BETWEEN :startDate AND :endDate")
    List<Personnel> findByDateEmbaucheBetween(@Param("startDate") java.time.LocalDate startDate, 
                                             @Param("endDate") java.time.LocalDate endDate);
}