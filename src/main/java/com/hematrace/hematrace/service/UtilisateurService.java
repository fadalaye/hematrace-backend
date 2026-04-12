package com.hematrace.hematrace.service;

import com.hematrace.hematrace.dto.CreateUtilisateurRequest;
import com.hematrace.hematrace.entite.Utilisateur;
import java.util.List;
import java.util.Optional;

public interface UtilisateurService {
    // Utilisateur creerUtilisateur(Utilisateur utilisateur);
    // List<Utilisateur> getAllUtilisateurs();
    // Optional<Utilisateur> getUtilisateurById(Long id);
    // Utilisateur getUtilisateurByMatricule(String matricule);
    // Utilisateur getUtilisateurByEmail(String email);
    // Utilisateur updateUtilisateur(Long id, Utilisateur utilisateurDetails);
    // void changerStatutUtilisateur(Long id, String nouveauStatut);
    // void deleteUtilisateur(Long id);
    // boolean existeUtilisateurByMatricule(String matricule);
    // boolean existeUtilisateurByEmail(String email);

        Utilisateur creerUtilisateur(Utilisateur utilisateur);

    Utilisateur creerUtilisateurParAdmin(CreateUtilisateurRequest request);

    List<Utilisateur> getAllUtilisateurs();

    Optional<Utilisateur> getUtilisateurById(Long id);

    Utilisateur getUtilisateurByMatricule(String matricule);

    Utilisateur getUtilisateurByEmail(String email);

    Utilisateur updateUtilisateur(Long id, Utilisateur utilisateurDetails);

    void changerStatutUtilisateur(Long id, String nouveauStatut);

    void deleteUtilisateur(Long id);

    boolean existeUtilisateurByMatricule(String matricule);

    boolean existeUtilisateurByEmail(String email);
}