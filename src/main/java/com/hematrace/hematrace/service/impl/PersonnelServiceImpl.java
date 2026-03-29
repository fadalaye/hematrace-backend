package com.hematrace.hematrace.service.impl;

import com.hematrace.hematrace.entite.Personnel;
import com.hematrace.hematrace.entite.Utilisateur;
import com.hematrace.hematrace.repository.PersonnelRepository;
import com.hematrace.hematrace.repository.UtilsateurRepository;
import com.hematrace.hematrace.service.PersonnelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PersonnelServiceImpl implements PersonnelService {

    @Autowired
    private PersonnelRepository personnelRepository;
    
    @Autowired
    private UtilsateurRepository utilisateurRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Personnel creerPersonnel(Personnel personnel) {
        // Vérifier l'unicité du matricule
        if (utilisateurRepository.existsByMatricule(personnel.getMatricule())) {
            throw new RuntimeException("Le matricule existe déjà");
        }
        
        // Vérifier l'unicité de l'email
        if (utilisateurRepository.existsByEmail(personnel.getEmail())) {
            throw new RuntimeException("L'email existe déjà");
        }
        
        // Hasher le mot de passe avant sauvegarde
        personnel.setMotDePasse(passwordEncoder.encode(personnel.getMotDePasse()));
        
        // Normaliser les données
        personnel.setEmail(personnel.getEmail().toLowerCase().trim());
        personnel.setNom(personnel.getNom().toUpperCase().trim());
        personnel.setPrenom(capitalizeFirstLetter(personnel.getPrenom().trim()));
        personnel.setFonction(capitalizeFirstLetter(personnel.getFonction().trim()));
        
        // Définir le statut par défaut pour le personnel
        personnel.setStatut("ACTIF");
        
        return personnelRepository.save(personnel);
    }

    @Override
    public List<Personnel> getAllPersonnel() {
        return personnelRepository.findAll();
    }

    @Override
    public Optional<Personnel> getPersonnelById(Long id) {
        return personnelRepository.findById(id);
    }

    @Override
    public Personnel getPersonnelByMatricule(String matricule) {
        return personnelRepository.findAll().stream()
                .filter(personnel -> personnel.getMatricule().equals(matricule))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Personnel non trouvé avec le matricule: " + matricule));
    }

    @Override
    public Personnel getPersonnelByEmail(String email) {
        return personnelRepository.findAll().stream()
                .filter(personnel -> personnel.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Personnel non trouvé avec l'email: " + email));
    }

    @Override
    public Personnel updatePersonnel(Long id, Personnel personnelDetails) {
        Personnel personnel = personnelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Personnel non trouvé avec l'id: " + id));
        
        // Vérifier si le nouveau matricule existe déjà (pour un autre utilisateur)
        if (!personnel.getMatricule().equals(personnelDetails.getMatricule()) && 
            utilisateurRepository.existsByMatricule(personnelDetails.getMatricule())) {
            throw new RuntimeException("Le matricule existe déjà pour un autre utilisateur");
        }
        
        // Vérifier si le nouvel email existe déjà (pour un autre utilisateur)
        if (!personnel.getEmail().equalsIgnoreCase(personnelDetails.getEmail()) && 
            utilisateurRepository.existsByEmail(personnelDetails.getEmail())) {
            throw new RuntimeException("L'email existe déjà pour un autre utilisateur");
        }
        
        // Mettre à jour les champs de base (hérités de Utilisateur)
        personnel.setMatricule(personnelDetails.getMatricule());
        personnel.setNom(personnelDetails.getNom().toUpperCase().trim());
        personnel.setPrenom(capitalizeFirstLetter(personnelDetails.getPrenom().trim()));
        personnel.setEmail(personnelDetails.getEmail().toLowerCase().trim());
        personnel.setTelephone(personnelDetails.getTelephone());
        personnel.setSexe(personnelDetails.getSexe());
        personnel.setDateNaissance(personnelDetails.getDateNaissance());
        personnel.setAdresse(personnelDetails.getAdresse());
        personnel.setDateEmbauche(personnelDetails.getDateEmbauche());
        personnel.setPhotoProfil(personnelDetails.getPhotoProfil());
        personnel.setStatut(personnelDetails.getStatut());
        
        // Mettre à jour les champs spécifiques à Personnel
        personnel.setFonction(capitalizeFirstLetter(personnelDetails.getFonction().trim()));
        
        // Ne mettre à jour le mot de passe que s'il est fourni
        if (personnelDetails.getMotDePasse() != null && !personnelDetails.getMotDePasse().isEmpty()) {
            personnel.setMotDePasse(passwordEncoder.encode(personnelDetails.getMotDePasse()));
        }
        
        return personnelRepository.save(personnel);
    }

    @Override
    public void updateFonction(Long id, String nouvelleFonction) {
        Personnel personnel = personnelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Personnel non trouvé avec l'id: " + id));
        
        personnel.setFonction(capitalizeFirstLetter(nouvelleFonction.trim()));
        personnelRepository.save(personnel);
    }

    @Override
    public void deletePersonnel(Long id) {
        Personnel personnel = personnelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Personnel non trouvé avec l'id: " + id));
        
        personnelRepository.delete(personnel);
    }

    @Override
    public List<Personnel> getPersonnelByFonction(String fonction) {
        return personnelRepository.findAll().stream()
                .filter(personnel -> personnel.getFonction().equalsIgnoreCase(fonction))
                .collect(Collectors.toList());
    }

    @Override
    public long countDemandesByPersonnel(Long personnelId) {
        Personnel personnel = personnelRepository.findById(personnelId)
                .orElseThrow(() -> new RuntimeException("Personnel non trouvé avec l'id: " + personnelId));
        
        return personnel.getDemandes().size();
    }
    
    // Méthode utilitaire pour capitaliser la première lettre
    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}