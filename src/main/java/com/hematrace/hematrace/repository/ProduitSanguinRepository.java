package com.hematrace.hematrace.repository;

import com.hematrace.hematrace.entite.ProduitSanguin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface ProduitSanguinRepository extends JpaRepository<ProduitSanguin, Long>, JpaSpecificationExecutor<ProduitSanguin> {
    
    ProduitSanguin findByCodeProduit(String codeProduit);

    List<ProduitSanguin> findByTypeProduit(String typeProduit);

    List<ProduitSanguin> findByEtat(String etat);

    long countByTypeProduit(String typeProduit);

    @Query("SELECT ps FROM ProduitSanguin ps WHERE ps.delivrance IS NULL AND ps.datePeremption > CURRENT_DATE AND UPPER(ps.etat) = 'DISPONIBLE'")
    List<ProduitSanguin> findDisponibles();

    @Query("SELECT ps FROM ProduitSanguin ps WHERE ps.id = :id AND ps.delivrance IS NULL AND ps.datePeremption > CURRENT_DATE")
    ProduitSanguin findByIdAndDisponible(Long id);

    @Query("SELECT COUNT(ps) FROM ProduitSanguin ps WHERE ps.delivrance IS NULL AND ps.datePeremption > CURRENT_DATE")
    long countProduitsDisponibles();

    List<ProduitSanguin> findByDatePeremptionBefore(LocalDate date);

    List<ProduitSanguin> findByDatePeremptionLessThanEqual(LocalDate date);

    ProduitSanguin findByCodeProduitIgnoreCase(String codeProduit);

    List<ProduitSanguin> findByGroupeSanguin(String groupeSanguin);
    List<ProduitSanguin> findByRhesus(String rhesus);
    long countByGroupeSanguin(String groupeSanguin);
    long countByEtat(String etat);

    @Query("SELECT ps FROM ProduitSanguin ps WHERE ps.groupeSanguin = :groupeSanguin AND ps.typeProduit = :typeProduit")
    List<ProduitSanguin> findByGroupeSanguinAndTypeProduit(@Param("groupeSanguin") String groupeSanguin,
                                                           @Param("typeProduit") String typeProduit);



long countByEtatIgnoreCase(String etat);

@Query("""
SELECT COUNT(ps) FROM ProduitSanguin ps
WHERE ps.datePeremption < :today
AND UPPER(ps.etat) <> 'UTILISÉ'
""")
long countExpired(@Param("today") LocalDate today);

@Query("""
SELECT COUNT(ps) FROM ProduitSanguin ps
WHERE ps.datePeremption BETWEEN :today AND :limitDate
AND UPPER(ps.etat) <> 'UTILISÉ'
""")
long countExpiringSoon(@Param("today") LocalDate today, @Param("limitDate") LocalDate limitDate);

@Query("""
SELECT ps.groupeSanguin, ps.rhesus, COUNT(ps)
FROM ProduitSanguin ps
GROUP BY ps.groupeSanguin, ps.rhesus
""")
List<Object[]> countByGroupeAndRhesus();

@Query("""
SELECT ps.typeProduit, COUNT(ps)
FROM ProduitSanguin ps
GROUP BY ps.typeProduit
""")
List<Object[]> countByTypeProduitGlobal();

@Query("""
SELECT COUNT(ps) FROM ProduitSanguin ps
WHERE ps.delivrance IS NOT NULL
AND UPPER(ps.etat) <> 'UTILISÉ'
AND UPPER(ps.etat) <> 'EXPIRÉ'
""")
long countProduitsDelivres();
}