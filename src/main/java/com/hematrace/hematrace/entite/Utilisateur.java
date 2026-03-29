package com.hematrace.hematrace.entite;

import lombok.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@Entity
@Table(name = "utilisateurs")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Utilisateur {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 20)
    @NotBlank(message = "Le matricule est obligatoire")
    private String matricule;
    
    @Column(nullable = false, length = 50)
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;
    
    @Column(nullable = false, length = 50)
    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;
    
    @Column(unique = true, nullable = false, length = 100)
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être une adresse valide")
    private String email;
    
    @Column(length = 20)
    private String telephone;
    
    @Column(nullable = false, length = 1)
    @NotNull(message = "Le sexe est obligatoire")
    private char sexe;
    
    @Column(name = "date_naissance", nullable = false)
    @NotNull(message = "La date de naissance est obligatoire")
    private LocalDate dateNaissance;
    
    @Column(length = 200)
    private String adresse;
    
    @Column(name = "date_embauche")
    private LocalDate dateEmbauche;
    
    @Column(name = "mot_de_passe", nullable = false, length = 255)
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String motDePasse;
    
    @Column(name = "photo_profil", length = 255)
    private String photoProfil;
    
    @Column(nullable = false, length = 20)
    @NotBlank(message = "Le statut est obligatoire")
    private String statut;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonIgnoreProperties("user")
    private List<TraceLog> traceLogs;
}