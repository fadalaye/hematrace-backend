package com.hematrace.hematrace.entite;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@Entity
@Table(name = "demande")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Demande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // Médecin qui fait la demande
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "medecin_id", nullable = false)
    private Medecin medecin;

    // Personnel qui valide la demande - IMPORTANT: EAGER pour charger automatiquement
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "personnel_id")
    private Personnel personnel;

    @JsonIgnore
    @OneToOne(mappedBy = "demande", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Delivrance delivrance;

    @NotNull
    @Column(name = "date_heure_demande", nullable = false)
    private LocalDateTime dateHeureDemande;

    @NotBlank(message = "Le service demandeur est obligatoire")
    @Column(name = "service_demandeur", length = 100, nullable = false)
    private String serviceDemandeur;

    @NotBlank(message = "Le prenom du patient est obligatoire")
    @Column(name = "patient_prenom", length = 100, nullable = false)
    private String patientPrenom;

    @NotBlank(message = "Le nom du patient est obligatoire")
    @Column(name = "patient_nom", length = 100, nullable = false)
    private String patientNom;

    @NotNull
    @Column(name = "patient_date_naissance", nullable = false)
    private LocalDate patientDateNaissance;

    @NotBlank
    @Column(name = "patient_num_dossier", length = 100, nullable = false)
    private String patientNumDossier;

    @NotBlank(message = "Le groupe sanguin du patient est obligatoire")
    @Column(name = "groupe_sanguin_patient", length = 10, nullable = false)
    private String groupeSanguinPatient;

    @NotBlank(message = "Le produit demandé est obligatoire")
    @Column(name = "type_produit_demande", length = 100, nullable = false)
    private String typeProduitDemande;

    @NotNull(message = "La quantité demandée est obligatoire")
    @Column(name = "quantite_demande", nullable = false)
    private Integer quantiteDemande;

    @NotBlank
    @Column(name = "indication_transfusion", length = 255, nullable = false)
    private String indicationTransfusion;

    @NotNull
    @Column(name = "urgence", nullable = false)
    private Boolean urgence;

    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;

    @Column(nullable = false)
    private String statut = "EN ATTENTE";

    @Override
    public String toString() {
        return "Demande{" +
               "id=" + id +
               ", patient='" + patientNom + " " + patientPrenom + '\'' +
               ", statut='" + statut + '\'' +
               ", personnelId=" + (personnel != null ? personnel.getId() : "null") +
               '}';
    }
}