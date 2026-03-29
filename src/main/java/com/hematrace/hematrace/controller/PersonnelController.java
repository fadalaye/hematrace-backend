package com.hematrace.hematrace.controller;

import com.hematrace.hematrace.entite.Personnel;
import com.hematrace.hematrace.service.PersonnelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/personnel")
public class PersonnelController {
    
    @Autowired
    private PersonnelService personnelService;
    
    @PostMapping
    public ResponseEntity<Personnel> creerPersonnel(@RequestBody Personnel personnel) {
        try {
            return ResponseEntity.ok(personnelService.creerPersonnel(personnel));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Personnel>> getAllPersonnel() {
        return ResponseEntity.ok(personnelService.getAllPersonnel());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Personnel> getPersonnelById(@PathVariable Long id) {
        Optional<Personnel> personnel = personnelService.getPersonnelById(id);
        return personnel.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/matricule/{matricule}")
    public ResponseEntity<Personnel> getPersonnelByMatricule(@PathVariable String matricule) {
        try {
            return ResponseEntity.ok(personnelService.getPersonnelByMatricule(matricule));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/fonction/{fonction}")
    public ResponseEntity<List<Personnel>> getPersonnelByFonction(@PathVariable String fonction) {
        return ResponseEntity.ok(personnelService.getPersonnelByFonction(fonction));
    }
    
    @GetMapping("/{id}/statistiques/demandes")
    public ResponseEntity<Long> countDemandesByPersonnel(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(personnelService.countDemandesByPersonnel(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Personnel> updatePersonnel(@PathVariable Long id, @RequestBody Personnel personnelDetails) {
        try {
            return ResponseEntity.ok(personnelService.updatePersonnel(id, personnelDetails));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PatchMapping("/{id}/fonction")
    public ResponseEntity<Void> updateFonction(@PathVariable Long id, @RequestParam String fonction) {
        try {
            personnelService.updateFonction(id, fonction);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePersonnel(@PathVariable Long id) {
        try {
            personnelService.deletePersonnel(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}