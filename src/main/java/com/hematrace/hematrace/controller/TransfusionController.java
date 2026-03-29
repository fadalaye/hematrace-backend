package com.hematrace.hematrace.controller;

import com.hematrace.hematrace.dto.CreerTransfusionDTO;
import com.hematrace.hematrace.dto.TransfusionWithSurveillancesDTO;
import com.hematrace.hematrace.entite.Transfusion;
import com.hematrace.hematrace.repository.TransfusionRepository;
import com.hematrace.hematrace.service.IncidentTransfusionnelService;
import com.hematrace.hematrace.service.TransfusionService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transfusions")
//@CrossOrigin(origins = "*") // Autorise toutes les origines (à ajuster en production)
public class TransfusionController {
    
    @Autowired
    private TransfusionService transfusionService;

        @Autowired
    private TransfusionRepository transfusionRepository; // Ajouté
    
    @Autowired
    private IncidentTransfusionnelService incidentService;
    
    // ========== ENDPOINTS DE CRÉATION ==========
    
    /**
     * Crée une nouvelle transfusion (sans surveillances)
     * Endpoint : POST /api/transfusions
     */
    @PostMapping
    public ResponseEntity<?> creerTransfusion(@Valid @RequestBody CreerTransfusionDTO dto) {
        try {
            Transfusion transfusion = transfusionService.creerTransfusion(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(transfusion);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * Crée une transfusion avec ses surveillances
     * Endpoint : POST /api/transfusions/avec-surveillances
     */
    @PostMapping("/avec-surveillances")
    public ResponseEntity<?> creerTransfusionAvecSurveillances(
            @Valid @RequestBody TransfusionWithSurveillancesDTO dto) {
        try {
            Transfusion transfusion = transfusionService.creerTransfusionAvecSurveillances(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(transfusion);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * Endpoint de compatibilité - Crée une transfusion à partir d'une entité
     * Endpoint : POST /api/transfusions/simple
     */
    @PostMapping("/simple")
    public ResponseEntity<Transfusion> creerTransfusionSimple(@RequestBody Transfusion transfusion) {
        try {
            Transfusion createdTransfusion = transfusionService.creerTransfusion(transfusion);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTransfusion);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // ========== ENDPOINTS DE RÉCUPÉRATION ==========
    
    /**
     * Récupère toutes les transfusions
     * Endpoint : GET /api/transfusions
     */
    @GetMapping
    public ResponseEntity<List<Transfusion>> getAllTransfusions() {
        List<Transfusion> transfusions = transfusionService.getAllTransfusions();
        return ResponseEntity.ok(transfusions);
    }
    
    /**
     * Récupère une transfusion par son ID
     * Endpoint : GET /api/transfusions/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Transfusion> getTransfusionById(@PathVariable Long id) {
        Optional<Transfusion> transfusion = transfusionService.getTransfusionById(id);
        return transfusion.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }
    
    // ========== ENDPOINTS DE RECHERCHE ==========
    
    /**
     * Récupère les transfusions par médecin
     * Endpoint : GET /api/transfusions/medecin/{medecinId}
     */
    @GetMapping("/medecin/{medecinId}")
    public ResponseEntity<List<Transfusion>> getTransfusionsByMedecin(@PathVariable Long medecinId) {
        List<Transfusion> transfusions = transfusionService.getTransfusionsByMedecin(medecinId);
        return ResponseEntity.ok(transfusions);
    }
    
    /**
     * Récupère les transfusions par produit sanguin
     * Endpoint : GET /api/transfusions/produit-sanguin/{produitSanguinId}
     */
    @GetMapping("/produit-sanguin/{produitSanguinId}")
    public ResponseEntity<List<Transfusion>> getTransfusionsByProduitSanguin(@PathVariable Long produitSanguinId) {
        List<Transfusion> transfusions = transfusionService.getTransfusionsByProduitSanguin(produitSanguinId);
        return ResponseEntity.ok(transfusions);
    }
    
    /**
     * Récupère les transfusions par groupe sanguin
     * Endpoint : GET /api/transfusions/groupe-sanguin/{groupeSanguin}
     */
    @GetMapping("/groupe-sanguin/{groupeSanguin}")
    public ResponseEntity<List<Transfusion>> getTransfusionsByGroupeSanguin(@PathVariable String groupeSanguin) {
        List<Transfusion> transfusions = transfusionService.getTransfusionsByGroupeSanguin(groupeSanguin);
        return ResponseEntity.ok(transfusions);
    }
    
    /**
     * Récupère les transfusions par tolérance
     * Endpoint : GET /api/transfusions/tolerance/{tolerance}
     */
    @GetMapping("/tolerance/{tolerance}")
    public ResponseEntity<List<Transfusion>> getTransfusionsByTolerance(@PathVariable String tolerance) {
        List<Transfusion> transfusions = transfusionService.getTransfusionsByTolerance(tolerance);
        return ResponseEntity.ok(transfusions);
    }
    
    /**
     * Récupère les transfusions avec effets indésirables
     * Endpoint : GET /api/transfusions/effets-indesirables
     */
    @GetMapping("/effets-indesirables")
    public ResponseEntity<List<Transfusion>> getTransfusionsAvecEffetsIndesirables() {
        List<Transfusion> transfusions = transfusionService.getTransfusionsAvecEffetsIndesirables();
        return ResponseEntity.ok(transfusions);
    }
    
    /**
     * Récupère les transfusions par patient (nom et prénom)
     * Endpoint : GET /api/transfusions/patient?nom={nom}&prenom={prenom}
     */
    @GetMapping("/patient")
    public ResponseEntity<List<Transfusion>> getTransfusionsByPatient(
            @RequestParam String nom, 
            @RequestParam String prenom) {
        List<Transfusion> transfusions = transfusionService.getTransfusionsByPatient(nom, prenom);
        return ResponseEntity.ok(transfusions);
    }
    
    /**
     * Récupère les transfusions par numéro de dossier patient
     * Endpoint : GET /api/transfusions/dossier/{numDossier}
     */
    @GetMapping("/dossier/{numDossier}")
    public ResponseEntity<List<Transfusion>> getTransfusionsByNumDossier(@PathVariable String numDossier) {
        List<Transfusion> transfusions = transfusionService.getTransfusionsByNumDossier(numDossier);
        return ResponseEntity.ok(transfusions);
    }
    
    /**
     * Récupère les transfusions par date
     * Endpoint : GET /api/transfusions/date?date={date}
     */
    @GetMapping("/date")
    public ResponseEntity<List<Transfusion>> getTransfusionsByDate(@RequestParam LocalDate date) {
        List<Transfusion> transfusions = transfusionService.getTransfusionsByDate(date);
        return ResponseEntity.ok(transfusions);
    }
    
    /**
     * Récupère les transfusions par plage de dates
     * Endpoint : GET /api/transfusions/date-range?startDate={startDate}&endDate={endDate}
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<Transfusion>> getTransfusionsByDateRange(
            @RequestParam LocalDate startDate, 
            @RequestParam LocalDate endDate) {
        List<Transfusion> transfusions = transfusionService.getTransfusionsByDateRange(startDate, endDate);
        return ResponseEntity.ok(transfusions);
    }
    
    // ========== ENDPOINTS STATISTIQUES ==========
    
    /**
     * Compte les transfusions par tolérance
     * Endpoint : GET /api/transfusions/count/tolerance/{tolerance}
     */
    @GetMapping("/count/tolerance/{tolerance}")
    public ResponseEntity<Long> countTransfusionsByTolerance(@PathVariable String tolerance) {
        long count = transfusionService.countTransfusionsByTolerance(tolerance);
        return ResponseEntity.ok(count);
    }
    
    /**
     * Compte les transfusions avec effets indésirables
     * Endpoint : GET /api/transfusions/count/effets-indesirables
     */
    @GetMapping("/count/effets-indesirables")
    public ResponseEntity<Long> countTransfusionsAvecEffetsIndesirables() {
        long count = transfusionService.countTransfusionsAvecEffetsIndesirables();
        return ResponseEntity.ok(count);
    }
    
    // ========== ENDPOINTS DE MISE À JOUR ET SUPPRESSION ==========
    
    /**
     * Met à jour une transfusion existante
     * Endpoint : PUT /api/transfusions/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Transfusion> updateTransfusion(
            @PathVariable Long id, 
            @RequestBody Transfusion transfusionDetails) {
        try {
            Transfusion updatedTransfusion = transfusionService.updateTransfusion(id, transfusionDetails);
            return ResponseEntity.ok(updatedTransfusion);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Supprime une transfusion
     * Endpoint : DELETE /api/transfusions/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransfusion(@PathVariable Long id) {
        try {
            transfusionService.deleteTransfusion(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

/**
     * Récupère les transfusions sans incident déclaré
     * Endpoint : GET /api/transfusions/sans-incident debut modification
     */
@GetMapping("/compatibles-incident")
public ResponseEntity<List<Transfusion>> getTransfusionsCompatiblesIncident() {
    try {
        // Utilisez la méthode optimisée du repository
        List<Transfusion> compatibles = transfusionRepository.findTransfusionsSansIncident();
        return ResponseEntity.ok(compatibles);
    } catch (Exception e) {
        // Log l'erreur pour le débogage
        System.err.println("Erreur dans getTransfusionsCompatiblesIncident: " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.internalServerError()
            .body(null);
    }
}

@GetMapping("/sans-incident")
public ResponseEntity<List<Transfusion>> getTransfusionsSansIncident() {
    try {
        // Utilisez la méthode optimisée du repository
        List<Transfusion> sansIncident = transfusionRepository.findTransfusionsSansIncident();
        return ResponseEntity.ok(sansIncident);
    } catch (Exception e) {
        return ResponseEntity.internalServerError().build();
    }
}

@GetMapping("/{id}/has-incident")
public ResponseEntity<Boolean> hasIncident(@PathVariable Long id) {
    try {
        // Vérifiez directement dans le repository
        Optional<Transfusion> transfusion = transfusionRepository.findById(id);
        boolean hasIncident = transfusion.isPresent() && 
                            transfusion.get().getIncidentTransfusionnel() != null;
        return ResponseEntity.ok(hasIncident);
    } catch (Exception e) {
        return ResponseEntity.internalServerError().build();
    }
}
    // fin modification
    // ========== ENDPOINT DE SANTÉ ==========
    
    /**
     * Endpoint de santé pour vérifier que l'API fonctionne
     * Endpoint : GET /api/transfusions/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("API Transfusions est opérationnelle");
    }
}