package com.hematrace.hematrace.controller;

import com.hematrace.hematrace.service.ActivationService;
import com.hematrace.hematrace.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final ActivationService activationService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String identifiant = credentials.get("identifiant");
            String motDePasse = credentials.get("motDePasse");

            if (identifiant == null || identifiant.trim().isEmpty()) {
                return buildErrorResponse("L'identifiant est obligatoire", "IDENTIFIANT", HttpStatus.BAD_REQUEST);
            }

            if (motDePasse == null || motDePasse.trim().isEmpty()) {
                return buildErrorResponse("Le mot de passe est obligatoire", "MOT_DE_PASSE", HttpStatus.BAD_REQUEST);
            }

            Map<String, Object> response = authService.authenticate(identifiant, motDePasse);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            String message = e.getMessage();
            String errorType = "OTHER";
            HttpStatus status = HttpStatus.UNAUTHORIZED;

            if (message != null) {
                if (message.startsWith("IDENTIFIANT:")) {
                    errorType = "IDENTIFIANT";
                    message = message.substring(12);
                } else if (message.startsWith("MOT_DE_PASSE:")) {
                    errorType = "MOT_DE_PASSE";
                    message = message.substring(13);
                }
            } else {
                message = "Erreur d'authentification";
            }

            return buildErrorResponse(message, errorType, status);
        } catch (Exception e) {
            return buildErrorResponse("Une erreur est survenue lors de la connexion", "OTHER", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");

            if (token == null || token.trim().isEmpty()) {
                return buildErrorResponse("Token manquant", "OTHER", HttpStatus.BAD_REQUEST);
            }

            Map<String, Object> response = authService.refreshToken(token);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return buildErrorResponse(e.getMessage(), "OTHER", HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String token = authHeader.substring(7);

            Map<String, Object> user = new HashMap<>();
            user.put("id", 1);
            user.put("matricule", "TEST001");
            user.put("nom", "Test");
            user.put("prenom", "Utilisateur");
            user.put("email", "test@hematrace.com");
            user.put("role", "ADMIN");

            return ResponseEntity.ok(user);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/activer-compte")
    public ResponseEntity<?> activerCompte(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            String motDePasse = request.get("motDePasse");

            if (token == null || token.isBlank()) {
                return buildErrorResponse("Token manquant", "TOKEN", HttpStatus.BAD_REQUEST);
            }

            if (motDePasse == null || motDePasse.isBlank()) {
                return buildErrorResponse("Le mot de passe est obligatoire", "MOT_DE_PASSE", HttpStatus.BAD_REQUEST);
            }

            activationService.activerCompte(token, motDePasse);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Compte activé avec succès");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return buildErrorResponse(e.getMessage(), "ACTIVATION", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return buildErrorResponse("Erreur lors de l'activation du compte", "ACTIVATION", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<?> buildErrorResponse(String message, String errorType, HttpStatus status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        errorResponse.put("errorType", errorType);

        return ResponseEntity.status(status).body(errorResponse);
    }
}