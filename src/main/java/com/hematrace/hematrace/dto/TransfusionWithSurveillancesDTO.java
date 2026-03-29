package com.hematrace.hematrace.dto;

import com.hematrace.hematrace.entite.Surveillance;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class TransfusionWithSurveillancesDTO {
    
    // Champs de la transfusion
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
    
    // Incident transfusionnel (optionnel)
    @Valid
    private IncidentTransfusionnelDTO incident;
    
    // Liste des surveillances (optionnel)
    @Valid
    private List<SurveillanceDTO> surveillances = new ArrayList<>();
    
    @Data
    public static class SurveillanceDTO {
        
        @NotNull(message = "L'heure de surveillance est obligatoire")
        private LocalTime heure;
        
        @NotBlank(message = "La tension est obligatoire")
        private String tension;
        
        @NotNull(message = "La température est obligatoire")
        @DecimalMin(value = "32.0", message = "La température doit être d'au moins 32°C")
        @DecimalMax(value = "42.0", message = "La température doit être d'au plus 42°C")
        private Double temperature;
        
        @NotNull(message = "Le pouls est obligatoire")
        @Min(value = 30, message = "Le pouls doit être d'au moins 30 bpm")
        @Max(value = 200, message = "Le pouls doit être d'au plus 200 bpm")
        private Integer pouls;
        
        @NotBlank(message = "Les signes cliniques sont obligatoires")
        private String signesCliniques;
        
        private String observations;
    }
    
    @Data
    public static class IncidentTransfusionnelDTO {
        
        @NotNull(message = "La date de l'incident est obligatoire")
        private LocalDate dateIncident;
        
        @NotNull(message = "L'heure de l'incident est obligatoire")
        private LocalTime heureIncident;
        
        @NotBlank(message = "Le lieu de l'incident est obligatoire")
        private String lieuIncident;
        
        @NotBlank(message = "Le type de produit transfusé est obligatoire")
        private String typeProduitTransfuse;
        
        @NotBlank(message = "Le numéro de lot du produit est obligatoire")
        private String numeroLotProduit;
        
        @NotNull(message = "La date de péremption du produit est obligatoire")
        private LocalDate datePeremptionProduit;
        
        private String descriptionIncident;
        private String signes;
        private String symptomes;
        private String actionsImmediates;
        private String personnesInformees;
        private String analysePreliminaire;
        private String actionsCorrectives;
        
        @NotBlank(message = "Le nom du déclarant est obligatoire")
        private String nomDeclarant;
        
        @NotBlank(message = "La fonction du déclarant est obligatoire")
        private String fonctionDeclarant;
        
        private String registreHemovigilance;
    }
}