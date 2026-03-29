package com.hematrace.hematrace.entite;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "medecins")
@PrimaryKeyJoinColumn(name = "utilisateur_id")
@Getter
@Setter
@NoArgsConstructor
public class Medecin extends Utilisateur {
    
    @Column(nullable = false, length = 100)
    @NotBlank(message = "La spécialité est obligatoire")
    private String specialite;

    @OneToMany(mappedBy = "medecin", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Demande> demandes = new ArrayList<>();

    @OneToMany(mappedBy = "medecin", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Transfusion> transfusions = new ArrayList<>();
    
}
