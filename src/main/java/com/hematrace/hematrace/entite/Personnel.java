package com.hematrace.hematrace.entite;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "personnels")
@PrimaryKeyJoinColumn(name = "utilisateur_id")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Personnel extends Utilisateur {
    
    @Column(nullable = false, length = 100)
    @NotBlank(message = "La fonction est obligatoire")
    private String fonction;
    
    @OneToMany(mappedBy = "personnel", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Demande> demandes = new ArrayList<>();
    
}