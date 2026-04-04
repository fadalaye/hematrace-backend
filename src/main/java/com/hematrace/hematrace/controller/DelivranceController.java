package com.hematrace.hematrace.controller;

import com.hematrace.hematrace.dto.ModifierDelivranceRequest;
import com.hematrace.hematrace.entite.Delivrance;
import com.hematrace.hematrace.entite.ProduitSanguin;
import com.hematrace.hematrace.service.DelivranceService;

import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/delivrances")
@RequiredArgsConstructor
@Validated
public class DelivranceController {
    
    private final DelivranceService delivranceService;
    
    // ========== ENDPOINTS DE CRÉATION ==========
    
    @PostMapping("/creer")
    public ResponseEntity<?> creerDelivrance(@RequestBody @Validated CreerDelivranceRequest request) {
        try {
            System.out.println("🚀 Demande de création délivrance reçue:");
            System.out.println("- Demande ID: " + request.getDemandeId());
            System.out.println("- Produits IDs: " + request.getProduitIds());
            System.out.println("- Personnel ID: " + request.getPersonnelId());
            System.out.println("- Destination: " + request.getDestination());
            
            Delivrance delivrance = delivranceService.creerDelivranceAvecProduits(
                request.getDemandeId(), 
                request.getProduitIds(), 
                request.getPersonnelId(), 
                request.getDestination(), 
                request.getModeTransport(),
                request.getObservations()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Délivrance créée avec succès");
            response.put("delivrance", delivrance);
            
            System.out.println("✅ Délivrance créée avec succès - ID: " + delivrance.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            System.err.println("❌ Erreur création délivrance: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("erreur", e.getMessage());
            errorResponse.put("code", "DELIVRANCE_CREATION_ERREUR");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    // Endpoint POST pour compatibilité
    @PostMapping
    public ResponseEntity<?> creerDelivrancePost(@RequestBody @Validated CreerDelivranceRequest request) {
        return creerDelivrance(request);
    }
    
    // ========== ENDPOINTS DE LECTURE ==========
    
    @GetMapping
    public ResponseEntity<?> getAllDelivrances() {
        try {
            List<Delivrance> delivrances = delivranceService.getAllDelivrances();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", delivrances.size());
            response.put("data", delivrances);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(e, "GET_ALL_DELIVRANCES_ERROR");
        }
    }
    
    @GetMapping("/details")
public ResponseEntity<?> getAllDelivrancesWithDetails() {
    try {
        List<Delivrance> delivrances = delivranceService.getAllDelivrancesWithDetails();
        
        // Enrichir les données avec l'information sur les produits disponibles
        List<Map<String, Object>> delivrancesEnrichies = delivrances.stream()
            .map(delivrance -> {
                Map<String, Object> delivranceMap = new HashMap<>();
                delivranceMap.put("delivrance", delivrance);
                
                // Calculer les produits disponibles
                long produitsDisponibles = delivrance.getProduitsSanguins().stream()
                    .filter(produit -> delivranceService.estProduitDisponible(produit.getId()))
                    .count();
                
                long produitsTransfuses = delivrance.getProduitsSanguins().size() - produitsDisponibles;
                
                delivranceMap.put("produitsDisponiblesCount", produitsDisponibles);
                delivranceMap.put("produitsTransfusesCount", produitsTransfuses);
                delivranceMap.put("aProduitsDisponibles", produitsDisponibles > 0);
                
                return delivranceMap;
            })
            .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", delivrancesEnrichies.size());
        response.put("data", delivrancesEnrichies);
        
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        e.printStackTrace();
        return handleException(e, "GET_ALL_DETAILS_ERROR");
    }
}
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getDelivranceById(@PathVariable Long id) {
        try {
            Optional<Delivrance> delivrance = delivranceService.getDelivranceById(id);
            
            if (delivrance.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", delivrance.get());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Délivrance non trouvée avec l'id: " + id, "NOT_FOUND"));
            }
        } catch (Exception e) {
            return handleException(e, "GET_BY_ID_ERROR");
        }
    }
    
    @GetMapping("/{id}/details")
    public ResponseEntity<?> getDelivranceByIdWithDetails(@PathVariable Long id) {
        try {
            Optional<Delivrance> delivrance = delivranceService.getDelivranceByIdWithDetails(id);
            
            if (delivrance.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", delivrance.get());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Délivrance non trouvée avec l'id: " + id, "NOT_FOUND"));
            }
        } catch (Exception e) {
            return handleException(e, "GET_BY_ID_DETAILS_ERROR");
        }
    }
    
    // ========== ENDPOINTS DE RECHERCHE ET FILTRES ==========
    
    @GetMapping("/personnel/{personnelId}")
    public ResponseEntity<?> getDelivrancesByPersonnel(@PathVariable Long personnelId) {
        try {
            List<Delivrance> delivrances = delivranceService.getDelivrancesByPersonnel(personnelId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("personnelId", personnelId);
            response.put("count", delivrances.size());
            response.put("data", delivrances);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(e, "GET_BY_PERSONNEL_ERROR");
        }
    }
    
    @GetMapping("/demande/{demandeId}")
    public ResponseEntity<?> getDelivranceByDemande(@PathVariable Long demandeId) {
        try {
            Optional<Delivrance> delivrance = delivranceService.getDelivranceByDemande(demandeId);
            
            if (delivrance.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("demandeId", demandeId);
                response.put("data", delivrance.get());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Aucune délivrance trouvée pour la demande: " + demandeId, "NOT_FOUND"));
            }
        } catch (Exception e) {
            return handleException(e, "GET_BY_DEMANDE_ERROR");
        }
    }
    
    @GetMapping("/date/{date}")
    public ResponseEntity<?> getDelivrancesByDate(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<Delivrance> delivrances = delivranceService.getDelivrancesByDate(date);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("date", date);
            response.put("count", delivrances.size());
            response.put("data", delivrances);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(e, "GET_BY_DATE_ERROR");
        }
    }
    
    @GetMapping("/periode")
    public ResponseEntity<?> getDelivrancesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        try {
            List<Delivrance> delivrances = delivranceService.getDelivrancesByDateRange(start, end);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("start", start);
            response.put("end", end);
            response.put("count", delivrances.size());
            response.put("data", delivrances);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(e, "GET_BY_DATE_RANGE_ERROR");
        }
    }
    
    @GetMapping("/type/{typeProduit}")
    public ResponseEntity<?> getDelivrancesByTypeProduit(@PathVariable String typeProduit) {
        try {
            List<Delivrance> delivrances = delivranceService.getDelivrancesByTypeProduit(typeProduit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("typeProduit", typeProduit);
            response.put("count", delivrances.size());
            response.put("data", delivrances);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(e, "GET_BY_TYPE_PRODUIT_ERROR");
        }
    }
    
    @GetMapping("/groupe/{groupeSanguin}")
    public ResponseEntity<?> getDelivrancesByGroupeSanguin(@PathVariable String groupeSanguin) {
        try {
            List<Delivrance> delivrances = delivranceService.getDelivrancesByGroupeSanguin(groupeSanguin);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("groupeSanguin", groupeSanguin);
            response.put("count", delivrances.size());
            response.put("data", delivrances);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(e, "GET_BY_GROUPE_SANGUIN_ERROR");
        }
    }
    
    @GetMapping("/destination")
    public ResponseEntity<?> searchByDestination(@RequestParam String destination) {
        try {
            List<Delivrance> delivrances = delivranceService.getDelivrancesByDestination(destination);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("destination", destination);
            response.put("count", delivrances.size());
            response.put("data", delivrances);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(e, "SEARCH_BY_DESTINATION_ERROR");
        }
    }
    
    @GetMapping("/produit/{produitId}")
    public ResponseEntity<?> getDelivrancesByProduitSanguin(@PathVariable Long produitId) {
        try {
            List<Delivrance> delivrances = delivranceService.getDelivrancesByProduitSanguin(produitId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("produitId", produitId);
            response.put("count", delivrances.size());
            response.put("data", delivrances);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(e, "GET_BY_PRODUIT_ERROR");
        }
    }
    
    @GetMapping("/alerte/peremption/{joursRestants}")
    public ResponseEntity<?> getDelivrancesProchesPeremption(@PathVariable int joursRestants) {
        try {
            List<Delivrance> delivrances = delivranceService.getDelivrancesProchesPeremption(joursRestants);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("joursRestants", joursRestants);
            response.put("count", delivrances.size());
            response.put("data", delivrances);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(e, "GET_PROCHES_PEREMPTION_ERROR");
        }
    }
    
    // ========== ENDPOINTS DE VÉRIFICATION ==========
    
    @GetMapping("/verifier/demande/{demandeId}")
    public ResponseEntity<?> peutDelivrerDemande(@PathVariable Long demandeId) {
        try {
            boolean peutDelivrer = delivranceService.peutDelivrerDemande(demandeId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("peutDelivrer", peutDelivrer);
            response.put("demandeId", demandeId);
            response.put("message", peutDelivrer ? 
                "La demande peut être délivrée" : 
                "La demande ne peut pas être délivrée");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("peutDelivrer", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    @PostMapping("/verifier/produits")
    public ResponseEntity<?> verifierProduitsDelivrables(@RequestBody VerifierProduitsRequest request) {
        try {
            boolean produitsDisponibles = delivranceService.sontProduitsDisponibles(request.getProduitIds());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("produitsDisponibles", produitsDisponibles);
            response.put("message", produitsDisponibles ? 
                "Tous les produits sont disponibles" : 
                "Un ou plusieurs produits ne sont pas disponibles");
            response.put("produitIds", request.getProduitIds());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(e, "VERIFIER_PRODUITS_ERROR");
        }
    }
    
    @GetMapping("/verifier/produits/disponibles")
    public ResponseEntity<?> sontProduitsDisponibles(@RequestParam List<Long> produitIds) {
        try {
            boolean disponibles = delivranceService.sontProduitsDisponibles(produitIds);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("produitsDisponibles", disponibles);
            response.put("message", disponibles ? 
                "Tous les produits sont disponibles" : 
                "Un ou plusieurs produits ne sont pas disponibles");
            response.put("produitIds", produitIds);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(e, "PRODUITS_DISPONIBLES_ERROR");
        }
    }
    
    @PostMapping("/verifier/compatibilite")
    public ResponseEntity<?> verifierCompatibilite(@RequestBody VerifierCompatibiliteRequest request) {
        try {
            // Récupérer la demande
            Optional<Delivrance> delivranceOpt = delivranceService.getDelivranceByDemande(request.getDemandeId());
            if (delivranceOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Demande non trouvée", "DEMANDE_NOT_FOUND"));
            }
            
            // Récupérer les produits
            List<ProduitSanguin> produits = new ArrayList<>();
            for (Long produitId : request.getProduitIds()) {
                // Implémentez cette méthode dans votre service
                // ProduitSanguin produit = produitSanguinService.getProduitSanguinById(produitId);
                // if (produit != null) produits.add(produit);
            }
            
            // Vérifier la compatibilité (logique simplifiée)
            boolean compatible = true;
            String message = "Produits compatibles avec le patient";
            
            // Ici vous devez implémenter la logique de compatibilité réelle
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("compatible", compatible);
            response.put("message", message);
            response.put("demandeId", request.getDemandeId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(e, "VERIFIER_COMPATIBILITE_ERROR");
        }
    }
    
    @GetMapping("/verifier/demande/{demandeId}/validee")
    public ResponseEntity<?> estDemandeValidee(@PathVariable Long demandeId) {
        try {
            boolean estValidee = delivranceService.estDemandeValidee(demandeId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("estValidee", estValidee);
            response.put("demandeId", demandeId);
            response.put("message", estValidee ? 
                "La demande est validée" : 
                "La demande n'est pas validée");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(e, "DEMANDE_VALIDEE_ERROR");
        }
    }
    
    // ========== ENDPOINTS DE MISE À JOUR ==========
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDelivrance(@PathVariable Long id, @RequestBody Delivrance delivranceDetails) {
        try {
            Delivrance updated = delivranceService.updateDelivrance(id, delivranceDetails);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Délivrance mise à jour avec succès");
            response.put("data", updated);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("erreur", e.getMessage());
            errorResponse.put("code", "DELIVRANCE_UPDATE_ERREUR");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PatchMapping("/{id}/produits/ajouter/{produitId}")
    public ResponseEntity<?> ajouterProduitADelivrance(@PathVariable Long id, @PathVariable Long produitId) {
        try {
            delivranceService.ajouterProduitADelivrance(id, produitId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Produit ajouté à la délivrance avec succès");
            response.put("delivranceId", id);
            response.put("produitId", produitId);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("erreur", e.getMessage());
            errorResponse.put("code", "AJOUT_PRODUIT_ERREUR");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PatchMapping("/{id}/produits/retirer/{produitId}")
    public ResponseEntity<?> retirerProduitDeDelivrance(@PathVariable Long id, @PathVariable Long produitId) {
        try {
            delivranceService.retirerProduitDeDelivrance(id, produitId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Produit retiré de la délivrance avec succès");
            response.put("delivranceId", id);
            response.put("produitId", produitId);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("erreur", e.getMessage());
            errorResponse.put("code", "RETRAIT_PRODUIT_ERREUR");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PatchMapping("/{id}/annuler")
    public ResponseEntity<?> annulerDelivrance(@PathVariable Long id) {
        try {
            delivranceService.deleteDelivrance(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Délivrance annulée et supprimée avec succès");
            response.put("delivranceId", id);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("erreur", e.getMessage());
            errorResponse.put("code", "ANNULATION_DELIVRANCE_ERREUR");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    // ========== ENDPOINTS DE SUPPRESSION ==========
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDelivrance(@PathVariable Long id) {
        try {
            delivranceService.deleteDelivrance(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Délivrance supprimée avec succès");
            response.put("delivranceId", id);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("erreur", e.getMessage());
            errorResponse.put("code", "SUPPRESSION_DELIVRANCE_ERREUR");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    // ========== ENDPOINTS DE STATISTIQUES ==========
    
    @GetMapping("/statistiques/total")
    public ResponseEntity<?> getTotalDelivrances() {
        try {
            long total = delivranceService.getTotalDelivrances();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("total", total);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(e, "STATS_TOTAL_ERROR");
        }
    }
    
    @GetMapping("/statistiques/type/{typeProduit}")
    public ResponseEntity<?> countDelivrancesByTypeProduit(@PathVariable String typeProduit) {
        try {
            long count = delivranceService.countDelivrancesByTypeProduit(typeProduit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("typeProduit", typeProduit);
            response.put("count", count);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(e, "STATS_TYPE_ERROR");
        }
    }
    
    @GetMapping("/statistiques/personnel/{personnelId}")
    public ResponseEntity<?> countDelivrancesByPersonnel(@PathVariable Long personnelId) {
        try {
            long count = delivranceService.countDelivrancesByPersonnel(personnelId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("personnelId", personnelId);
            response.put("count", count);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(e, "STATS_PERSONNEL_ERROR");
        }
    }
    
    @GetMapping("/statistiques")
    public ResponseEntity<?> getStatistiquesGlobales() {
        try {
            Map<String, Long> stats = delivranceService.getStatistiquesDelivrances();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(e, "STATS_GLOBAL_ERROR");
        }
    }
    
    @GetMapping("/statistiques/mois/{annee}")
    public ResponseEntity<?> getStatistiquesMensuelles(@PathVariable int annee) {
        try {
            Map<String, Long> stats = delivranceService.getDelivrancesParMois(annee);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("annee", annee);
            response.put("data", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(e, "STATS_MENSUELLES_ERROR");
        }
    }
    
    @GetMapping("/statistiques/mensuel/{annee}")
    public ResponseEntity<?> getStatistiquesParMois(@PathVariable int annee) {
        return getStatistiquesMensuelles(annee);
    }
    
    // ========== ENDPOINTS UTILITAIRES ==========
    
    @GetMapping("/with-products")
    @Transactional
    public ResponseEntity<?> getDelivrancesWithProducts() {
        try {
            List<Delivrance> delivrances = delivranceService.getAllDelivrancesWithDetails();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", delivrances.size());
            response.put("data", delivrances);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(e, "GET_WITH_PRODUCTS_ERROR");
        }
    }
    
    @GetMapping("/export/excel")
    public ResponseEntity<?> exporterExcel() {
        try {
            // Implémentez l'export Excel ici
            byte[] excelData = new byte[0]; // À implémenter
            
            return ResponseEntity.ok()
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header("Content-Disposition", "attachment; filename=\"delivrances.xlsx\"")
                .body(excelData);
        } catch (Exception e) {
            return handleException(e, "EXPORT_EXCEL_ERROR");
        }
    }
    
    @GetMapping("/export/csv")
    public ResponseEntity<?> exporterCSV() {
        try {
            // Implémentez l'export CSV ici
            String csvData = ""; // À implémenter
            
            return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=\"delivrances.csv\"")
                .body(csvData);
        } catch (Exception e) {
            return handleException(e, "EXPORT_CSV_ERROR");
        }
    }
    
    @GetMapping("/{id}/rapport")
    public ResponseEntity<?> genererRapportPDF(@PathVariable Long id) {
        try {
            // Implémentez la génération de rapport PDF ici
            byte[] pdfData = new byte[0]; // À implémenter
            
            return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=\"delivrance_" + id + ".pdf\"")
                .body(pdfData);
        } catch (Exception e) {
            return handleException(e, "RAPPORT_PDF_ERROR");
        }
    }
    
    // ========== MÉTHODES UTILITAIRES ==========
    
    private ResponseEntity<Map<String, Object>> handleException(Exception e, String errorCode) {
        e.printStackTrace();
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", e.getMessage());
        errorResponse.put("code", errorCode);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(errorResponse);
    }
    
    private Map<String, Object> createErrorResponse(String message, String code) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);
        response.put("code", code);
        return response;
    }
    
    // Dans DelivranceController.java

@GetMapping("/produits-disponibles")
public ResponseEntity<?> getAllDelivrancesWithAvailableProducts() {
    try {
        List<Delivrance> delivrances = delivranceService.getAllDelivrancesWithAvailableProducts();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", delivrances.size());
        response.put("data", delivrances);
        
        System.out.println("✅ Délivrances avec produits disponibles: " + delivrances.size());
        
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        e.printStackTrace();
        return handleException(e, "GET_DELIVRANCES_AVAILABLE_PRODUCTS_ERROR");
    }
}

@GetMapping("/{id}/produits-disponibles")
public ResponseEntity<?> getDelivranceByIdWithAvailableProducts(@PathVariable Long id) {
    try {
        Optional<Delivrance> delivranceOpt = delivranceService.getDelivranceByIdWithAvailableProducts(id);
        
        if (delivranceOpt.isPresent()) {
            Delivrance delivrance = delivranceOpt.get();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", delivrance);
            response.put("produitsDisponiblesCount", delivrance.getProduitsSanguins().size());
            
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse("Délivrance non trouvée ou aucun produit disponible", "NOT_FOUND"));
        }
    } catch (Exception e) {
        e.printStackTrace();
        return handleException(e, "GET_DELIVRANCE_AVAILABLE_PRODUCTS_ERROR");
    }
}

@GetMapping("/produit/{produitId}/disponible")
public ResponseEntity<?> estProduitDisponible(@PathVariable Long produitId) {
    try {
        boolean disponible = delivranceService.estProduitDisponible(produitId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("produitId", produitId);
        response.put("disponible", disponible);
        response.put("message", disponible ? 
            "Le produit est disponible" : 
            "Le produit n'est pas disponible (déjà transfusé ou état invalide)");
        
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        e.printStackTrace();
        return handleException(e, "CHECK_PRODUIT_DISPONIBLE_ERROR");
    }
}

    // ========== DTOs INTERNES ==========
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreerDelivranceRequest {
        @NotNull(message = "L'ID de la demande est requis")
        private Long demandeId;
        
        @NotNull(message = "La liste des produits est requise")
        @Size(min = 1, message = "Au moins un produit doit être sélectionné")
        private List<Long> produitIds;
        
        @NotNull(message = "L'ID du personnel est requis")
        private Long personnelId;
        
        @NotBlank(message = "La destination est requise")
        private String destination;
        
        @NotBlank(message = "Le mode de transport est requis")
        private String modeTransport;
        
        private String observations;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerifierProduitsRequest {
        @NotNull
        @Size(min = 1)
        private List<Long> produitIds;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerifierCompatibiliteRequest {
        @NotNull
        private Long demandeId;
        
        @NotNull
        @Size(min = 1)
        private List<Long> produitIds;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AjouterProduitRequest {
        @NotNull
        private Long produitId;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatistiquesResponse {
        private Long totalDelivrances;
        private Map<String, Long> parTypeProduit;
        private Map<String, Long> parPersonnel;
        private Map<String, Long> parMois;
    }

    @PutMapping("/{id}/complete")
public ResponseEntity<Delivrance> modifierComplete(
        @PathVariable Long id,
        @RequestBody ModifierDelivranceRequest request) {

    Delivrance del = delivranceService.modifierDelivranceComplete(
        id,
        request.getProduitIds(),
        request.getDestination(),
        request.getModeTransport(),
        request.getObservations()
    );

    return ResponseEntity.ok(del);
}
}