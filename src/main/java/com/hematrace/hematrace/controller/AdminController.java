package com.hematrace.hematrace.controller;

import com.hematrace.hematrace.entite.Admin;
import com.hematrace.hematrace.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admins")
public class AdminController {
    
    @Autowired
    private AdminService adminService;
    
    @PostMapping
    public ResponseEntity<Admin> creerAdmin(@RequestBody Admin admin) {
        try {
            return ResponseEntity.ok(adminService.creerAdmin(admin));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Admin>> getAllAdmins() {
        return ResponseEntity.ok(adminService.getAllAdmins());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Admin> getAdminById(@PathVariable Long id) {
        Optional<Admin> admin = adminService.getAdminById(id);
        return admin.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/matricule/{matricule}")
    public ResponseEntity<Admin> getAdminByMatricule(@PathVariable String matricule) {
        try {
            return ResponseEntity.ok(adminService.getAdminByMatricule(matricule));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/role/{role}")
    public ResponseEntity<List<Admin>> getAdminsByRole(@PathVariable String role) {
        return ResponseEntity.ok(adminService.getAdminsByRole(role));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Admin> updateAdmin(@PathVariable Long id, @RequestBody Admin adminDetails) {
        try {
            return ResponseEntity.ok(adminService.updateAdmin(id, adminDetails));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PatchMapping("/{id}/role")
    public ResponseEntity<Void> updateRoleAdmin(@PathVariable Long id, @RequestParam String role) {
        try {
            adminService.updateRoleAdmin(id, role);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PatchMapping("/{id}/droits")
    public ResponseEntity<Void> updateDroitsAccessAdmin(@PathVariable Long id, @RequestParam String droits) {
        try {
            adminService.updateDroitsAccessAdmin(id, droits);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable Long id) {
        try {
            adminService.deleteAdmin(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}