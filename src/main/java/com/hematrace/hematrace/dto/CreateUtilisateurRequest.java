package com.hematrace.hematrace.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateUtilisateurRequest {

    @NotBlank(message = "Le matricule est obligatoire")
    private String matricule;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    private String telephone;

    @NotNull(message = "Le sexe est obligatoire")
    private Character sexe;

    @NotNull(message = "La date de naissance est obligatoire")
    private LocalDate dateNaissance;

    private String adresse;
    private LocalDate dateEmbauche;
    private String statut;

    @NotBlank(message = "Le type d'utilisateur est obligatoire")
    private String typeUtilisateur; // ADMIN, MEDECIN, PERSONNEL, CHEF_SERVICE

    private String specialite;
    private String fonction;
    private String serviceDirige;
    private String departement;
    private String role;
    private String droitsAccess;
}