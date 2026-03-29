package com.hematrace.hematrace.entite;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "chefs_service")
@PrimaryKeyJoinColumn(name = "utilisateur_id")
@Getter
@Setter
@NoArgsConstructor
public class ChefService extends Utilisateur {
    
    @Column(name = "service_dirige", nullable = false, length = 100)
    @NotBlank(message = "Le service dirigé est obligatoire")
    private String serviceDirige;
    
    @Column(nullable = false, length = 100)
    @NotBlank(message = "Le département est obligatoire")
    private String departement;
    
    public ChefService(String serviceDirige, String departement) {
        this.serviceDirige = serviceDirige;
        this.departement = departement;
    }
}
