package com.hematrace.hematrace.service.impl;

import com.hematrace.hematrace.entite.Admin;
import com.hematrace.hematrace.entite.Utilisateur;
import com.hematrace.hematrace.repository.AdminRepository;
import com.hematrace.hematrace.repository.UtilsateurRepository;
import com.hematrace.hematrace.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private UtilsateurRepository utilisateurRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Admin creerAdmin(Admin admin) {
        // Vérifier l'unicité du matricule
        if (utilisateurRepository.existsByMatricule(admin.getMatricule())) {
            throw new RuntimeException("Le matricule existe déjà");
        }
        
        // Vérifier l'unicité de l'email
        if (utilisateurRepository.existsByEmail(admin.getEmail())) {
            throw new RuntimeException("L'email existe déjà");
        }
        
        // Hasher le mot de passe avant sauvegarde
        admin.setMotDePasse(passwordEncoder.encode(admin.getMotDePasse()));
        
        // Normaliser les données
        admin.setEmail(admin.getEmail().toLowerCase().trim());
        admin.setNom(admin.getNom().toUpperCase().trim());
        admin.setPrenom(capitalizeFirstLetter(admin.getPrenom().trim()));
        admin.setRole(admin.getRole().toUpperCase().trim());
        
        // Définir le statut par défaut pour un admin
        admin.setStatut("ACTIF");
        
        return adminRepository.save(admin);
    }

    @Override
    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    @Override
    public Optional<Admin> getAdminById(Long id) {
        return adminRepository.findById(id);
    }

    @Override
    public Admin getAdminByMatricule(String matricule) {
        return adminRepository.findAll().stream()
                .filter(admin -> admin.getMatricule().equals(matricule))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Admin non trouvé avec le matricule: " + matricule));
    }

    @Override
    public Admin getAdminByEmail(String email) {
        return adminRepository.findAll().stream()
                .filter(admin -> admin.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Admin non trouvé avec l'email: " + email));
    }

    @Override
    public Admin updateAdmin(Long id, Admin adminDetails) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin non trouvé avec l'id: " + id));
        
        // Vérifier si le nouveau matricule existe déjà (pour un autre utilisateur)
        if (!admin.getMatricule().equals(adminDetails.getMatricule()) && 
            utilisateurRepository.existsByMatricule(adminDetails.getMatricule())) {
            throw new RuntimeException("Le matricule existe déjà pour un autre utilisateur");
        }
        
        // Vérifier si le nouvel email existe déjà (pour un autre utilisateur)
        if (!admin.getEmail().equalsIgnoreCase(adminDetails.getEmail()) && 
            utilisateurRepository.existsByEmail(adminDetails.getEmail())) {
            throw new RuntimeException("L'email existe déjà pour un autre utilisateur");
        }
        
        // Mettre à jour les champs de base (hérités de Utilisateur)
        admin.setMatricule(adminDetails.getMatricule());
        admin.setNom(adminDetails.getNom().toUpperCase().trim());
        admin.setPrenom(capitalizeFirstLetter(adminDetails.getPrenom().trim()));
        admin.setEmail(adminDetails.getEmail().toLowerCase().trim());
        admin.setTelephone(adminDetails.getTelephone());
        admin.setSexe(adminDetails.getSexe());
        admin.setDateNaissance(adminDetails.getDateNaissance());
        admin.setAdresse(adminDetails.getAdresse());
        admin.setDateEmbauche(adminDetails.getDateEmbauche());
        admin.setPhotoProfil(adminDetails.getPhotoProfil());
        admin.setStatut(adminDetails.getStatut());
        
        // Mettre à jour les champs spécifiques à Admin
        admin.setRole(adminDetails.getRole().toUpperCase().trim());
        admin.setDroitsAccess(adminDetails.getDroitsAccess());
        
        // Ne mettre à jour le mot de passe que s'il est fourni
        if (adminDetails.getMotDePasse() != null && !adminDetails.getMotDePasse().isEmpty()) {
            admin.setMotDePasse(passwordEncoder.encode(adminDetails.getMotDePasse()));
        }
        
        return adminRepository.save(admin);
    }

    @Override
    public void updateRoleAdmin(Long id, String nouveauRole) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin non trouvé avec l'id: " + id));
        
        admin.setRole(nouveauRole.toUpperCase().trim());
        adminRepository.save(admin);
    }

    @Override
    public void updateDroitsAccessAdmin(Long id, String nouveauxDroits) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin non trouvé avec l'id: " + id));
        
        admin.setDroitsAccess(nouveauxDroits);
        adminRepository.save(admin);
    }

    @Override
    public void deleteAdmin(Long id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin non trouvé avec l'id: " + id));
        
        adminRepository.delete(admin);
    }

    @Override
    public List<Admin> getAdminsByRole(String role) {
        return adminRepository.findAll().stream()
                .filter(admin -> admin.getRole().equalsIgnoreCase(role))
                .collect(Collectors.toList());
    }
    
    // Méthode utilitaire pour capitaliser la première lettre
    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}