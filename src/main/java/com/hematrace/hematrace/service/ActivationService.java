package com.hematrace.hematrace.service;

import com.hematrace.hematrace.entite.ActivationToken;
import com.hematrace.hematrace.entite.Utilisateur;
import com.hematrace.hematrace.repository.ActivationTokenRepository;
import com.hematrace.hematrace.repository.UtilsateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivationService {

    private final ActivationTokenRepository activationTokenRepository;
    private final UtilsateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public String creerToken(Utilisateur utilisateur) {
        ActivationToken activationToken = ActivationToken.builder()
                .token(UUID.randomUUID().toString())
                .utilisateur(utilisateur)
                .expiration(LocalDateTime.now().plusHours(24))
                .utilise(false)
                .build();

        activationTokenRepository.save(activationToken);
        return activationToken.getToken();
    }

    public void activerCompte(String tokenValue, String nouveauMotDePasse) {
        ActivationToken token = activationTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new RuntimeException("Token invalide"));

        if (token.isUtilise()) {
            throw new RuntimeException("Ce lien d'activation a déjà été utilisé");
        }

        if (token.getExpiration().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Le lien d'activation a expiré");
        }

        Utilisateur utilisateur = token.getUtilisateur();
        utilisateur.setMotDePasse(passwordEncoder.encode(nouveauMotDePasse));
        utilisateur.setStatut("ACTIF");

        utilisateurRepository.save(utilisateur);

        token.setUtilise(true);
        activationTokenRepository.save(token);
    }
}