package com.hematrace.hematrace.repository;

import com.hematrace.hematrace.entite.ProduitSanguin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface ProduitSanguinRepository extends JpaRepository<ProduitSanguin, Long>, JpaSpecificationExecutor<ProduitSanguin> {
    
    ProduitSanguin findByCodeProduit(String codeProduit);

    List<ProduitSanguin> findByTypeProduit(String typeProduit);

    List<ProduitSanguin> findByEtat(String etat);

    long countByTypeProduit(String typeProduit);

    // Produits encore disponibles (pas attribués et non périmés)
    @Query("SELECT ps FROM ProduitSanguin ps WHERE ps.delivrance IS NULL AND ps.datePeremption > CURRENT_DATE")
    List<ProduitSanguin> findDisponibles();

    // Vérifier disponibilité d’un produit par son ID
    @Query("SELECT ps FROM ProduitSanguin ps WHERE ps.id = :id AND ps.delivrance IS NULL AND ps.datePeremption > CURRENT_DATE")
    ProduitSanguin findByIdAndDisponible(Long id);

    // Compter les produits disponibles
    @Query("SELECT COUNT(ps) FROM ProduitSanguin ps WHERE ps.delivrance IS NULL AND ps.datePeremption > CURRENT_DATE")
    long countProduitsDisponibles();

    // Produits proches de la péremption
    List<ProduitSanguin> findByDatePeremptionBefore(LocalDate date);

    ProduitSanguin findByCodeProduitIgnoreCase(String codeProduit);

    List<ProduitSanguin> findByGroupeSanguin(String groupeSanguin);
    List<ProduitSanguin> findByRhesus(String rhesus);
    long countByGroupeSanguin(String groupeSanguin);
    long countByEtat(String etat);

    
    
    // Recherche combinée groupe + type
    @Query("SELECT ps FROM ProduitSanguin ps WHERE ps.groupeSanguin = :groupeSanguin AND ps.typeProduit = :typeProduit")
    List<ProduitSanguin> findByGroupeSanguinAndTypeProduit(@Param("groupeSanguin") String groupeSanguin, 
                                                        @Param("typeProduit") String typeProduit);
}
