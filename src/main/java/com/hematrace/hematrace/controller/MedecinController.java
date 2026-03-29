package com.hematrace.hematrace.controller;

import com.hematrace.hematrace.entite.Medecin;
import com.hematrace.hematrace.service.MedecinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/medecins")
public class MedecinController {
    
    @Autowired
    private MedecinService medecinService;
    
    @PostMapping
    public ResponseEntity<?> creerMedecin(@RequestBody Medecin medecin) {
        try {
            System.out.println("🟢 Requête reçue pour créer médecin: " + medecin.getEmail());
            Medecin savedMedecin = medecinService.creerMedecin(medecin);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedMedecin);
        } catch (RuntimeException e) {
            System.err.println("❌ Erreur création médecin: " + e.getMessage());
            
            // Retourner un message d'erreur détaillé
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            errorResponse.put("error", "Erreur de création");
            errorResponse.put("status", "400");
            
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue: " + e.getMessage());
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Erreur interne du serveur");
            errorResponse.put("error", "INTERNAL_SERVER_ERROR");
            errorResponse.put("status", "500");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    @GetMapping
    public ResponseEntity<List<Medecin>> getAllMedecins() {
        return ResponseEntity.ok(medecinService.getAllMedecins());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Medecin> getMedecinById(@PathVariable Long id) {
        Optional<Medecin> medecin = medecinService.getMedecinById(id);
        return medecin.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/matricule/{matricule}")
    public ResponseEntity<Medecin> getMedecinByMatricule(@PathVariable String matricule) {
        try {
            return ResponseEntity.ok(medecinService.getMedecinByMatricule(matricule));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/specialite/{specialite}")
    public ResponseEntity<List<Medecin>> getMedecinsBySpecialite(@PathVariable String specialite) {
        return ResponseEntity.ok(medecinService.getMedecinsBySpecialite(specialite));
    }
    
    @GetMapping("/{id}/statistiques/demandes")
    public ResponseEntity<Long> countDemandesByMedecin(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(medecinService.countDemandesByMedecin(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/{id}/statistiques/transfusions")
    public ResponseEntity<Long> countTransfusionsByMedecin(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(medecinService.countTransfusionsByMedecin(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Medecin> updateMedecin(@PathVariable Long id, @RequestBody Medecin medecinDetails) {
        try {
            return ResponseEntity.ok(medecinService.updateMedecin(id, medecinDetails));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PatchMapping("/{id}/specialite")
    public ResponseEntity<Void> updateSpecialite(@PathVariable Long id, @RequestParam String specialite) {
        try {
            medecinService.updateSpecialite(id, specialite);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedecin(@PathVariable Long id) {
        try {
            medecinService.deleteMedecin(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}