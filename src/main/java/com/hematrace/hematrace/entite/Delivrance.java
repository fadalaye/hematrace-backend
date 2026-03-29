package com.hematrace.hematrace.entite;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonInclude;

@Entity
@Table(name = "delivrances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // Ajouter cette ligne
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Delivrance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La date et l'heure de délivrance sont obligatoires")
    @Column(name = "date_heure_delivrance", nullable = false)
    @Builder.Default
    private LocalDateTime dateHeureDelivrance = LocalDateTime.now();

    @NotNull(message = "La demande associée est obligatoire")
    @OneToOne
    @JoinColumn(name = "demande_id", nullable = false)
    // MODIFIER CETTE LIGNE - Permettre la sérialisation du médecin
    @JsonIgnoreProperties({"delivrance", "hibernateLazyInitializer", "handler"})
    private Demande demande;

    @OneToMany(mappedBy = "delivrance", fetch = FetchType.EAGER)
    @JsonManagedReference
    @Builder.Default
    private List<ProduitSanguin> produitsSanguins = new ArrayList<>();

    @NotNull(message = "Le personnel est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personnel_id", nullable = false)
    @JsonIgnoreProperties({"demandes", "hibernateLazyInitializer", "handler"})
    private Personnel personnel;

    @NotBlank(message = "La destination est obligatoire")
    @Column(nullable = false)
    private String destination;

    @NotBlank(message = "Le mode de transport est obligatoire")
    @Column(nullable = false)
    private String modeTransport;

    private String observations;
}