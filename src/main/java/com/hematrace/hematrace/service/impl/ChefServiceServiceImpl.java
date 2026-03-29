package com.hematrace.hematrace.service.impl;

import com.hematrace.hematrace.entite.ChefService;
import com.hematrace.hematrace.entite.Utilisateur;
import com.hematrace.hematrace.repository.ChefServiceRepository;
import com.hematrace.hematrace.repository.UtilsateurRepository;
import com.hematrace.hematrace.service.ChefServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChefServiceServiceImpl implements ChefServiceService {

    @Autowired
    private ChefServiceRepository chefServiceRepository;
    
    @Autowired
    private UtilsateurRepository utilisateurRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public ChefService creerChefService(ChefService chefService) {
        // Vérifier l'unicité du matricule
        if (utilisateurRepository.existsByMatricule(chefService.getMatricule())) {
            throw new RuntimeException("Le matricule existe déjà");
        }
        
        // Vérifier l'unicité de l'email
        if (utilisateurRepository.existsByEmail(chefService.getEmail())) {
            throw new RuntimeException("L'email existe déjà");
        }
        
        // Hasher le mot de passe avant sauvegarde
        chefService.setMotDePasse(passwordEncoder.encode(chefService.getMotDePasse()));
        
        // Normaliser les données
        chefService.setEmail(chefService.getEmail().toLowerCase().trim());
        chefService.setNom(chefService.getNom().toUpperCase().trim());
        chefService.setPrenom(capitalizeFirstLetter(chefService.getPrenom().trim()));
        chefService.setServiceDirige(capitalizeFirstLetter(chefService.getServiceDirige().trim()));
        chefService.setDepartement(capitalizeFirstLetter(chefService.getDepartement().trim()));
        
        // Définir le statut par défaut pour un chef de service
        chefService.setStatut("ACTIF");
        
        return chefServiceRepository.save(chefService);
    }

    @Override
    public List<ChefService> getAllChefsService() {
        return chefServiceRepository.findAll();
    }

    @Override
    public Optional<ChefService> getChefServiceById(Long id) {
        return chefServiceRepository.findById(id);
    }

    @Override
    public ChefService getChefServiceByMatricule(String matricule) {
        return chefServiceRepository.findAll().stream()
                .filter(chef -> chef.getMatricule().equals(matricule))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Chef de service non trouvé avec le matricule: " + matricule));
    }

    @Override
    public ChefService getChefServiceByEmail(String email) {
        return chefServiceRepository.findAll().stream()
                .filter(chef -> chef.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Chef de service non trouvé avec l'email: " + email));
    }

    @Override
    public ChefService updateChefService(Long id, ChefService chefServiceDetails) {
        ChefService chefService = chefServiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chef de service non trouvé avec l'id: " + id));
        
        // Vérifier si le nouveau matricule existe déjà (pour un autre utilisateur)
        if (!chefService.getMatricule().equals(chefServiceDetails.getMatricule()) && 
            utilisateurRepository.existsByMatricule(chefServiceDetails.getMatricule())) {
            throw new RuntimeException("Le matricule existe déjà pour un autre utilisateur");
        }
        
        // Vérifier si le nouvel email existe déjà (pour un autre utilisateur)
        if (!chefService.getEmail().equalsIgnoreCase(chefServiceDetails.getEmail()) && 
            utilisateurRepository.existsByEmail(chefServiceDetails.getEmail())) {
            throw new RuntimeException("L'email existe déjà pour un autre utilisateur");
        }
        
        // Mettre à jour les champs de base (hérités de Utilisateur)
        chefService.setMatricule(chefServiceDetails.getMatricule());
        chefService.setNom(chefServiceDetails.getNom().toUpperCase().trim());
        chefService.setPrenom(capitalizeFirstLetter(chefServiceDetails.getPrenom().trim()));
        chefService.setEmail(chefServiceDetails.getEmail().toLowerCase().trim());
        chefService.setTelephone(chefServiceDetails.getTelephone());
        chefService.setSexe(chefServiceDetails.getSexe());
        chefService.setDateNaissance(chefServiceDetails.getDateNaissance());
        chefService.setAdresse(chefServiceDetails.getAdresse());
        chefService.setDateEmbauche(chefServiceDetails.getDateEmbauche());
        chefService.setPhotoProfil(chefServiceDetails.getPhotoProfil());
        chefService.setStatut(chefServiceDetails.getStatut());
        
        // Mettre à jour les champs spécifiques à ChefService
        chefService.setServiceDirige(capitalizeFirstLetter(chefServiceDetails.getServiceDirige().trim()));
        chefService.setDepartement(capitalizeFirstLetter(chefServiceDetails.getDepartement().trim()));
        
        // Ne mettre à jour le mot de passe que s'il est provided
        if (chefServiceDetails.getMotDePasse() != null && !chefServiceDetails.getMotDePasse().isEmpty()) {
            chefService.setMotDePasse(passwordEncoder.encode(chefServiceDetails.getMotDePasse()));
        }
        
        return chefServiceRepository.save(chefService);
    }

    @Override
    public void updateServiceDirige(Long id, String nouveauService) {
        ChefService chefService = chefServiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chef de service non trouvé avec l'id: " + id));
        
        chefService.setServiceDirige(capitalizeFirstLetter(nouveauService.trim()));
        chefServiceRepository.save(chefService);
    }

    @Override
    public void updateDepartement(Long id, String nouveauDepartement) {
        ChefService chefService = chefServiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chef de service non trouvé avec l'id: " + id));
        
        chefService.setDepartement(capitalizeFirstLetter(nouveauDepartement.trim()));
        chefServiceRepository.save(chefService);
    }

    @Override
    public void deleteChefService(Long id) {
        ChefService chefService = chefServiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chef de service non trouvé avec l'id: " + id));
        
        chefServiceRepository.delete(chefService);
    }

    @Override
    public List<ChefService> getChefsServiceByService(String service) {
        return chefServiceRepository.findAll().stream()
                .filter(chef -> chef.getServiceDirige().equalsIgnoreCase(service))
                .collect(Collectors.toList());
    }

    @Override
    public List<ChefService> getChefsServiceByDepartement(String departement) {
        return chefServiceRepository.findAll().stream()
                .filter(chef -> chef.getDepartement().equalsIgnoreCase(departement))
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