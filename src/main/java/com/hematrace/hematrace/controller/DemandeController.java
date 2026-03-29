package com.hematrace.hematrace.controller;

import com.hematrace.hematrace.entite.Demande;
import com.hematrace.hematrace.service.DemandeService;
import com.hematrace.hematrace.service.impl.DemandeServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/demandes")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@Slf4j
public class DemandeController {
    
    private final DemandeService demandeService;
    
    @GetMapping
    public ResponseEntity<List<Demande>> getAllDemandes() {
        log.info("GET /api/demandes - Récupération de toutes les demandes");
        try {
            List<Demande> demandes = demandeService.getAllDemandes();
            log.info("✅ {} demandes récupérées avec succès", demandes.size());
            return ResponseEntity.ok(demandes);
        } catch (Exception e) {
            log.error("❌ Erreur lors de la récupération des demandes: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Demande> getDemandeById(@PathVariable Long id) {
        log.info("GET /api/demandes/{} - Récupération de la demande", id);
        try {
            return demandeService.getDemandeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("❌ Erreur lors de la récupération de la demande {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping
    public ResponseEntity<Demande> createDemande(@RequestBody Demande demande) {
        log.info("POST /api/demandes - Création d'une nouvelle demande");
        try {
            Demande createdDemande = demandeService.creerDemande(demande);
            log.info("✅ Demande créée avec ID: {}", createdDemande.getId());
            return ResponseEntity.ok(createdDemande);
        } catch (Exception e) {
            log.error("❌ Erreur lors de la création de la demande: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Demande> updateDemande(@PathVariable Long id, @RequestBody Demande demande) {
        log.info("PUT /api/demandes/{} - Mise à jour de la demande", id);
        try {
            Demande updatedDemande = demandeService.updateDemande(id, demande);
            log.info("✅ Demande {} mise à jour avec succès", id);
            return ResponseEntity.ok(updatedDemande);
        } catch (Exception e) {
            log.error("❌ Erreur lors de la mise à jour de la demande {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/statut/{statut}")
    public ResponseEntity<Void> updateStatutDemande(@PathVariable Long id, @PathVariable String statut) {
        log.info("PUT /api/demandes/{}/statut/{} - Changement de statut", id, statut);
        try {
            demandeService.updateStatutDemande(id, statut);
            log.info("✅ Statut de la demande {} changé à {}", id, statut);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("❌ Erreur lors du changement de statut de la demande {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{demandeId}/valider/{personnelId}")
    public ResponseEntity<Demande> validerDemande(@PathVariable Long demandeId, @PathVariable Long personnelId) {
        log.info("PUT /api/demandes/{}/valider/{} - Validation de la demande", demandeId, personnelId);
        try {
            Demande validatedDemande = demandeService.validerDemande(demandeId, personnelId);
            log.info("✅ Demande {} validée par le personnel {}", demandeId, personnelId);
            return ResponseEntity.ok(validatedDemande);
        } catch (Exception e) {
            log.error("❌ Erreur lors de la validation de la demande {}: {}", demandeId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDemande(@PathVariable Long id) {
        log.info("DELETE /api/demandes/{} - Suppression de la demande", id);
        try {
            demandeService.deleteDemande(id);
            log.info("✅ Demande {} supprimée avec succès", id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("❌ Erreur lors de la suppression de la demande {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Endpoint de débogage
    @GetMapping("/debug/info")
    public ResponseEntity<Map<String, Object>> getDebugInfo() {
        log.info("GET /api/demandes/debug/info - Informations de débogage");
        try {
            if (demandeService instanceof DemandeServiceImpl) {
                DemandeServiceImpl serviceImpl = (DemandeServiceImpl) demandeService;
                Map<String, Object> debugInfo = serviceImpl.getDebugInfo();
                return ResponseEntity.ok(debugInfo);
            }
            return ResponseEntity.ok(Map.of("message", "Service non disponible"));
        } catch (Exception e) {
            log.error("❌ Erreur lors de la récupération des infos de débogage: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Endpoints supplémentaires pour les filtres
    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<Demande>> getDemandesByStatut(@PathVariable String statut) {
        log.info("GET /api/demandes/statut/{} - Demandes par statut", statut);
        try {
            List<Demande> demandes = demandeService.getDemandesByStatut(statut);
            return ResponseEntity.ok(demandes);
        } catch (Exception e) {
            log.error("❌ Erreur: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/urgentes")
    public ResponseEntity<List<Demande>> getDemandesUrgentes() {
        log.info("GET /api/demandes/urgentes - Demandes urgentes");
        try {
            List<Demande> demandes = demandeService.getDemandesUrgentes();
            return ResponseEntity.ok(demandes);
        } catch (Exception e) {
            log.error("❌ Erreur: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/statistiques")
    public ResponseEntity<Map<String, Long>> getStatistiques() {
        log.info("GET /api/demandes/statistiques - Statistiques des demandes");
        try {
            Map<String, Long> stats = demandeService.getStatistiquesDemandes();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("❌ Erreur: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}