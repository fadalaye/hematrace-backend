package com.hematrace.hematrace.service.impl;

import com.hematrace.hematrace.entite.Medecin;
import com.hematrace.hematrace.entite.Utilisateur;
import com.hematrace.hematrace.repository.MedecinRepository;
import com.hematrace.hematrace.repository.UtilsateurRepository;
import com.hematrace.hematrace.service.MedecinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class MedecinServiceImpl implements MedecinService {

    @Autowired
    private MedecinRepository medecinRepository;
    
    @Autowired
    private UtilsateurRepository utilisateurRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

        @Override
    public Medecin creerMedecin(Medecin medecin) {
        try {
            System.out.println("🔍 Validation du médecin: " + medecin.getMatricule());
            
            // Validation des champs obligatoires
            validateMedecinFields(medecin);
            
            // Vérifier l'unicité du matricule
            if (utilisateurRepository.existsByMatricule(medecin.getMatricule())) {
                throw new RuntimeException("Le matricule '" + medecin.getMatricule() + "' existe déjà");
            }
            
            // Vérifier l'unicité de l'email
            if (utilisateurRepository.existsByEmail(medecin.getEmail())) {
                throw new RuntimeException("L'email '" + medecin.getEmail() + "' existe déjà");
            }
            
            // Hasher le mot de passe avant sauvegarde
            if (medecin.getMotDePasse() == null || medecin.getMotDePasse().trim().isEmpty()) {
                throw new RuntimeException("Le mot de passe est obligatoire");
            }
            medecin.setMotDePasse(passwordEncoder.encode(medecin.getMotDePasse()));
            
            // Normaliser les données
            medecin.setEmail(medecin.getEmail().toLowerCase().trim());
            medecin.setNom(medecin.getNom().toUpperCase().trim());
            medecin.setPrenom(capitalizeFirstLetter(medecin.getPrenom().trim()));
            medecin.setSpecialite(capitalizeFirstLetter(medecin.getSpecialite().trim()));
            
            // Définir le statut par défaut pour un médecin
            medecin.setStatut("ACTIF");
            
            System.out.println("💾 Sauvegarde du médecin...");
            Medecin savedMedecin = medecinRepository.save(medecin);
            System.out.println("✅ Médecin sauvegardé avec ID: " + savedMedecin.getId());
            
            return savedMedecin;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur dans creerMedecin: " + e.getMessage());
            throw e; // Propager l'exception
        }
    }
    private void validateMedecinFields(Medecin medecin) {
        if (medecin.getMatricule() == null || medecin.getMatricule().trim().isEmpty()) {
            throw new RuntimeException("Le matricule est obligatoire");
        }
        if (medecin.getNom() == null || medecin.getNom().trim().isEmpty()) {
            throw new RuntimeException("Le nom est obligatoire");
        }
        if (medecin.getPrenom() == null || medecin.getPrenom().trim().isEmpty()) {
            throw new RuntimeException("Le prénom est obligatoire");
        }
        if (medecin.getEmail() == null || medecin.getEmail().trim().isEmpty()) {
            throw new RuntimeException("L'email est obligatoire");
        }
        if (medecin.getSexe() != 'M' && medecin.getSexe() != 'F') {
            throw new RuntimeException("Le sexe doit être 'M' ou 'F'");
        }
        if (medecin.getDateNaissance() == null) {
            throw new RuntimeException("La date de naissance est obligatoire");
        }
        if (medecin.getSpecialite() == null || medecin.getSpecialite().trim().isEmpty()) {
            throw new RuntimeException("La spécialité est obligatoire");
        }
    }

    @Override
    public List<Medecin> getAllMedecins() {
        return medecinRepository.findAll();
    }

    @Override
    public Optional<Medecin> getMedecinById(Long id) {
        return medecinRepository.findById(id);
    }

    @Override
    public Medecin getMedecinByMatricule(String matricule) {
        return medecinRepository.findAll().stream()
                .filter(medecin -> medecin.getMatricule().equals(matricule))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé avec le matricule: " + matricule));
    }

    @Override
    public Medecin getMedecinByEmail(String email) {
        return medecinRepository.findAll().stream()
                .filter(medecin -> medecin.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé avec l'email: " + email));
    }

    @Override
    public Medecin updateMedecin(Long id, Medecin medecinDetails) {
        Medecin medecin = medecinRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé avec l'id: " + id));
        
        // Vérifier si le nouveau matricule existe déjà (pour un autre utilisateur)
        if (!medecin.getMatricule().equals(medecinDetails.getMatricule()) && 
            utilisateurRepository.existsByMatricule(medecinDetails.getMatricule())) {
            throw new RuntimeException("Le matricule existe déjà pour un autre utilisateur");
        }
        
        // Vérifier si le nouvel email existe déjà (pour un autre utilisateur)
        if (!medecin.getEmail().equalsIgnoreCase(medecinDetails.getEmail()) && 
            utilisateurRepository.existsByEmail(medecinDetails.getEmail())) {
            throw new RuntimeException("L'email existe déjà pour un autre utilisateur");
        }
        
        // Mettre à jour les champs de base (hérités de Utilisateur)
        medecin.setMatricule(medecinDetails.getMatricule());
        medecin.setNom(medecinDetails.getNom().toUpperCase().trim());
        medecin.setPrenom(capitalizeFirstLetter(medecinDetails.getPrenom().trim()));
        medecin.setEmail(medecinDetails.getEmail().toLowerCase().trim());
        medecin.setTelephone(medecinDetails.getTelephone());
        medecin.setSexe(medecinDetails.getSexe());
        medecin.setDateNaissance(medecinDetails.getDateNaissance());
        medecin.setAdresse(medecinDetails.getAdresse());
        medecin.setDateEmbauche(medecinDetails.getDateEmbauche());
        medecin.setPhotoProfil(medecinDetails.getPhotoProfil());
        medecin.setStatut(medecinDetails.getStatut());
        
        // Mettre à jour les champs spécifiques à Medecin
        medecin.setSpecialite(capitalizeFirstLetter(medecinDetails.getSpecialite().trim()));
        
        // Ne mettre à jour le mot de passe que s'il est fourni
        if (medecinDetails.getMotDePasse() != null && !medecinDetails.getMotDePasse().isEmpty()) {
            medecin.setMotDePasse(passwordEncoder.encode(medecinDetails.getMotDePasse()));
        }
        
        return medecinRepository.save(medecin);
    }

    @Override
    public void updateSpecialite(Long id, String nouvelleSpecialite) {
        Medecin medecin = medecinRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé avec l'id: " + id));
        
        medecin.setSpecialite(capitalizeFirstLetter(nouvelleSpecialite.trim()));
        medecinRepository.save(medecin);
    }

    @Override
    public void deleteMedecin(Long id) {
        Medecin medecin = medecinRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé avec l'id: " + id));
        
        medecinRepository.delete(medecin);
    }

    @Override
    public List<Medecin> getMedecinsBySpecialite(String specialite) {
        return medecinRepository.findAll().stream()
                .filter(medecin -> medecin.getSpecialite().equalsIgnoreCase(specialite))
                .collect(Collectors.toList());
    }

    @Override
    public long countDemandesByMedecin(Long medecinId) {
        Medecin medecin = medecinRepository.findById(medecinId)
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé avec l'id: " + medecinId));
        
        return medecin.getDemandes().size();
    }

    @Override
    public long countTransfusionsByMedecin(Long medecinId) {
        Medecin medecin = medecinRepository.findById(medecinId)
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé avec l'id: " + medecinId));
        
        return medecin.getTransfusions().size();
    }
    
    // Méthode utilitaire pour capitaliser la première lettre
    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}