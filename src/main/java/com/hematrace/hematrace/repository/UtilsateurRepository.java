package com.hematrace.hematrace.repository;

import com.hematrace.hematrace.entite.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource
public interface UtilsateurRepository extends JpaRepository<Utilisateur, Long> {
    
    Optional<Utilisateur> findByMatricule(String matricule);
    
    Optional<Utilisateur> findByEmail(String email);
    
    boolean existsByMatricule(String matricule);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM Utilisateur u WHERE u.statut = :statut")
    List<Utilisateur> findByStatut(@Param("statut") String statut);

    default Optional<Utilisateur> findByMatriculeOrEmail(String identifiant) {
        return findByMatricule(identifiant)
                .or(() -> findByEmail(identifiant));
    }
}