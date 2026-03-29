package com.hematrace.hematrace.repository;

import com.hematrace.hematrace.entite.Surveillance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.time.LocalTime;
import java.util.List;

@RepositoryRestResource
public interface SurveillanceRepository extends JpaRepository<Surveillance, Long>, JpaSpecificationExecutor<Surveillance> {
        
    List<Surveillance> findByTransfusionId(Long transfusionId);
    
    List<Surveillance> findByHeure(LocalTime heure);
    
    List<Surveillance> findByTemperature(Double temperature);
    
    List<Surveillance> findByPouls(Integer pouls);
    
    @Query("SELECT s FROM Surveillance s WHERE s.heure BETWEEN :startHeure AND :endHeure")
    List<Surveillance> findByHeureBetween(@Param("startHeure") LocalTime startHeure, 
                                         @Param("endHeure") LocalTime endHeure);
    
    @Query("SELECT s FROM Surveillance s WHERE s.temperature BETWEEN :minTemp AND :maxTemp")
    List<Surveillance> findByTemperatureBetween(@Param("minTemp") Double minTemp, 
                                               @Param("maxTemp") Double maxTemp);
    
    @Query("SELECT s FROM Surveillance s WHERE s.pouls BETWEEN :minPouls AND :maxPouls")
    List<Surveillance> findByPoulsBetween(@Param("minPouls") Integer minPouls, 
                                         @Param("maxPouls") Integer maxPouls);
    
    @Query("SELECT s FROM Surveillance s WHERE LOWER(s.signesCliniques) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Surveillance> findBySignesCliniquesContaining(@Param("keyword") String keyword);
    
    @Query("SELECT s FROM Surveillance s WHERE LOWER(s.observations) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Surveillance> findByObservationsContaining(@Param("keyword") String keyword);
    
    @Query("SELECT s FROM Surveillance s WHERE s.tension LIKE :tensionPattern")
    List<Surveillance> findByTensionLike(@Param("tensionPattern") String tensionPattern);
    
    @Query("SELECT COUNT(s) FROM Surveillance s WHERE s.transfusion.id = :transfusionId")
    long countByTransfusionId(@Param("transfusionId") Long transfusionId);
    
    @Query("SELECT s FROM Surveillance s ORDER BY s.transfusion.id, s.heure")
    List<Surveillance> findAllOrderByTransfusionAndHeure();
}