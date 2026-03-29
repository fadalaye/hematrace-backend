package com.hematrace.hematrace.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TraceElementDTO {
    private Long id;
    private String type;
    private String libelle;
    private String reference;
    private String description;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime date;
    
    private String utilisateur;
    private String statut;
    private String details;
    private String lien;
    
    private Map<String, Object> entity;
    
    // Champs optionnels pour les relations (peuvent être null)
    private String relation;
    private Integer etape;
    
    // Champs de métadonnées (calculés côté front ou back selon besoin)
    private String icon;
    private String color;
    private String displayDate;
    
    // Constructeur pratique
    public TraceElementDTO(Long id, String type, String libelle, String reference, 
                          String description, LocalDateTime date, String utilisateur, 
                          String statut, String lien, Map<String, Object> entity) {
        this.id = id;
        this.type = type;
        this.libelle = libelle;
        this.reference = reference;
        this.description = description;
        this.date = date;
        this.utilisateur = utilisateur;
        this.statut = statut;
        this.lien = lien;
        this.entity = entity;
    }
}