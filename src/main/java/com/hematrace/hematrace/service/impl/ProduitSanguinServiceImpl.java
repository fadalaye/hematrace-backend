package com.hematrace.hematrace.service.impl;

import com.hematrace.hematrace.entite.ProduitSanguin;
import com.hematrace.hematrace.entite.Delivrance;
import com.hematrace.hematrace.repository.ProduitSanguinRepository;
import com.hematrace.hematrace.repository.DelivranceRepository;
import com.hematrace.hematrace.service.ProduitSanguinService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ProduitSanguinServiceImpl implements ProduitSanguinService {

    private final ProduitSanguinRepository produitSanguinRepository;
    private final DelivranceRepository delivranceRepository;

    @Override
    public ProduitSanguin ajouterProduitAuStock(ProduitSanguin produitSanguin) {
        // Validation des dates
        if (produitSanguin.getDatePrelevement() == null) {
            produitSanguin.setDatePrelevement(LocalDate.now());
        }
        
        if (produitSanguin.getDatePeremption() == null) {
            // Date de péremption par défaut selon le type de produit
            produitSanguin.setDatePeremption(calculerDatePeremption(produitSanguin.getTypeProduit()));
        }
        
        if (produitSanguin.getDatePeremption().isBefore(produitSanguin.getDatePrelevement())) {
            throw new RuntimeException("La date de péremption doit être après la date de prélèvement");
        }
        
        // Validation du code produit unique
        if (produitSanguin.getCodeProduit() != null) {
            ProduitSanguin existing = produitSanguinRepository.findByCodeProduit(produitSanguin.getCodeProduit());
            if (existing != null) {
                throw new RuntimeException("Un produit avec ce code existe déjà");
            }
        }
        
        // État par défaut
        if (produitSanguin.getEtat() == null) {
            produitSanguin.setEtat("disponible");
        }
        
        return produitSanguinRepository.save(produitSanguin);
    }

    @Override
    public List<ProduitSanguin> ajouterProduitsEnLot(List<ProduitSanguin> produits) {
        List<ProduitSanguin> produitsSauvegardes = new ArrayList<>();
        
        for (ProduitSanguin produit : produits) {
            try {
                ProduitSanguin saved = ajouterProduitAuStock(produit);
                produitsSauvegardes.add(saved);
            } catch (Exception e) {
                // Logger l'erreur mais continuer avec les autres produits
                System.err.println("Erreur lors de l'ajout du produit: " + e.getMessage());
            }
        }
        
        return produitsSauvegardes;
    }

    @Override
    public List<ProduitSanguin> getAllProduitsSanguins() {
        return produitSanguinRepository.findAll();
    }

    @Override
    public Optional<ProduitSanguin> getProduitSanguinById(Long id) {
        return produitSanguinRepository.findById(id);
    }

    @Override
    public ProduitSanguin getProduitSanguinByCode(String codeProduit) {
        return produitSanguinRepository.findByCodeProduit(codeProduit);
    }

    @Override
    public ProduitSanguin updateProduitSanguin(Long id, ProduitSanguin produitSanguinDetails) {
        ProduitSanguin produit = produitSanguinRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Produit sanguin non trouvé avec l'id: " + id));
        
        // Ne pas permettre la modification si le produit est déjà attribué
        if (produit.getDelivrance() != null) {
            throw new RuntimeException("Impossible de modifier un produit déjà attribué à une délivrance");
        }
        
        // Mise à jour des champs modifiables
        if (produitSanguinDetails.getCodeProduit() != null) {
            // Vérifier l'unicité du nouveau code
            ProduitSanguin existing = produitSanguinRepository.findByCodeProduit(produitSanguinDetails.getCodeProduit());
            if (existing != null && !existing.getId().equals(id)) {
                throw new RuntimeException("Un autre produit avec ce code existe déjà");
            }
            produit.setCodeProduit(produitSanguinDetails.getCodeProduit());
        }
        if (produitSanguinDetails.getTypeProduit() != null) {
            produit.setTypeProduit(produitSanguinDetails.getTypeProduit());
        }
        if (produitSanguinDetails.getGroupeSanguin() != null) {
            produit.setGroupeSanguin(produitSanguinDetails.getGroupeSanguin());
        }
        if (produitSanguinDetails.getRhesus() != null) {
            produit.setRhesus(produitSanguinDetails.getRhesus());
        }
        if (produitSanguinDetails.getVolumeMl() != null) {
            produit.setVolumeMl(produitSanguinDetails.getVolumeMl());
        }
        if (produitSanguinDetails.getDatePrelevement() != null) {
            produit.setDatePrelevement(produitSanguinDetails.getDatePrelevement());
        }
        if (produitSanguinDetails.getDatePeremption() != null) {
            produit.setDatePeremption(produitSanguinDetails.getDatePeremption());
        }
        if (produitSanguinDetails.getEtat() != null) {
            produit.setEtat(produitSanguinDetails.getEtat());
        }
        
        return produitSanguinRepository.save(produit);
    }

    @Override
    public void updateEtatProduitSanguin(Long id, String nouvelEtat) {
        ProduitSanguin produit = produitSanguinRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Produit sanguin non trouvé"));
        
        produit.setEtat(nouvelEtat);
        produitSanguinRepository.save(produit);
    }

    @Override
    public void marquerCommeExpire(Long produitId) {
        ProduitSanguin produit = produitSanguinRepository.findById(produitId)
            .orElseThrow(() -> new RuntimeException("Produit sanguin non trouvé"));
        
        produit.setEtat("PÉRIMÉ"); // Changé de "EXPIRÉ" à "PÉRIMÉ"
        produitSanguinRepository.save(produit);
    }

    @Override
    public void marquerCommeUtilise(Long produitId) {
        ProduitSanguin produit = produitSanguinRepository.findById(produitId)
            .orElseThrow(() -> new RuntimeException("Produit sanguin non trouvé"));
        
        produit.setEtat("UTILISÉ");
        produitSanguinRepository.save(produit);
    }

    @Override
    public void verifierEtMarquerProduitsExpires() {
        LocalDate aujourdhui = LocalDate.now();
        List<ProduitSanguin> produitsExpires = produitSanguinRepository.findByDatePeremptionBefore(aujourdhui);
        
        produitsExpires.forEach(produit -> {
            if (!"PÉRIMÉ".equals(produit.getEtat())) { // Changé de "EXPIRÉ" à "PÉRIMÉ"
                produit.setEtat("PÉRIMÉ");
                produitSanguinRepository.save(produit);
            }
        });
    }

    @Override
    public void deleteProduitSanguin(Long id) {
        ProduitSanguin produit = produitSanguinRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Produit sanguin non trouvé"));
        
        if (produit.getDelivrance() != null) {
            throw new RuntimeException("Impossible de supprimer un produit associé à une délivrance");
        }
        
        produitSanguinRepository.deleteById(id);
    }

    @Override
    public List<ProduitSanguin> getProduitsSanguinsByType(String typeProduit) {
        return produitSanguinRepository.findByTypeProduit(typeProduit);
    }

    @Override
    public List<ProduitSanguin> getProduitsSanguinsByGroupeSanguin(String groupeSanguin) {
        // Implémentation avec filtre manuel puisque la méthode n'existe pas dans le repository
        return produitSanguinRepository.findAll().stream()
            .filter(p -> p.getGroupeSanguin().equalsIgnoreCase(groupeSanguin))
            .collect(Collectors.toList());
    }

    @Override
    public List<ProduitSanguin> getProduitsSanguinsByRhesus(String rhesus) {
        // Implémentation avec filtre manuel puisque la méthode n'existe pas dans le repository
        return produitSanguinRepository.findAll().stream()
            .filter(p -> p.getRhesus().equalsIgnoreCase(rhesus))
            .collect(Collectors.toList());
    }

    @Override
    public List<ProduitSanguin> getProduitsSanguinsByEtat(String etat) {
        return produitSanguinRepository.findByEtat(etat);
    }

    @Override
    public List<ProduitSanguin> getProduitsSanguinsDisponibles() {
        return produitSanguinRepository.findDisponibles();
    }

    @Override
    public List<ProduitSanguin> getProduitsSanguinsProchesPeremption(int joursRestants) {
        LocalDate dateLimite = LocalDate.now().plusDays(joursRestants);
        return produitSanguinRepository.findByDatePeremptionBefore(dateLimite);
    }

    @Override
    public List<ProduitSanguin> getProduitsSanguinsExpires() {
        return produitSanguinRepository.findByDatePeremptionBefore(LocalDate.now());
    }

    @Override
    public List<ProduitSanguin> getProduitsSanguinsByDelivrance(Long delivranceId) {
        Delivrance delivrance = delivranceRepository.findById(delivranceId)
            .orElseThrow(() -> new RuntimeException("Délivrance non trouvée"));
        return delivrance.getProduitsSanguins();
    }

    @Override
    public List<ProduitSanguin> getProduitsSanguinsByTransfusion(Long transfusionId) {
        // Implémentation à compléter quand l'entité Transfusion sera disponible
        return new ArrayList<>();
    }

    @Override
    public List<ProduitSanguin> getProduitsDisponiblesByGroupe(String groupeSanguin) {
        return getProduitsSanguinsDisponibles().stream()
            .filter(p -> p.getGroupeSanguin().equalsIgnoreCase(groupeSanguin))
            .collect(Collectors.toList());
    }

    @Override
    public List<ProduitSanguin> getProduitsDisponiblesByType(String typeProduit) {
        return getProduitsSanguinsDisponibles().stream()
            .filter(p -> p.getTypeProduit().equalsIgnoreCase(typeProduit))
            .collect(Collectors.toList());
    }

    @Override
    public List<ProduitSanguin> getProduitsDisponiblesByGroupeAndType(String groupeSanguin, String typeProduit) {
        return getProduitsSanguinsDisponibles().stream()
            .filter(p -> p.getGroupeSanguin().equalsIgnoreCase(groupeSanguin))
            .filter(p -> p.getTypeProduit().equalsIgnoreCase(typeProduit))
            .collect(Collectors.toList());
    }

    @Override
    public List<ProduitSanguin> getProduitsCompatibles(String groupeSanguinPatient, String typeProduit) {
        // Logique de compatibilité sanguine simplifiée
        return getProduitsDisponiblesByType(typeProduit).stream()
            .filter(p -> estCompatibleAvecPatient(p, groupeSanguinPatient))
            .collect(Collectors.toList());
    }

    @Override
    public boolean estCompatible(Long produitId, String groupeSanguinPatient) {
        ProduitSanguin produit = produitSanguinRepository.findById(produitId)
            .orElseThrow(() -> new RuntimeException("Produit sanguin non trouvé"));
        
        return estCompatibleAvecPatient(produit, groupeSanguinPatient);
    }

    @Override
    public boolean estUtilisable(Long produitId) {
        ProduitSanguin produit = produitSanguinRepository.findById(produitId)
            .orElseThrow(() -> new RuntimeException("Produit sanguin non trouvé"));
        
        return produit.getDelivrance() == null && 
            produit.getDatePeremption().isAfter(LocalDate.now()) &&
            "DISPONIBLE".equalsIgnoreCase(produit.getEtat()); // Vérifier "DISPONIBLE"
    }

    @Override
    public long countProduitsSanguinsByType(String typeProduit) {
        return produitSanguinRepository.countByTypeProduit(typeProduit);
    }

    @Override
    public long countProduitsSanguinsByGroupeSanguin(String groupeSanguin) {
        // Implémentation manuelle puisque la méthode n'existe pas dans le repository
        return produitSanguinRepository.findAll().stream()
            .filter(p -> p.getGroupeSanguin().equalsIgnoreCase(groupeSanguin))
            .count();
    }

    @Override
    public long countProduitsSanguinsDisponibles() {
        return produitSanguinRepository.countProduitsDisponibles();
    }

    @Override
    public Map<String, Long> getStatistiquesStock() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("TOTAL", produitSanguinRepository.count());
        stats.put("DISPONIBLES", countProduitsSanguinsDisponibles());
        
        // Compter les états manuellement
        List<ProduitSanguin> tousProduits = produitSanguinRepository.findAll();
        
        // Compter les produits délivrés (corrigé)
        stats.put("DÉLIVRÉS", tousProduits.stream()
            .filter(p -> "DÉLIVRÉ".equals(p.getEtat()))
            .count());
        
        // Compter les produits utilisés (corrigé)
        stats.put("UTILISÉS", tousProduits.stream()
            .filter(p -> "UTILISÉ".equals(p.getEtat()))
            .count());
        
        // Compter les produits périmés (corrigé)
        stats.put("PÉRIMÉS", tousProduits.stream()
            .filter(p -> "PÉRIMÉ".equals(p.getEtat()))
            .count());
            
        return stats;
    }

    @Override
    public Map<String, Long> getAlertesStock() {
        Map<String, Long> alertes = new HashMap<>();
        
        // Produits périmés (corrigé)
        alertes.put("PÉRIMÉS", (long) getProduitsSanguinsExpires().size());
        
        // Produits proches de la péremption (7 jours)
        alertes.put("PROCHES_PEREMPTION", (long) getProduitsSanguinsProchesPeremption(7).size());
        
        // Stocks critiques par type
        List<String> types = List.of("CGR", "PLS", "PFC", "CP");
        for (String type : types) {
            long count = getProduitsDisponiblesByType(type).size();
            if (count < 10) { // Seuil arbitraire
                alertes.put("CRITIQUE_" + type, count);
            }
        }
        
        return alertes;
    }

    // Méthodes privées utilitaires
    private LocalDate calculerDatePeremption(String typeProduit) {
        LocalDate aujourdhui = LocalDate.now();
        
        switch (typeProduit.toUpperCase()) {
            case "CGR": // Concentré de Globules Rouges
                return aujourdhui.plusDays(42);
            case "PLS": // Plasma
                return aujourdhui.plusYears(1);
            case "PFC": // Plasma Frais Congelé
                return aujourdhui.plusYears(1);
            case "CP":  // Concentré Plaquettaire
                return aujourdhui.plusDays(5);
            default:
                return aujourdhui.plusDays(30);
        }
    }

    private boolean estCompatibleAvecPatient(ProduitSanguin produit, String groupeSanguinPatient) {
    // Logique de compatibilité sanguine ABO et Rhésus
    String groupeProduit = produit.getGroupeSanguin();
    String rhesusProduit = produit.getRhesus();
    
    // Simplification - À adapter selon vos règles
    switch (groupeSanguinPatient) {
        case "A+":
            return groupeProduit.equals("A+") || groupeProduit.equals("A-") ||
                   groupeProduit.equals("O+") || groupeProduit.equals("O-");
        case "A-":
            return groupeProduit.equals("A-") || groupeProduit.equals("O-");
        case "B+":
            return groupeProduit.equals("B+") || groupeProduit.equals("B-") ||
                   groupeProduit.equals("O+") || groupeProduit.equals("O-");
        case "B-":
            return groupeProduit.equals("B-") || groupeProduit.equals("O-");
        case "AB+":
            return true; // Receveur universel
        case "AB-":
            return groupeProduit.equals("AB-") || groupeProduit.equals("A-") ||
                   groupeProduit.equals("B-") || groupeProduit.equals("O-");
        case "O+":
            return groupeProduit.equals("O+") || groupeProduit.equals("O-");
        case "O-":
            return groupeProduit.equals("O-");
        default:
            return false;
    }
}
}