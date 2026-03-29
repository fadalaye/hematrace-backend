package com.hematrace.hematrace.repository;

import com.hematrace.hematrace.entite.ChefService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource
public interface ChefServiceRepository extends JpaRepository<ChefService, Long> {
    
    Optional<ChefService> findByMatricule(String matricule);
    
    Optional<ChefService> findByEmail(String email);
    
    @Query("SELECT c FROM ChefService c WHERE c.serviceDirige = :serviceDirige")
    List<ChefService> findByServiceDirige(@Param("serviceDirige") String serviceDirige);
    
    @Query("SELECT c FROM ChefService c WHERE c.departement = :departement")
    List<ChefService> findByDepartement(@Param("departement") String departement);
    
    @Query("SELECT c FROM ChefService c WHERE c.serviceDirige = :service AND c.departement = :departement")
    List<ChefService> findByServiceAndDepartement(@Param("service") String service, @Param("departement") String departement);
    
    @Query("SELECT c FROM ChefService c WHERE c.statut = :statut")
    List<ChefService> findByStatut(@Param("statut") String statut);
}