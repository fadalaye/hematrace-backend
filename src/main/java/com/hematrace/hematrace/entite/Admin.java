package com.hematrace.hematrace.entite;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "admins")
@PrimaryKeyJoinColumn(name = "utilisateur_id")
@Getter
@Setter
@NoArgsConstructor
public class Admin extends Utilisateur {
    
    @Column(nullable = false, length = 50)
    @NotBlank(message = "Le rôle est obligatoire")
    private String role;
    
    @Column(name = "droits_access", length = 255)
    private String droitsAccess;
    
}