package com.hematrace.hematrace.service;

import com.hematrace.hematrace.entite.Utilisateur;
import com.hematrace.hematrace.entite.Medecin;
import com.hematrace.hematrace.entite.Personnel;
import com.hematrace.hematrace.entite.ChefService;
import com.hematrace.hematrace.entite.Admin;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {
    
    @Value("${jwt.secret:mySuperSecretKeyForHematraceApplication2024WithMinimum64CharactersLength}")
    private String jwtSecret;
    
    @Value("${jwt.expiration:3600000}")
    private int jwtExpirationInMs;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public String generateToken(Utilisateur utilisateur) {
        try {
            // Header
            Map<String, Object> header = new HashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");
            
            // Payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("sub", utilisateur.getMatricule());
            payload.put("id", utilisateur.getId());
            payload.put("matricule", utilisateur.getMatricule());
            payload.put("email", utilisateur.getEmail());
            payload.put("nom", utilisateur.getNom());
            payload.put("prenom", utilisateur.getPrenom());
            
            // Déterminer le type d'utilisateur
            if (utilisateur instanceof Medecin) {
                payload.put("type", "MEDECIN");
            } else if (utilisateur instanceof Personnel) {
                payload.put("type", "PERSONNEL");
            } else if (utilisateur instanceof ChefService) {
                payload.put("type", "CHEF_SERVICE");
            } else if (utilisateur instanceof Admin) {
                payload.put("type", "ADMIN");
            } else {
                payload.put("type", "UTILISATEUR");
            }
            
            payload.put("iat", System.currentTimeMillis() / 1000);
            payload.put("exp", (System.currentTimeMillis() + jwtExpirationInMs) / 1000);
            
            // Encoder en Base64 URL-safe
            String headerBase64 = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(objectMapper.writeValueAsBytes(header));
            String payloadBase64 = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(objectMapper.writeValueAsBytes(payload));
            
            // Signature simplifiée
            String signature = generateSignature(headerBase64, payloadBase64);
            
            return headerBase64 + "." + payloadBase64 + "." + signature;
            
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du token", e);
        }
    }
    
    private String generateSignature(String header, String payload) {
        // Signature simplifiée pour le moment
        String data = header + "." + payload + jwtSecret;
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(data.getBytes()).substring(0, 43);
    }
    
    public String getUsernameFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;
            
            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            Map<?, ?> payload = objectMapper.readValue(payloadBytes, Map.class);
            return (String) payload.get("matricule");
            
        } catch (Exception e) {
            return null;
        }
    }
    
    public boolean validateToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return false;
            
            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            Map<?, ?> payload = objectMapper.readValue(payloadBytes, Map.class);
            
            // Vérifier l'expiration
            if (payload.containsKey("exp")) {
                Long expiration = ((Number) payload.get("exp")).longValue();
                return expiration > (System.currentTimeMillis() / 1000);
            }
            
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }
}