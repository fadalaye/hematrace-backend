package com.hematrace.hematrace.entite;

import java.time.LocalDate;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "produit_sanguin")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // Ajouter cette ligne
public class ProduitSanguin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "delivrance_id")
    @JsonBackReference // Remplacer la référence circulaire
    private Delivrance delivrance;


    @OneToOne(mappedBy = "produitSanguin", fetch = FetchType.LAZY)
    @JsonIgnore
    private Transfusion transfusion;

    @NotBlank(message = "Le code du produit sanguin est obligatoire.")
    @Column(name = "code_produit", nullable = false, length = 100)
    private String codeProduit;

    @NotBlank(message = "Le Type de produit est obligatoire.")
    @Column(name = "type_produit", nullable = false, length = 100)
    private String typeProduit;
    
    @NotBlank(message = "Le groupe sanguin est obligatoire.")
    @Column(name = "groupe_sanguin", nullable = false, length = 5)
    private String groupeSanguin;

    @NotBlank(message = "Le rhesus de produit est obligatoire.")
    @Column(name = "rhesus", nullable = false, length = 5)
    private String rhesus;

    @NotNull(message = "Le volume est obligatoire.")
    @Column(name = "volume_ml", nullable = false)
    private Integer volumeMl;

    @NotNull
    @Column(name = "date_prelevement", nullable = false)
    private LocalDate datePrelevement;

    @NotNull
    @Column(name = "date_peremption", nullable = false)
    private LocalDate datePeremption;

    @NotBlank(message = "Le statut est obligatoire (ex : disponible, expiré).")
    @Column(name = "etat", nullable = false, length = 20)
    private String etat;

}