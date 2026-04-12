package com.hematrace.hematrace.service;

import com.hematrace.hematrace.entite.*;
import com.hematrace.hematrace.repository.UtilsateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UtilsateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    public Map<String, Object> authenticate(String identifiant, String motDePasse) {
        Map<String, Object> response = new HashMap<>();
        
        // Rechercher par matricule OU email
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByMatricule(identifiant)
                .or(() -> utilisateurRepository.findByEmail(identifiant));
        
        // Cas 1: L'utilisateur n'existe pas
        if (utilisateurOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Identifiant incorrect");
            response.put("errorType", "IDENTIFIANT");
            throw new RuntimeException("IDENTIFIANT:Identifiant incorrect");
        }
        
        Utilisateur utilisateur = utilisateurOpt.get();
        
        // Cas 2: Mot de passe incorrect
        if (!passwordEncoder.matches(motDePasse, utilisateur.getMotDePasse())) {
            response.put("success", false);
            response.put("message", "Mot de passe incorrect");
            response.put("errorType", "MOT_DE_PASSE");
            throw new RuntimeException("MOT_DE_PASSE:Mot de passe incorrect");
        }
        
        // Cas 3: Compte inactif
        if (!"ACTIF".equalsIgnoreCase(utilisateur.getStatut())) {
            String status = utilisateur.getStatut() == null ? "" : utilisateur.getStatut().toUpperCase();

            String statusMessage = switch (status) {
                case "EN_ATTENTE_ACTIVATION" -> "Compte non activé. Vérifiez votre email d'activation.";
                case "INACTIF" -> "Compte inactif. Veuillez contacter l'administrateur.";
                case "SUSPENDU" -> "Compte suspendu. Veuillez contacter l'administrateur.";
                case "BLOQUE" -> "Compte bloqué. Veuillez contacter l'administrateur.";
                default -> "Compte " + utilisateur.getStatut().toLowerCase();
            };

            throw new RuntimeException(statusMessage);
        }
        
        // Cas 4: Authentification réussie
        String token = jwtTokenProvider.generateToken(utilisateur);
        
        Map<String, Object> authResponse = new HashMap<>();
        authResponse.put("success", true);
        authResponse.put("message", "Authentification réussie");
        authResponse.put("token", token);
        authResponse.put("user", createUserResponse(utilisateur));
        authResponse.put("permissions", generatePermissions(utilisateur));
        
        return authResponse;
    }
    
    private Map<String, Object> createUserResponse(Utilisateur utilisateur) {
        Map<String, Object> userResponse = new HashMap<>();
        
        // Données de base de Utilisateur
        userResponse.put("id", utilisateur.getId());
        userResponse.put("matricule", utilisateur.getMatricule());
        userResponse.put("nom", utilisateur.getNom());
        userResponse.put("prenom", utilisateur.getPrenom());
        userResponse.put("email", utilisateur.getEmail());
        userResponse.put("telephone", utilisateur.getTelephone());
        userResponse.put("sexe", String.valueOf(utilisateur.getSexe()));
        userResponse.put("dateNaissance", utilisateur.getDateNaissance() != null ? 
                         utilisateur.getDateNaissance().toString() : null);
        userResponse.put("adresse", utilisateur.getAdresse());
        userResponse.put("dateEmbauche", utilisateur.getDateEmbauche() != null ? 
                         utilisateur.getDateEmbauche().toString() : null);
        userResponse.put("photoProfil", utilisateur.getPhotoProfil());
        userResponse.put("statut", utilisateur.getStatut());
        
        // Ajouter les champs spécifiques selon le type
        if (utilisateur instanceof Medecin medecin) {
            userResponse.put("specialite", medecin.getSpecialite());
            userResponse.put("role", "MEDECIN");
        } else if (utilisateur instanceof Personnel personnel) {
            userResponse.put("fonction", personnel.getFonction());
            userResponse.put("role", "PERSONNEL");
        } else if (utilisateur instanceof ChefService chefService) {
            userResponse.put("serviceDirige", chefService.getServiceDirige());
            userResponse.put("departement", chefService.getDepartement());
            userResponse.put("role", "CHEF_SERVICE");
        } else if (utilisateur instanceof Admin admin) {
            userResponse.put("role", admin.getRole());
            userResponse.put("droitsAccess", admin.getDroitsAccess());
        } else {
            userResponse.put("role", "UTILISATEUR");
        }
        
        return userResponse;
    }
    
    private String[] generatePermissions(Utilisateur utilisateur) {
        if (utilisateur instanceof Admin) {
            return new String[]{
                "USER_MANAGEMENT", "BLOOD_PRODUCT_MANAGEMENT", "PATIENT_MANAGEMENT",
                "REPORT_VIEW", "SYSTEM_CONFIG", "DEMANDE_MANAGEMENT",
                "TRANSFUSION_MANAGEMENT", "INCIDENT_MANAGEMENT", "TRACABILITY_VIEW",
                "DELIVRANCE_MANAGEMENT"
            };
        } else if (utilisateur instanceof ChefService) {
            return new String[]{
                "BLOOD_PRODUCT_MANAGEMENT", "PATIENT_MANAGEMENT", "REPORT_VIEW",
                "DEMANDE_MANAGEMENT", "TRANSFUSION_MANAGEMENT", "INCIDENT_MANAGEMENT",
                "TRACABILITY_VIEW", "DELIVRANCE_MANAGEMENT"
            };
        } else if (utilisateur instanceof Medecin) {
            return new String[]{
                "BLOOD_PRODUCT_VIEW", "PATIENT_MANAGEMENT", "DEMANDE_CREATE",
                "DEMANDE_VIEW", "DEMANDE_MANAGE", "TRANSFUSION_CREATE",
                "TRANSFUSION_VIEW", "TRANSFUSION_MANAGE", "DELIVRANCE_VIEW",
                "INCIDENT_REPORT"
            };
        } else if (utilisateur instanceof Personnel) {
            return new String[]{
                "BLOOD_PRODUCT_VIEW", "PATIENT_MANAGEMENT", "DEMANDE_VIEW",
                "DELIVRANCE_CREATE", "DELIVRANCE_VIEW", "DELIVRANCE_MANAGE",
                "TRANSFUSION_VIEW", "INCIDENT_VIEW", "INCIDENT_MANAGEMENT",
                "TRACABILITY_VIEW", "REPORT_VIEW"
            };
        }
        
        return new String[]{"BASIC_ACCESS"};
    }
    
    public Map<String, Object> refreshToken(String token) {
        Map<String, Object> response = new HashMap<>();
        
        if (jwtTokenProvider.validateToken(token)) {
            String username = jwtTokenProvider.getUsernameFromToken(token);
            Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByMatricule(username)
                    .or(() -> utilisateurRepository.findByEmail(username));
            
            if (utilisateurOpt.isPresent()) {
                String newToken = jwtTokenProvider.generateToken(utilisateurOpt.get());
                response.put("success", true);
                response.put("token", newToken);
                return response;
            } else {
                response.put("success", false);
                response.put("message", "Utilisateur non trouvé");
                throw new RuntimeException("Utilisateur non trouvé");
            }
        } else {
            response.put("success", false);
            response.put("message", "Token invalide");
            throw new RuntimeException("Token invalide");
        }
    }
}