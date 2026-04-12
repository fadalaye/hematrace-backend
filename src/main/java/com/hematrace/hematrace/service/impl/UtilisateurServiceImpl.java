package com.hematrace.hematrace.service.impl;

import com.hematrace.hematrace.dto.CreateUtilisateurRequest;
import com.hematrace.hematrace.entite.*;
import com.hematrace.hematrace.repository.UtilsateurRepository;
import com.hematrace.hematrace.service.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UtilisateurServiceImpl implements UtilisateurService {

    @Autowired
    private UtilsateurRepository utilisateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Utilisateur creerUtilisateur(Utilisateur utilisateur) {
        if (existeUtilisateurByMatricule(utilisateur.getMatricule())) {
            throw new RuntimeException("Le matricule existe déjà");
        }

        if (existeUtilisateurByEmail(utilisateur.getEmail())) {
            throw new RuntimeException("L'email existe déjà");
        }

        utilisateur.setEmail(utilisateur.getEmail().toLowerCase().trim());
        utilisateur.setNom(utilisateur.getNom().toUpperCase().trim());
        utilisateur.setPrenom(capitalizeFirstLetter(utilisateur.getPrenom().trim()));

        if (utilisateur.getMotDePasse() == null || utilisateur.getMotDePasse().isBlank()) {
            throw new RuntimeException("Le mot de passe est obligatoire");
        }

        utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));

        return utilisateurRepository.save(utilisateur);
    }

    @Override
    public Utilisateur creerUtilisateurParAdmin(CreateUtilisateurRequest request) {
    if (existeUtilisateurByMatricule(request.getMatricule().trim())) {
        throw new RuntimeException("Le matricule existe déjà");
    }

    if (existeUtilisateurByEmail(request.getEmail().trim().toLowerCase())) {
        throw new RuntimeException("L'email existe déjà");
    }

    Utilisateur utilisateur = buildUtilisateurFromRequest(request);

    utilisateur.setMatricule(request.getMatricule().trim());
    utilisateur.setNom(request.getNom().trim().toUpperCase());
    utilisateur.setPrenom(capitalizeFirstLetter(request.getPrenom().trim()));
    utilisateur.setEmail(request.getEmail().trim().toLowerCase());
    utilisateur.setTelephone(
        request.getTelephone() != null && !request.getTelephone().isBlank()
            ? request.getTelephone().trim()
            : null
    );
    utilisateur.setAdresse(
        request.getAdresse() != null && !request.getAdresse().isBlank()
            ? request.getAdresse().trim()
            : null
    );

    if (request.getSexe() == null) {
        throw new RuntimeException("Le sexe est obligatoire");
    }

    if (request.getDateNaissance() == null) {
        throw new RuntimeException("La date de naissance est obligatoire");
    }

    utilisateur.setSexe(Character.toUpperCase(request.getSexe()));
    utilisateur.setDateNaissance(request.getDateNaissance());
    utilisateur.setDateEmbauche(request.getDateEmbauche());

    utilisateur.setMotDePasse(passwordEncoder.encode(UUID.randomUUID().toString()));
    utilisateur.setStatut("EN_ATTENTE_ACTIVATION");

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
        return utilisateurRepository.findByMatricule(matricule)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec le matricule: " + matricule));
    }

    @Override
    public Utilisateur getUtilisateurByEmail(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'email: " + email));
    }

    @Override
    public Utilisateur updateUtilisateur(Long id, Utilisateur utilisateurDetails) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id: " + id));

        if (!utilisateur.getMatricule().equals(utilisateurDetails.getMatricule())
                && existeUtilisateurByMatricule(utilisateurDetails.getMatricule())) {
            throw new RuntimeException("Le matricule existe déjà pour un autre utilisateur");
        }

        if (!utilisateur.getEmail().equalsIgnoreCase(utilisateurDetails.getEmail())
                && existeUtilisateurByEmail(utilisateurDetails.getEmail())) {
            throw new RuntimeException("L'email existe déjà pour un autre utilisateur");
        }

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
        return utilisateurRepository.existsByMatricule(matricule);
    }

    @Override
    public boolean existeUtilisateurByEmail(String email) {
        return utilisateurRepository.existsByEmailIgnoreCase(email);
    }

    private Utilisateur buildUtilisateurFromRequest(CreateUtilisateurRequest request) {
    String type = request.getTypeUtilisateur();

    if (type == null || type.isBlank()) {
        throw new RuntimeException("Le type d'utilisateur est obligatoire");
    }

    return switch (type.trim().toUpperCase()) {
        case "MEDECIN" -> {
            if (request.getSpecialite() == null || request.getSpecialite().isBlank()) {
                throw new RuntimeException("La spécialité est obligatoire pour un médecin");
            }
            Medecin medecin = new Medecin();
            medecin.setSpecialite(request.getSpecialite().trim());
            yield medecin;
        }
        case "PERSONNEL" -> {
            if (request.getFonction() == null || request.getFonction().isBlank()) {
                throw new RuntimeException("La fonction est obligatoire pour le personnel");
            }
            Personnel personnel = new Personnel();
            personnel.setFonction(request.getFonction().trim());
            yield personnel;
        }
        case "CHEF_SERVICE" -> {
            if (request.getServiceDirige() == null || request.getServiceDirige().isBlank()) {
                throw new RuntimeException("Le service dirigé est obligatoire pour un chef de service");
            }
            if (request.getDepartement() == null || request.getDepartement().isBlank()) {
                throw new RuntimeException("Le département est obligatoire pour un chef de service");
            }
            ChefService chefService = new ChefService();
            chefService.setServiceDirige(request.getServiceDirige().trim());
            chefService.setDepartement(request.getDepartement().trim());
            yield chefService;
        }
        case "ADMIN" -> {
            if (request.getRole() == null || request.getRole().isBlank()) {
                throw new RuntimeException("Le rôle est obligatoire pour un administrateur");
            }
            Admin admin = new Admin();
            admin.setRole(request.getRole().trim());
            admin.setDroitsAccess(
                request.getDroitsAccess() != null && !request.getDroitsAccess().isBlank()
                    ? request.getDroitsAccess().trim()
                    : null
            );
            yield admin;
        }
        default -> throw new RuntimeException("Type d'utilisateur invalide : " + type);
    };
    }

    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}