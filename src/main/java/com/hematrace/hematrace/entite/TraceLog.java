package com.hematrace.hematrace.entite;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "trace_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TraceLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String action; // CREATE, UPDATE, DELETE, VIEW, etc.
    
    @Column(name = "entity_type", nullable = false)
    private String entityType; // PRODUIT, DEMANDE, DELIVRANCE, etc.
    
    @Column(name = "entity_id", nullable = false)
    private Long entityId;
    
    @Column(columnDefinition = "TEXT")
    private String details;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"password", "tokens", "roles"}) // Évite les références circulaires
    private Utilisateur user;
    
    // Méthode pratique pour créer un log
    public static TraceLog create(String action, String entityType, Long entityId, String details) {
        TraceLog log = new TraceLog();
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);
        log.setTimestamp(LocalDateTime.now());
        return log;
    }
}