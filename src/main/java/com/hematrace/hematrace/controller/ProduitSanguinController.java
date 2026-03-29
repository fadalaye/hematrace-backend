package com.hematrace.hematrace.controller;

import com.hematrace.hematrace.entite.ProduitSanguin;
import com.hematrace.hematrace.repository.ProduitSanguinRepository;
import com.hematrace.hematrace.service.ProduitSanguinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/produits-sanguins")
@RequiredArgsConstructor
public class ProduitSanguinController {
    
    private final ProduitSanguinService produitSanguinService;
    private final ProduitSanguinRepository produitSanguinRepository;
    
    @PostMapping
    public ResponseEntity<?> creerProduitSanguin(@RequestBody ProduitSanguin produitSanguin) {
        try {
            ProduitSanguin saved = produitSanguinService.ajouterProduitAuStock(produitSanguin);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }
    
    @GetMapping
    public ResponseEntity<List<ProduitSanguin>> getAllProduitsSanguins() {
        return ResponseEntity.ok(produitSanguinService.getAllProduitsSanguins());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProduitSanguin> getProduitSanguinById(@PathVariable Long id) {
        Optional<ProduitSanguin> produitSanguin = produitSanguinService.getProduitSanguinById(id);
        return produitSanguin.map(ResponseEntity::ok)
                           .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/code/{codeProduit}")
    public ResponseEntity<ProduitSanguin> getProduitSanguinByCode(@PathVariable String codeProduit) {
        try {
            return ResponseEntity.ok(produitSanguinService.getProduitSanguinByCode(codeProduit));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/type/{typeProduit}")
    public ResponseEntity<List<ProduitSanguin>> getProduitsSanguinsByType(@PathVariable String typeProduit) {
        return ResponseEntity.ok(produitSanguinService.getProduitsSanguinsByType(typeProduit));
    }
    
    @GetMapping("/groupe-sanguin/{groupeSanguin}")
    public ResponseEntity<List<ProduitSanguin>> getProduitsSanguinsByGroupeSanguin(@PathVariable String groupeSanguin) {
        return ResponseEntity.ok(produitSanguinService.getProduitsSanguinsByGroupeSanguin(groupeSanguin));
    }
    
    @GetMapping("/rhesus/{rhesus}")
    public ResponseEntity<List<ProduitSanguin>> getProduitsSanguinsByRhesus(@PathVariable String rhesus) {
        return ResponseEntity.ok(produitSanguinService.getProduitsSanguinsByRhesus(rhesus));
    }
    
    @GetMapping("/etat/{etat}")
    public ResponseEntity<List<ProduitSanguin>> getProduitsSanguinsByEtat(@PathVariable String etat) {
        return ResponseEntity.ok(produitSanguinService.getProduitsSanguinsByEtat(etat));
    }
    
    @GetMapping("/disponibles")
    public ResponseEntity<List<ProduitSanguin>> getProduitsSanguinsDisponibles() {
        return ResponseEntity.ok(produitSanguinService.getProduitsSanguinsDisponibles());
    }
    
    @GetMapping("/expires")
    public ResponseEntity<List<ProduitSanguin>> getProduitsSanguinsExpires() {
        return ResponseEntity.ok(produitSanguinService.getProduitsSanguinsExpires());
    }
    
    @GetMapping("/peremption/{joursRestants}")
    public ResponseEntity<List<ProduitSanguin>> getProduitsSanguinsProchesPeremption(@PathVariable int joursRestants) {
        return ResponseEntity.ok(produitSanguinService.getProduitsSanguinsProchesPeremption(joursRestants));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduitSanguin(@PathVariable Long id, @RequestBody ProduitSanguin produitSanguinDetails) {
        try {
            return ResponseEntity.ok(produitSanguinService.updateProduitSanguin(id, produitSanguinDetails));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }
    
    @PatchMapping("/{id}/etat")
    public ResponseEntity<?> updateEtatProduitSanguin(@PathVariable Long id, @RequestParam String etat) {
        try {
            produitSanguinService.updateEtatProduitSanguin(id, etat);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }
    
    @PatchMapping("/{id}/expire")
    public ResponseEntity<?> marquerCommeExpire(@PathVariable Long id) {
        try {
            produitSanguinService.marquerCommeExpire(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }
    
    @PatchMapping("/{id}/utilise")
    public ResponseEntity<?> marquerCommeUtilise(@PathVariable Long id) {
        try {
            produitSanguinService.marquerCommeUtilise(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduitSanguin(@PathVariable Long id) {
        try {
            produitSanguinService.deleteProduitSanguin(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }
    
    // Endpoints de statistiques
    @GetMapping("/statistiques/type/{typeProduit}")
    public ResponseEntity<Long> countProduitsSanguinsByType(@PathVariable String typeProduit) {
        return ResponseEntity.ok(produitSanguinService.countProduitsSanguinsByType(typeProduit));
    }
    
    @GetMapping("/statistiques/groupe-sanguin/{groupeSanguin}")
    public ResponseEntity<Long> countProduitsSanguinsByGroupeSanguin(@PathVariable String groupeSanguin) {
        return ResponseEntity.ok(produitSanguinService.countProduitsSanguinsByGroupeSanguin(groupeSanguin));
    }
    
    @GetMapping("/statistiques/disponibles/count")
    public ResponseEntity<Long> countProduitsSanguinsDisponibles() {
        return ResponseEntity.ok(produitSanguinService.countProduitsSanguinsDisponibles());
    }
    
    @GetMapping("/statistiques")
    public ResponseEntity<Map<String, Long>> getStatistiquesStock() {
        return ResponseEntity.ok(produitSanguinService.getStatistiquesStock());
    }

     @GetMapping("/check-code/{code}")
    public ResponseEntity<Map<String, Boolean>> checkCodeUnique(@PathVariable String code) {
        ProduitSanguin existing = produitSanguinRepository.findByCodeProduitIgnoreCase(code);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isUnique", existing == null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ProduitSanguin> getByCode(@PathVariable String code) {
        ProduitSanguin produit = produitSanguinRepository.findByCodeProduitIgnoreCase(code);
        if (produit == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(produit);
    }
}