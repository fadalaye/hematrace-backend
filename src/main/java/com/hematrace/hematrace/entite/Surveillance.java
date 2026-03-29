package com.hematrace.hematrace.entite;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "surveillance")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Surveillance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfusion_id", nullable = false)
    @JsonBackReference
    private Transfusion transfusion;

    @Column(name = "heure", nullable = false)
    private LocalTime heure;

    // SUPPRIMEZ @NotBlank et nullable=false
    @Column(name = "tension", length = 50)
    private String tension;

    // SUPPRIMEZ nullable=false
    @Column(name = "temperature")
    private Double temperature;

    // SUPPRIMEZ nullable=false
    @Column(name = "pouls")
    private Integer pouls;

    // SUPPRIMEZ @NotBlank
    @Column(name = "signes_cliniques", columnDefinition = "TEXT")
    private String signesCliniques;

    // SUPPRIMEZ @NotBlank
    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;
}