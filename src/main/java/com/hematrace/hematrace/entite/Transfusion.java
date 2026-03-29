package com.hematrace.hematrace.entite;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "transfusion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transfusion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(optional = true)
    @JoinColumn(name = "medecin_id", nullable = false)
    private Medecin medecin;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produitSanguin_id", nullable = false)
    private ProduitSanguin produitSanguin;

     @OneToMany(mappedBy = "transfusion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference // Pour éviter la récursion infinie en JSON
    private List<Surveillance> surveillances = new ArrayList<>();

    @OneToOne(mappedBy = "transfusion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private IncidentTransfusionnel incidentTransfusionnel;


    @NotBlank(message = "Le prénom du patient est obligatoire")
    @Column(name = "patient_prenom", nullable = false, length = 100)
    private String patientPrenom;

    @NotBlank(message = "Le nom du patient est obligatoire")
    @Column(name = "patient_nom", nullable = false, length = 100)
    private String patientNom;

    @Column(name = "patient_date_naissance", nullable = false)
    private LocalDate patientDateNaissance;

    @NotBlank(message = "Le numéro de dossier est obligatoire")
    @Column(name = "patient_num_dossier", nullable = false, length = 50)
    private String patientNumDossier;

    @NotBlank(message = "Le groupe sanguin du patient est obligatoire")
    @Column(name = "groupe_sanguin_patient", nullable = false, length = 10)
    private String groupeSanguinPatient;

    @Column(name = "heure_fin")
    private LocalTime heureFin;

    @NotBlank(message = "L'état du patient après transfusion est obligatoire")
    @Column(name = "etat_patient_apres", nullable = false, columnDefinition = "TEXT")
    private String etatPatientApres;

    @NotBlank(message = "La tolérance est obligatoire (Bonne/Mauvaise)")
    @Column(name = "tolerance", nullable = false, length = 20)
    private String tolerance;

    @Column(name = "effetsindesirables", nullable = false)
    private Boolean effetsIndesirables;

    @Column(name = "typeEffet", columnDefinition = "TEXT")
    private String typeEffet;

    @NotBlank(message = "Le prénom du déclarant est obligatoire")
    @Column(name = "prenomDeclarant", nullable = false, length = 100)
    private String prenomDeclarant;

    @NotBlank(message = "Le nom du déclarant est obligatoire")
    @Column(name = "nomDeclarant", nullable = false, length = 100)
    private String nomDeclarant;

    @NotBlank(message = "La fonction du déclarant est obligatoire")
    @Column(name = "fonctionDeclarant", nullable = false, length = 100)
    private String fonctionDeclarant;

    @Column(name = "date_transfusion", nullable = false)
    private LocalDate dateTransfusion = LocalDate.now();
    
    @Column(name = "heure_debut", nullable = false)
    private LocalTime heureDebut;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "volume_ml")
    private Integer volumeMl;
    
    @Column(name = "gravite_effet", length = 20)
    private String graviteEffet;
    
    @Column(name = "date_declaration")
    private LocalDate dateDeclaration;
    
    // Méthodes de calcul
    public String getDureeTransfusion() {
        if (heureDebut == null || heureFin == null) return null;
        Duration duration = Duration.between(heureDebut, heureFin);
        return String.format("%dh%02dm", 
            duration.toHours(), 
            duration.toMinutesPart());
    }
    
    public Double getVitesseTransfusion() {
        if (volumeMl == null || heureDebut == null || heureFin == null) return null;
        long minutes = Duration.between(heureDebut, heureFin).toMinutes();
        return minutes > 0 ? (double) volumeMl / minutes : null;
    }

    public void addSurveillance(Surveillance surveillance) {
        surveillances.add(surveillance);
        surveillance.setTransfusion(this);
    }
    
    public void removeSurveillance(Surveillance surveillance) {
        surveillances.remove(surveillance);
        surveillance.setTransfusion(null);
    }
}
