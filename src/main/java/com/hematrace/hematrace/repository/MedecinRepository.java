package com.hematrace.hematrace.repository;

import com.hematrace.hematrace.entite.Medecin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource
public interface MedecinRepository extends JpaRepository<Medecin, Long> {
    
    Optional<Medecin> findByMatricule(String matricule);
    
    Optional<Medecin> findByEmail(String email);
    
    @Query("SELECT m FROM Medecin m WHERE m.specialite = :specialite")
    List<Medecin> findBySpecialite(@Param("specialite") String specialite);
    
    @Query("SELECT m FROM Medecin m WHERE m.statut = :statut")
    List<Medecin> findByStatut(@Param("statut") String statut);
    
    @Query("SELECT m FROM Medecin m WHERE m.specialite LIKE %:keyword%")
    List<Medecin> findBySpecialiteContaining(@Param("keyword") String keyword);
    
    @Query("SELECT COUNT(m) FROM Medecin m WHERE m.specialite = :specialite")
    long countBySpecialite(@Param("specialite") String specialite);
}