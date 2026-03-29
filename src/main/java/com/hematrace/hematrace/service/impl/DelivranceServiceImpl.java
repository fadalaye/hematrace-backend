package com.hematrace.hematrace.service.impl;

import com.hematrace.hematrace.entite.Delivrance;
import com.hematrace.hematrace.entite.Demande;
import com.hematrace.hematrace.entite.Personnel;
import com.hematrace.hematrace.entite.ProduitSanguin;
import com.hematrace.hematrace.entite.Transfusion;
import com.hematrace.hematrace.repository.DelivranceRepository;
import com.hematrace.hematrace.repository.DemandeRepository;
import com.hematrace.hematrace.repository.PersonnelRepository;
import com.hematrace.hematrace.repository.ProduitSanguinRepository;
import com.hematrace.hematrace.service.DelivranceService;
import com.hematrace.hematrace.service.TransfusionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DelivranceServiceImpl implements DelivranceService {

    private final DelivranceRepository delivranceRepository;
    private final DemandeRepository demandeRepository;
    private final PersonnelRepository personnelRepository;
    private final ProduitSanguinRepository produitSanguinRepository;
    private final TransfusionService transfusionService;

    @Override
    public Delivrance creerDelivranceAvecProduits(Long demandeId, List<Long> produitIds, 
                                                 Long personnelId, String destination, 
                                                 String modeTransport, String observations) {
        
        // Logging de débogage
        System.out.println("🚀 Début création délivrance:");
        System.out.println("- Demande ID: " + demandeId);
        System.out.println("- Produit IDs: " + produitIds);
        System.out.println("- Personnel ID: " + personnelId);
        
        // Validation de la demande
        Demande demande = demandeRepository.findById(demandeId)
            .orElseThrow(() -> new RuntimeException("Demande non trouvée avec l'id: " + demandeId));
        
        System.out.println("- Patient: " + demande.getPatientPrenom() + " " + demande.getPatientNom());
        System.out.println("- Groupe sanguin patient: " + demande.getGroupeSanguinPatient());
        
        // Vérifier si la demande a déjà été délivrée
        if (delivranceRepository.existsByDemandeId(demandeId)) {
            throw new RuntimeException("Cette demande a déjà été délivrée");
        }
        
        if (!"VALIDÉE".equals(demande.getStatut())) {
            throw new RuntimeException("La demande doit être validée avant délivrance. Statut actuel: " + demande.getStatut());
        }
        
        // Validation du personnel
        Personnel personnel = personnelRepository.findById(personnelId)
            .orElseThrow(() -> new RuntimeException("Personnel non trouvé avec l'id: " + personnelId));
        
        // Validation des produits
        List<ProduitSanguin> produits = validerEtRecupererProduits(produitIds);
        
        // Vérifier la compatibilité des groupes sanguins
        verifierCompatibiliteGroupesSanguins(demande, produits);
        
        // Création de la délivrance
        Delivrance delivrance = Delivrance.builder()
            .demande(demande)
            .personnel(personnel)
            .dateHeureDelivrance(LocalDateTime.now())
            .destination(destination)
            .modeTransport(modeTransport)
            .observations(observations)
            .produitsSanguins(new ArrayList<>())
            .build();
        
        Delivrance savedDelivrance = delivranceRepository.save(delivrance);
        
        // Association des produits
        associerProduitsADelivrance(savedDelivrance, produits);
        
        // Mise à jour du statut de la demande
        demande.setStatut("DÉLIVRÉE");
        demandeRepository.save(demande);
        
        System.out.println("✅ Délivrance créée avec succès - ID: " + savedDelivrance.getId());
        
        return delivranceRepository.save(savedDelivrance);
    }

    private List<ProduitSanguin> validerEtRecupererProduits(List<Long> produitIds) {
        List<ProduitSanguin> produits = new ArrayList<>();
        for (Long produitId : produitIds) {
            ProduitSanguin produit = produitSanguinRepository.findById(produitId)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'id: " + produitId));
            
            // Logging détaillé du produit
            System.out.println("🔍 Inspection produit:");
            System.out.println("  - ID: " + produit.getId());
            System.out.println("  - Code: " + produit.getCodeProduit());
            System.out.println("  - Groupe: " + produit.getGroupeSanguin());
            System.out.println("  - Rhésus: " + produit.getRhesus());
            System.out.println("  - Groupe complet: " + getGroupeSanguinComplet(produit));
            System.out.println("  - État: " + produit.getEtat());
            System.out.println("  - Péremption: " + produit.getDatePeremption());
            System.out.println("  - Délivrance associée: " + (produit.getDelivrance() != null));
            
            if (produit.getDelivrance() != null) {
                throw new RuntimeException("Le produit " + produit.getCodeProduit() + " est déjà associé à une délivrance");
            }
            
            if (produit.getDatePeremption().isBefore(LocalDate.now())) {
                throw new RuntimeException("Le produit " + produit.getCodeProduit() + " est périmé");
            }
            
            if (!"DISPONIBLE".equalsIgnoreCase(produit.getEtat())) {
                throw new RuntimeException("Le produit " + produit.getCodeProduit() + 
                                        " n'est pas disponible. État: " + produit.getEtat());
            }
            
            produits.add(produit);
        }
        return produits;
    }

    private void verifierCompatibiliteGroupesSanguins(Demande demande, List<ProduitSanguin> produits) {
        String groupeSanguinPatient = demande.getGroupeSanguinPatient();
        
        System.out.println("🔍 Vérification compatibilité groupes sanguins:");
        System.out.println("- Groupe patient: " + groupeSanguinPatient);
        
        for (ProduitSanguin produit : produits) {
            String groupeProduitComplet = getGroupeSanguinComplet(produit);
            boolean estCompatible = estGroupeSanguinCompatible(groupeSanguinPatient, groupeProduitComplet);
            
            System.out.println("  - Produit " + produit.getCodeProduit() + ": " + groupeProduitComplet + " → " + (estCompatible ? "✅ COMPATIBLE" : "❌ INCOMPATIBLE"));
            
            if (!estCompatible) {
                throw new RuntimeException("Le produit " + produit.getCodeProduit() + " (groupe " + 
                    groupeProduitComplet + ") n'est pas compatible avec le patient (groupe " + 
                    groupeSanguinPatient + ")");
            }
        }
        
        System.out.println("✅ Tous les produits sont compatibles");
    }

    private String getGroupeSanguinComplet(ProduitSanguin produit) {
        String groupe = produit.getGroupeSanguin();
        String rhesus = produit.getRhesus();
        
        // Si le groupe contient déjà le Rhésus, le retourner tel quel
        if (groupe.contains("+") || groupe.contains("-")) {
            return groupe;
        }
        
        // Sinon, ajouter le Rhésus s'il existe
        if (rhesus != null && !rhesus.trim().isEmpty()) {
            return groupe + rhesus;
        }
        
        // Par défaut, considérer Rhésus positif
        return groupe + "+";
    }

    private boolean estGroupeSanguinCompatible(String groupePatient, String groupeProduit) {
        // Logique de compatibilité CORRIGÉE avec groupes complets
        Map<String, List<String>> compatibilite = Map.of(
            "O-", List.of("O-"),
            "O+", List.of("O+", "O-"),
            "A-", List.of("A-", "O-"),
            "A+", List.of("A+", "A-", "O+", "O-"),
            "B-", List.of("B-", "O-"),
            "B+", List.of("B+", "B-", "O+", "O-"),
            "AB-", List.of("AB-", "A-", "B-", "O-"),
            "AB+", List.of("AB+", "AB-", "A+", "A-", "B+", "B-", "O+", "O-")
        );
        
        // Normaliser les groupes (supprimer les espaces, mettre en majuscule)
        groupePatient = groupePatient != null ? groupePatient.trim().toUpperCase() : "";
        groupeProduit = groupeProduit != null ? groupeProduit.trim().toUpperCase() : "";
        
        System.out.println("  🔍 Comparaison: " + groupePatient + " ← " + groupeProduit);
        System.out.println("  📋 Compatibles: " + compatibilite.get(groupePatient));
        
        boolean estCompatible = compatibilite.getOrDefault(groupePatient, List.of()).contains(groupeProduit);
        System.out.println("  📊 Résultat: " + estCompatible);
        
        return estCompatible;
    }

private void associerProduitsADelivrance(Delivrance delivrance, List<ProduitSanguin> produits) {
    produits.forEach(produit -> {
        produit.setDelivrance(delivrance);
        produit.setEtat("DÉLIVRÉ");  // Changé de "UTILISÉ" à "DÉLIVRÉ"
        produitSanguinRepository.save(produit);
        delivrance.getProduitsSanguins().add(produit);
        
        System.out.println("🔗 Produit associé: " + produit.getCodeProduit() + 
                         " → Délivrance " + delivrance.getId() + 
                         " → État: DÉLIVRÉ");
    });
}

    @Override
    public List<Delivrance> getAllDelivrances() {
        return delivranceRepository.findAll();
    }

    @Override
    public List<Delivrance> getAllDelivrancesWithDetails() {
        return delivranceRepository.findAllWithDetails();
    }

    @Override
    public Optional<Delivrance> getDelivranceById(Long id) {
        return delivranceRepository.findById(id);
    }

    @Override
    public Optional<Delivrance> getDelivranceByIdWithDetails(Long id) {
        return delivranceRepository.findByIdWithDetails(id);
    }

    @Override
    public Delivrance updateDelivrance(Long id, Delivrance delivranceDetails) {
        Delivrance delivrance = delivranceRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Délivrance non trouvée avec l'id: " + id));
        
        if (delivranceDetails.getDestination() != null) {
            delivrance.setDestination(delivranceDetails.getDestination());
        }
        if (delivranceDetails.getModeTransport() != null) {
            delivrance.setModeTransport(delivranceDetails.getModeTransport());
        }
        if (delivranceDetails.getObservations() != null) {
            delivrance.setObservations(delivranceDetails.getObservations());
        }
        
        return delivranceRepository.save(delivrance);
    }

    @Override
    public void deleteDelivrance(Long id) {
        Delivrance delivrance = delivranceRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Délivrance non trouvée"));
        
        libererProduitsAssocies(delivrance);
        reinitialiserStatutDemande(delivrance);
        
        delivranceRepository.delete(delivrance);
        
        System.out.println("🗑️ Délivrance supprimée: " + id);
    }

    @Override
    public void annulerDelivrance(Long delivranceId) {
        Delivrance delivrance = delivranceRepository.findById(delivranceId)
            .orElseThrow(() -> new RuntimeException("Délivrance non trouvée"));
        
        libererProduitsAssocies(delivrance);
        reinitialiserStatutDemande(delivrance);
        
        delivrance.getProduitsSanguins().clear();
        delivranceRepository.save(delivrance);
        
        System.out.println("❌ Délivrance annulée: " + delivranceId);
    }

private void libererProduitsAssocies(Delivrance delivrance) {
    delivrance.getProduitsSanguins().forEach(produit -> {
        produit.setDelivrance(null);
        produit.setEtat("DISPONIBLE"); // Retour à DISPONIBLE lorsqu'annulé
        produitSanguinRepository.save(produit);
        System.out.println("🔄 Produit libéré: " + produit.getCodeProduit() + 
                         " → État: DISPONIBLE");
    });
}

    private void reinitialiserStatutDemande(Delivrance delivrance) {
        if (delivrance.getDemande() != null) {
            delivrance.getDemande().setStatut("VALIDÉE");
            demandeRepository.save(delivrance.getDemande());
            System.out.println("🔄 Statut demande réinitialisé: " + delivrance.getDemande().getId());
        }
    }

    @Override
    public List<Delivrance> getDelivrancesByPersonnel(Long personnelId) {
        return delivranceRepository.findByPersonnelId(personnelId);
    }

    @Override
    public Optional<Delivrance> getDelivranceByDemande(Long demandeId) {
        return delivranceRepository.findByDemandeId(demandeId);
    }

    @Override
    public List<Delivrance> getDelivrancesByDate(LocalDate date) {
        return delivranceRepository.findByDate(date);
    }

    @Override
    public List<Delivrance> getDelivrancesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return delivranceRepository.findByDateHeureDelivranceBetween(startDate, endDate);
    }

    @Override
    public List<Delivrance> getDelivrancesByTypeProduit(String typeProduit) {
        return delivranceRepository.findAll().stream()
            .filter(d -> d.getProduitsSanguins().stream()
                .anyMatch(p -> p.getTypeProduit().equalsIgnoreCase(typeProduit)))
            .collect(Collectors.toList());
    }

    @Override
    public List<Delivrance> getDelivrancesByGroupeSanguin(String groupeSanguin) {
        return delivranceRepository.findAll().stream()
            .filter(d -> d.getProduitsSanguins().stream()
                .anyMatch(p -> getGroupeSanguinComplet(p).equalsIgnoreCase(groupeSanguin)))
            .collect(Collectors.toList());
    }

    @Override
    public List<Delivrance> getDelivrancesByDestination(String destination) {
        return delivranceRepository.findByDestinationContainingIgnoreCase(destination);
    }

    @Override
    public List<Delivrance> getDelivrancesByProduitSanguin(Long produitId) {
        return delivranceRepository.findByProduitSanguinId(produitId);
    }

    @Override
    public boolean peutDelivrerDemande(Long demandeId) {
        try {
            Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée"));
            
            if (delivranceRepository.existsByDemandeId(demandeId)) {
                throw new RuntimeException("Demande déjà délivrée");
            }
            
            if (!"VALIDÉE".equals(demande.getStatut())) {
                throw new RuntimeException("Demande non validée");
            }
            
            return true;
        } catch (RuntimeException e) {
            System.out.println("❌ Ne peut pas délivrer demande " + demandeId + ": " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean sontProduitsDisponibles(List<Long> produitIds) {
        for (Long produitId : produitIds) {
            Optional<ProduitSanguin> produit = produitSanguinRepository.findById(produitId);
            if (produit.isEmpty() || 
                !"DISPONIBLE".equalsIgnoreCase(produit.get().getEtat()) ||
                produit.get().getDatePeremption().isBefore(LocalDate.now())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean estDemandeValidee(Long demandeId) {
        Optional<Demande> demande = demandeRepository.findById(demandeId);
        return demande.isPresent() && "VALIDÉE".equals(demande.get().getStatut());
    }

    @Override
    public List<Delivrance> getDelivrancesProchesPeremption(int joursRestants) {
        LocalDate dateLimite = LocalDate.now().plusDays(joursRestants);
        return delivranceRepository.findAll().stream()
            .filter(d -> d.getProduitsSanguins().stream()
                .anyMatch(p -> !p.getDatePeremption().isAfter(dateLimite)))
            .collect(Collectors.toList());
    }

    @Override
public void ajouterProduitADelivrance(Long delivranceId, Long produitId) {
    Delivrance delivrance = delivranceRepository.findById(delivranceId)
        .orElseThrow(() -> new RuntimeException("Délivrance non trouvée"));
    
    ProduitSanguin produit = produitSanguinRepository.findById(produitId)
        .orElseThrow(() -> new RuntimeException("Produit non trouvé"));
    
    if (produit.getDelivrance() != null) {
        throw new RuntimeException("Le produit est déjà associé à une délivrance");
    }
    
    produit.setDelivrance(delivrance);
    produit.setEtat("DÉLIVRÉ");  // Changé de "UTILISÉ" à "DÉLIVRÉ"
    produitSanguinRepository.save(produit);
    
    delivrance.getProduitsSanguins().add(produit);
    delivranceRepository.save(delivrance);
    
    System.out.println("➕ Produit ajouté à délivrance: " + produitId + 
                     " → " + delivranceId + " → État: DÉLIVRÉ");
}
@Override
public void retirerProduitDeDelivrance(Long delivranceId, Long produitId) {
    Delivrance delivrance = delivranceRepository.findById(delivranceId)
        .orElseThrow(() -> new RuntimeException("Délivrance non trouvée"));
    
    ProduitSanguin produit = produitSanguinRepository.findById(produitId)
        .orElseThrow(() -> new RuntimeException("Produit non trouvé"));
    
    if (!delivrance.getProduitsSanguins().contains(produit)) {
        throw new RuntimeException("Le produit n'est pas associé à cette délivrance");
    }
    
    produit.setDelivrance(null);
    produit.setEtat("DISPONIBLE"); // Retour à DISPONIBLE
    produitSanguinRepository.save(produit);
    
    delivrance.getProduitsSanguins().remove(produit);
    delivranceRepository.save(delivrance);
    
    System.out.println("➖ Produit retiré de délivrance: " + produitId + 
                     " ← " + delivranceId + " → État: DISPONIBLE");
}
    @Override
    public long countDelivrancesByTypeProduit(String typeProduit) {
        return delivranceRepository.countByProduitsSanguinsTypeProduit(typeProduit);
    }

    @Override
    public long countDelivrancesByPersonnel(Long personnelId) {
        return delivranceRepository.countByPersonnelId(personnelId);
    }

    @Override
    public Map<String, Long> getStatistiquesDelivrances() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", delivranceRepository.count());
        
        // Statistiques par type de produit
        List<String> typesProduits = List.of("SANG_TOTAL", "PLASMA", "PLAQUETTES", "GLOBULES_ROUGES");
        typesProduits.forEach(type -> {
            stats.put("type_" + type, countDelivrancesByTypeProduit(type));
        });
        
        return stats;
    }

    @Override
    public Map<String, Long> getDelivrancesParMois(int annee) {
        Map<String, Long> stats = new HashMap<>();
        List<Delivrance> delivrancesAnnee = delivranceRepository.findAll().stream()
            .filter(d -> d.getDateHeureDelivrance().getYear() == annee)
            .collect(Collectors.toList());
        
        for (int mois = 1; mois <= 12; mois++) {
            final int moisFinal = mois;
            long count = delivrancesAnnee.stream()
                .filter(d -> d.getDateHeureDelivrance().getMonthValue() == moisFinal)
                .count();
            stats.put(String.valueOf(mois), count);
        }
        
        return stats;
    }

    @Override
    public long getTotalDelivrances() {
        return delivranceRepository.count();
    }

    // Dans DelivranceServiceImpl.java

@Override
public List<Delivrance> getAllDelivrancesWithAvailableProducts() {
    List<Delivrance> delivrances = delivranceRepository.findAllWithDetails();
    
    return delivrances.stream()
        .map(this::filtrerProduitsDejaTransfuses)
        .filter(delivrance -> !delivrance.getProduitsSanguins().isEmpty())
        .collect(Collectors.toList());
}

@Override
public Optional<Delivrance> getDelivranceByIdWithAvailableProducts(Long id) {
    Optional<Delivrance> delivranceOpt = delivranceRepository.findByIdWithDetails(id);
    
    if (delivranceOpt.isPresent()) {
        Delivrance delivranceFiltree = filtrerProduitsDejaTransfuses(delivranceOpt.get());
        return Optional.of(delivranceFiltree);
    }
    
    return Optional.empty();
}

@Override
public boolean estProduitDisponible(Long produitId) {
    try {
        // Vérifier d'abord l'état du produit dans la base
        Optional<ProduitSanguin> produitOpt = produitSanguinRepository.findById(produitId);
        if (produitOpt.isEmpty()) {
            return false;
        }
        
        ProduitSanguin produit = produitOpt.get();
        
        // Le produit doit être dans un état "DÉLIVRÉ" ou "DISPONIBLE"
        boolean etatValide = "DÉLIVRÉ".equalsIgnoreCase(produit.getEtat()) 
                          || "DISPONIBLE".equalsIgnoreCase(produit.getEtat());
        
        if (!etatValide) {
            return false;
        }
        
        // Vérifier si le produit a déjà été transfusé
        List<Transfusion> transfusions = transfusionService.getTransfusionsByProduitSanguin(produitId);
        boolean dejaTransfuse = !transfusions.isEmpty();
        
        // Le produit est disponible s'il n'a pas été transfusé
        return !dejaTransfuse;
        
    } catch (Exception e) {
        System.err.println("Erreur vérification produit disponible " + produitId + ": " + e.getMessage());
        return false;
    }
}

/**
 * Filtre les produits qui ont déjà été transfusés
 */
private Delivrance filtrerProduitsDejaTransfuses(Delivrance delivrance) {
    List<ProduitSanguin> produitsDisponibles = new ArrayList<>();
    
    for (ProduitSanguin produit : delivrance.getProduitsSanguins()) {
        if (estProduitDisponible(produit.getId())) {
            produitsDisponibles.add(produit);
        }
    }
    
    // Créer une copie de la délivrance avec seulement les produits disponibles
    Delivrance delivranceFiltree = new Delivrance();
    delivranceFiltree.setId(delivrance.getId());
    delivranceFiltree.setDateHeureDelivrance(delivrance.getDateHeureDelivrance());
    delivranceFiltree.setDestination(delivrance.getDestination());
    delivranceFiltree.setModeTransport(delivrance.getModeTransport());
    delivranceFiltree.setObservations(delivrance.getObservations());
    delivranceFiltree.setPersonnel(delivrance.getPersonnel());
    delivranceFiltree.setDemande(delivrance.getDemande());
    delivranceFiltree.setProduitsSanguins(produitsDisponibles);
    
    return delivranceFiltree;
}

/**
 * Méthode simplifiée pour vérifier si un produit est déjà utilisé
 */
private boolean estProduitDejaTransfuse(Long produitId) {
    try {
        List<Transfusion> transfusions = transfusionService.getTransfusionsByProduitSanguin(produitId);
        return !transfusions.isEmpty();
    } catch (Exception e) {
        // En cas d'erreur, on considère que le produit n'est pas transfusé
        System.err.println("Erreur vérification transfusions pour produit " + produitId + ": " + e.getMessage());
        return false;
    }
}

// Version alternative utilisant l'état du produit
private boolean estProduitDisponibleParEtat(ProduitSanguin produit) {
    String etat = produit.getEtat();
    return "DÉLIVRÉ".equalsIgnoreCase(etat) 
        || "DISPONIBLE".equalsIgnoreCase(etat);
}
}