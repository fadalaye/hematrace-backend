package com.hematrace.hematrace.repository;

import com.hematrace.hematrace.entite.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource
public interface AdminRepository extends JpaRepository<Admin, Long> {
    
    Optional<Admin> findByMatricule(String matricule);
    
    Optional<Admin> findByEmail(String email);
    
    @Query("SELECT a FROM Admin a WHERE a.role = :role")
    List<Admin> findByRole(@Param("role") String role);
    
    @Query("SELECT a FROM Admin a WHERE a.droitsAccess LIKE %:droit%")
    List<Admin> findByDroitAccess(@Param("droit") String droit);
    
    @Query("SELECT a FROM Admin a WHERE a.statut = :statut")
    List<Admin> findByStatut(@Param("statut") String statut);
}