package com.hematrace.hematrace.controller;

import com.hematrace.hematrace.dto.CreateUtilisateurRequest;
import com.hematrace.hematrace.entite.Utilisateur;
import com.hematrace.hematrace.service.ActivationService;
import com.hematrace.hematrace.service.EmailService;
import com.hematrace.hematrace.service.UtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/utilisateurs")
@RequiredArgsConstructor
public class AdminUtilisateurController {

    private final UtilisateurService utilisateurService;
    private final ActivationService activationService;
    private final EmailService emailService;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @PostMapping
    public ResponseEntity<?> creerUtilisateurParAdmin(@Valid @RequestBody CreateUtilisateurRequest request) {
        try {
            Utilisateur utilisateur = utilisateurService.creerUtilisateurParAdmin(request);
            String token = activationService.creerToken(utilisateur);

            String lienActivation = frontendUrl + "/activer-compte?token=" + token;

            emailService.envoyerMailActivation(
                    utilisateur.getEmail(),
                    utilisateur.getPrenom(),
                    lienActivation
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Compte créé avec succès. Email d'activation envoyé.",
                    "email", utilisateur.getEmail(),
                    "statut", utilisateur.getStatut()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Erreur lors de la création du compte"
            ));
        }
    }
}