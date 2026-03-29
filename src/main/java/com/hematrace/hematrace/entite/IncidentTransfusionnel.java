package com.hematrace.hematrace.entite;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@Entity
@Table(name = "incident_transfusionnel")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IncidentTransfusionnel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // Mettre nullable = false si c'est toujours requis
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfusion_id", nullable = false)
    @JsonIgnoreProperties({"incidentTransfusionnel", "hibernateLazyInitializer", "handler"})
    private Transfusion transfusion;

    @NotNull(message = "La date de l'incident est obligatoire") // ← AJOUTER
    @Column(name = "date_incident", nullable = false)
    private LocalDate dateIncident;

    @NotNull(message = "L'heure de l'incident est obligatoire") // ← AJOUTER
    @Column(name = "heure_incident", nullable = false)
    private LocalTime heureIncident;

    @NotBlank(message = "Le lieu de l'incident est obligatoire")
    @Column(name = "lieu_incident", length = 255, nullable = false)
    private String lieuIncident;

    @NotBlank(message = "Le prénom du patient est obligatoire")
    @Column(name = "patient_prenom", length = 100, nullable = false)
    private String patientPrenom;

    @NotBlank(message = "Le nom du patient est obligatoire")
    @Column(name = "patient_nom", length = 100, nullable = false)
    private String patientNom;

    @NotNull(message = "La date de naissance du patient est obligatoire") // ← AJOUTER
    @Column(name = "patient_date_naissance", nullable = false)
    private LocalDate patientDateNaissance;

    @NotBlank(message = "Le numéro de dossier patient est obligatoire")
    @Column(name = "patient_num_dossier", length = 50, nullable = false)
    private String patientNumDossier;

    @NotBlank(message = "Le type de produit transfusé est obligatoire")
    @Column(name = "type_produit_transfuse", length = 100, nullable = false)
    private String typeProduitTransfuse;

    @NotBlank(message = "Le numéro de lot du produit est obligatoire")
    @Column(name = "numero_lot_produit", length = 100, nullable = false)
    private String numeroLotProduit;

    @NotNull(message = "La date de péremption du produit est obligatoire") // ← AJOUTER
    @Column(name = "date_peremption_produit", nullable = false)
    private LocalDate datePeremptionProduit;

    @Column(name = "description_incident", columnDefinition = "TEXT")
    private String descriptionIncident;

    @Column(name = "signes", columnDefinition = "TEXT")
    private String signes;

    @Column(name = "symptomes", columnDefinition = "TEXT")
    private String symptomes;

    @Column(name = "actions_immediates", columnDefinition = "TEXT")
    private String actionsImmediates;

    @Column(name = "personnes_informees", columnDefinition = "TEXT")
    private String personnesInformees;

    @Column(name = "analyse_preliminaire", columnDefinition = "TEXT")
    private String analysePreliminaire;

    @Column(name = "actions_correctives", columnDefinition = "TEXT")
    private String actionsCorrectives;

    @NotNull(message = "La date/heure de déclaration est obligatoire") // ← AJOUTER
    @Column(name = "date_heure_declaration", nullable = false)
    private LocalDateTime dateHeureDeclaration;

    @NotBlank(message = "Le nom du déclarant est obligatoire")
    @Column(name = "nom_declarant", length = 100, nullable = false)
    private String nomDeclarant;

    @NotBlank(message = "La fonction du déclarant est obligatoire")
    @Column(name = "fonction_declarant", length = 100, nullable = false)
    private String fonctionDeclarant;

    @Column(name = "registre_hemovigilance", length = 100)
    private String registreHemovigilance;

    @Column(name = "signature_declarant", length = 255)
    private String signatureDeclarant;

    @Column(name = "signature_responsable_qualite", length = 255)
    private String signatureResponsableQualite;

    @Column(name = "date_validation")
    private LocalDate dateValidation;
}
