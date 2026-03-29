package com.hematrace.hematrace.service.impl;

import com.hematrace.hematrace.entite.Utilisateur;
import com.hematrace.hematrace.repository.UtilsateurRepository;
import com.hematrace.hematrace.service.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UtilisateurServiceImpl implements UtilisateurService {

    @Autowired
    private UtilsateurRepository utilisateurRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Utilisateur creerUtilisateur(Utilisateur utilisateur) {
        // Vérifier l'unicité du matricule
        if (existeUtilisateurByMatricule(utilisateur.getMatricule())) {
            throw new RuntimeException("Le matricule existe déjà");
        }
        
        // Vérifier l'unicité de l'email
        if (existeUtilisateurByEmail(utilisateur.getEmail())) {
            throw new RuntimeException("L'email existe déjà");
        }
        
        // Hasher le mot de passe avant sauvegarde
        utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
        
        // Normaliser les données
        utilisateur.setEmail(utilisateur.getEmail().toLowerCase().trim());
        utilisateur.setNom(utilisateur.getNom().toUpperCase().trim());
        utilisateur.setPrenom(capitalizeFirstLetter(utilisateur.getPrenom().trim()));
        
        return utilisateurRepository.save(utilisateur);
    }

    @Override
    public List<Utilisateur> getAllUtilisateurs() {
        return utilisateurRepository.findAll();
    }

    @Override
    public Optional<Utilisateur> getUtilisateurById(Long id) {
        return utilisateurRepository.findById(id);
    }

    @Override
    public Utilisateur getUtilisateurByMatricule(String matricule) {
        return utilisateurRepository.findAll().stream()
                .filter(u -> u.getMatricule().equals(matricule))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec le matricule: " + matricule));
    }

    @Override
    public Utilisateur getUtilisateurByEmail(String email) {
        return utilisateurRepository.findAll().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'email: " + email));
    }

    @Override
    public Utilisateur updateUtilisateur(Long id, Utilisateur utilisateurDetails) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id: " + id));
        
        // Vérifier si le nouveau matricule existe déjà (pour un autre utilisateur)
        if (!utilisateur.getMatricule().equals(utilisateurDetails.getMatricule()) && 
            existeUtilisateurByMatricule(utilisateurDetails.getMatricule())) {
            throw new RuntimeException("Le matricule existe déjà pour un autre utilisateur");
        }
        
        // Vérifier si le nouvel email existe déjà (pour un autre utilisateur)
        if (!utilisateur.getEmail().equalsIgnoreCase(utilisateurDetails.getEmail()) && 
            existeUtilisateurByEmail(utilisateurDetails.getEmail())) {
            throw new RuntimeException("L'email existe déjà pour un autre utilisateur");
        }
        
        // Mettre à jour les champs
        utilisateur.setMatricule(utilisateurDetails.getMatricule());
        utilisateur.setNom(utilisateurDetails.getNom().toUpperCase().trim());
        utilisateur.setPrenom(capitalizeFirstLetter(utilisateurDetails.getPrenom().trim()));
        utilisateur.setEmail(utilisateurDetails.getEmail().toLowerCase().trim());
        utilisateur.setTelephone(utilisateurDetails.getTelephone());
        utilisateur.setSexe(utilisateurDetails.getSexe());
        utilisateur.setDateNaissance(utilisateurDetails.getDateNaissance());
        utilisateur.setAdresse(utilisateurDetails.getAdresse());
        utilisateur.setDateEmbauche(utilisateurDetails.getDateEmbauche());
        utilisateur.setPhotoProfil(utilisateurDetails.getPhotoProfil());
        utilisateur.setStatut(utilisateurDetails.getStatut());
        
        // Ne mettre à jour le mot de passe que s'il est fourni
        if (utilisateurDetails.getMotDePasse() != null && !utilisateurDetails.getMotDePasse().isEmpty()) {
            utilisateur.setMotDePasse(passwordEncoder.encode(utilisateurDetails.getMotDePasse()));
        }
        
        return utilisateurRepository.save(utilisateur);
    }

    @Override
    public void changerStatutUtilisateur(Long id, String nouveauStatut) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id: " + id));
        
        utilisateur.setStatut(nouveauStatut);
        utilisateurRepository.save(utilisateur);
    }

    @Override
    public void deleteUtilisateur(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id: " + id));
        
        utilisateurRepository.delete(utilisateur);
    }

    @Override
    public boolean existeUtilisateurByMatricule(String matricule) {
        return utilisateurRepository.findAll().stream()
                .anyMatch(u -> u.getMatricule().equals(matricule));
    }

    @Override
    public boolean existeUtilisateurByEmail(String email) {
        return utilisateurRepository.findAll().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
    }
    
    // Méthode utilitaire pour capitaliser la première lettre
    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}