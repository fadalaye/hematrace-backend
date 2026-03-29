package com.hematrace.hematrace.controller;

import com.hematrace.hematrace.entite.ChefService;
import com.hematrace.hematrace.service.ChefServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/chefs-service")
public class ChefServiceController {
    
    @Autowired
    private ChefServiceService chefServiceService;
    
    @PostMapping
    public ResponseEntity<ChefService> creerChefService(@RequestBody ChefService chefService) {
        try {
            return ResponseEntity.ok(chefServiceService.creerChefService(chefService));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @GetMapping
    public ResponseEntity<List<ChefService>> getAllChefsService() {
        return ResponseEntity.ok(chefServiceService.getAllChefsService());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ChefService> getChefServiceById(@PathVariable Long id) {
        Optional<ChefService> chefService = chefServiceService.getChefServiceById(id);
        return chefService.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/matricule/{matricule}")
    public ResponseEntity<ChefService> getChefServiceByMatricule(@PathVariable String matricule) {
        try {
            return ResponseEntity.ok(chefServiceService.getChefServiceByMatricule(matricule));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/service/{service}")
    public ResponseEntity<List<ChefService>> getChefsServiceByService(@PathVariable String service) {
        return ResponseEntity.ok(chefServiceService.getChefsServiceByService(service));
    }
    
    @GetMapping("/departement/{departement}")
    public ResponseEntity<List<ChefService>> getChefsServiceByDepartement(@PathVariable String departement) {
        return ResponseEntity.ok(chefServiceService.getChefsServiceByDepartement(departement));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ChefService> updateChefService(@PathVariable Long id, @RequestBody ChefService chefServiceDetails) {
        try {
            return ResponseEntity.ok(chefServiceService.updateChefService(id, chefServiceDetails));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PatchMapping("/{id}/service")
    public ResponseEntity<Void> updateServiceDirige(@PathVariable Long id, @RequestParam String service) {
        try {
            chefServiceService.updateServiceDirige(id, service);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PatchMapping("/{id}/departement")
    public ResponseEntity<Void> updateDepartement(@PathVariable Long id, @RequestParam String departement) {
        try {
            chefServiceService.updateDepartement(id, departement);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChefService(@PathVariable Long id) {
        try {
            chefServiceService.deleteChefService(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}