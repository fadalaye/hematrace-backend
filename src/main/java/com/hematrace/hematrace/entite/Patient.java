package com.hematrace.hematrace.entite;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "patients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le prénom du patient est obligatoire")
    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;

    @NotBlank(message = "Le nom du patient est obligatoire")
    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @NotNull(message = "La date de naissance est obligatoire")
    @Column(name = "date_naissance", nullable = false)
    private LocalDate dateNaissance;

    @NotBlank(message = "Le numéro de dossier est obligatoire")
    @Column(name = "num_dossier", nullable = false, unique = true, length = 50)
    private String numDossier;

    @NotBlank(message = "Le groupe sanguin est obligatoire")
    @Column(name = "groupe_sanguin", nullable = false, length = 10)
    private String groupeSanguin;
}