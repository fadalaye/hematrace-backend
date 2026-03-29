package com.hematrace.hematrace.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CreerTransfusionDTO {
    
    @NotNull(message = "L'ID du médecin est obligatoire")
    private Long medecinId;
    
    @NotNull(message = "L'ID du produit sanguin est obligatoire")
    private Long produitSanguinId;
    
    @NotBlank(message = "Le prénom du patient est obligatoire")
    private String patientPrenom;
    
    @NotBlank(message = "Le nom du patient est obligatoire")
    private String patientNom;
    
    @NotNull(message = "La date de naissance du patient est obligatoire")
    private LocalDate patientDateNaissance;
    
    @NotBlank(message = "Le numéro de dossier est obligatoire")
    private String patientNumDossier;
    
    @NotBlank(message = "Le groupe sanguin du patient est obligatoire")
    private String groupeSanguinPatient;
    
    private LocalTime heureFin;
    
    @NotBlank(message = "L'état du patient après transfusion est obligatoire")
    private String etatPatientApres;
    
    @NotBlank(message = "La tolérance est obligatoire")
    private String tolerance;
    
    @NotNull(message = "Les effets indésirables doivent être spécifiés")
    private Boolean effetsIndesirables;
    
    private String typeEffet;
    
    @NotBlank(message = "Le prénom du déclarant est obligatoire")
    private String prenomDeclarant;
    
    @NotBlank(message = "Le nom du déclarant est obligatoire")
    private String nomDeclarant;
    
    @NotBlank(message = "La fonction du déclarant est obligatoire")
    private String fonctionDeclarant;
    
    @NotNull(message = "La date de transfusion est obligatoire")
    private LocalDate dateTransfusion;
    
    @NotNull(message = "L'heure de début est obligatoire")
    private LocalTime heureDebut;
    
    private String notes;
    private Integer volumeMl;
    private String graviteEffet;
    private LocalDate dateDeclaration;
}