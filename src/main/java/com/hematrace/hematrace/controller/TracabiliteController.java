package com.hematrace.hematrace.controller;

import com.hematrace.hematrace.dto.TraceElementDTO;
import com.hematrace.hematrace.service.TracabiliteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tracabilite")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
@Slf4j
@RequiredArgsConstructor
public class TracabiliteController {
    
    private final TracabiliteService tracabiliteService;
    
    // ==================== RECHERCHE GLOBALE ====================
    
    @GetMapping("/search")
    public ResponseEntity<List<TraceElementDTO>> searchTraces(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String reference,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(required = false) String utilisateur,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String query) {
        
        log.info("🔍 Requête de recherche traçabilité - Type: {}, ID: {}, Réf: {}, Date: {} à {}, User: {}, Statut: {}, Query: {}", 
                type, id, reference, dateDebut, dateFin, utilisateur, statut, query);
        
        List<TraceElementDTO> traces = tracabiliteService.searchTraces(
            type, id, reference, dateDebut, dateFin, utilisateur, statut, query
        );
        
        log.info("✅ {} traces trouvées pour la recherche", traces.size());
        return ResponseEntity.ok(traces);
    }
    
    // ==================== HISTORIQUE D'ENTITÉ ====================
    
    @GetMapping("/historique/{type}/{id}")
    public ResponseEntity<List<TraceElementDTO>> getHistoriqueEntite(
            @PathVariable String type,
            @PathVariable Long id) {
        
        log.info("📜 Requête historique pour {}/{}", type, id);
        
        List<TraceElementDTO> historique = tracabiliteService.getHistoriqueEntite(type, id);
        
        log.info("✅ Historique récupéré: {} éléments", historique.size());
        return ResponseEntity.ok(historique);
    }
    
    // ==================== STATISTIQUES ====================
    
    @GetMapping("/statistiques")
    public ResponseEntity<Map<String, Object>> getStatistiques() {
        log.info("📊 Requête statistiques traçabilité");
        
        Map<String, Object> stats = tracabiliteService.getStatistiquesTraces();
        
        log.info("✅ Statistiques récupérées avec {} catégories", stats.size());
        return ResponseEntity.ok(stats);
    }
    
    // ==================== LOGS ====================
    
    @GetMapping("/logs")
    public ResponseEntity<?> getTraceLogs(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("📋 Requête logs - EntityType: {}, EntityId: {}, Start: {}, End: {}", 
                entityType, entityId, startDate, endDate);
        
        if (entityType != null && entityId != null) {
            List<TraceElementDTO> logs = tracabiliteService.getLogsByEntity(entityType, entityId);
            log.info("✅ {} logs trouvés pour {}/{}", logs.size(), entityType, entityId);
            return ResponseEntity.ok(logs);
            
        } else if (startDate != null && endDate != null) {
            List<TraceElementDTO> logs = tracabiliteService.getLogsByDateRange(startDate, endDate);
            log.info("✅ {} logs trouvés entre {} et {}", logs.size(), startDate, endDate);
            return ResponseEntity.ok(logs);
        }
        
        log.warn("⚠️ Paramètres invalides pour la requête logs");
        return ResponseEntity.badRequest().body(Map.of(
            "error", "Paramètres invalides",
            "message", "Fournissez soit entityType/entityId, soit startDate/endDate",
            "timestamp", LocalDateTime.now()
        ));
    }
    
    // ==================== CHAÎNE D'ENTITÉ ====================
    
    @GetMapping("/chain/{type}/{id}")

    public ResponseEntity<List<TraceElementDTO>> getEntityChain(
            @PathVariable String type,
            @PathVariable Long id) {
        
        log.info("⛓️ Requête chaîne complète pour {}/{}", type, id);
        
        List<TraceElementDTO> chain = tracabiliteService.getEntityChain(type, id);
        
        log.info("✅ Chaîne récupérée: {} éléments", chain.size());
        return ResponseEntity.ok(chain);
    }
    

    @GetMapping("/chain-metier/{type}/{id}")
    public ResponseEntity<List<TraceElementDTO>> getEntityBusinessChain(
            @PathVariable String type,
            @PathVariable Long id) {

        log.info("🧬 Requête chaîne métier pour {}/{}", type, id);

        List<TraceElementDTO> chain = tracabiliteService.getEntityChain(type, id);

        log.info("✅ Chaîne métier récupérée: {} éléments", chain.size());
        return ResponseEntity.ok(chain);
    }

    // ==================== ACTIVITÉ RÉCENTE ====================
    
    @GetMapping("/recent-activity")
    public ResponseEntity<?> getRecentActivity(
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("🕐 Requête activité récente - Limit: {}", limit);
        
        try {
            List<TraceElementDTO> recentActivity = tracabiliteService.getRecentActivity(limit);
            
            log.info("✅ {} activités récentes récupérées", recentActivity.size());
            return ResponseEntity.ok(recentActivity);
            
        } catch (Exception e) {
            log.error("❌ Erreur lors de la récupération des activités récentes", e);
            
            return ResponseEntity.status(500).body(Map.of(
                "error", "Erreur de serveur",
                "message", e.getMessage(),
                "cause", e.getCause() != null ? e.getCause().getMessage() : "N/A",
                "timestamp", LocalDateTime.now()
            ));
        }
    }
    
    // ==================== ACTIVITÉ UTILISATEUR ====================
    
    @GetMapping("/user-activity/{userId}")
    public ResponseEntity<List<TraceElementDTO>> getUserActivity(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("👤 Requête activité utilisateur {} - Date: {} à {}", userId, startDate, endDate);
        
        List<TraceElementDTO> userActivity = tracabiliteService.getUserActivity(userId, startDate, endDate);
        
        log.info("✅ {} activités trouvées pour l'utilisateur {}", userActivity.size(), userId);
        return ResponseEntity.ok(userActivity);
    }
    
    // ==================== LOG D'ACTION ====================
    
    @PostMapping("/log")
    public ResponseEntity<Map<String, Object>> logAction(
            @RequestParam String action,
            @RequestParam String entityType,
            @RequestParam Long entityId,
            @RequestParam(required = false) String details,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress,
            @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        
        log.info("📝 Requête log action: {} - {} {} - Details: {}...", 
                action, entityType, entityId, 
                details != null && details.length() > 50 ? details.substring(0, 50) + "..." : details);
        
        try {
            tracabiliteService.logAction(action, entityType, entityId, details, ipAddress, userAgent);
            
            log.info("✅ Action loguée avec succès");
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Action loguée avec succès",
                "timestamp", LocalDateTime.now()
            ));
            
        } catch (Exception e) {
            log.error("❌ Erreur lors du log de l'action", e);
            
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Erreur lors du log de l'action: " + e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }
    
    // ==================== RECHERCHE PATIENTS ====================
    
    @GetMapping("/patients/search")
    public ResponseEntity<List<Map<String, Object>>> searchPatients(
            @RequestParam(required = false) String keyword) {
        
        log.info("👥 Requête recherche patients - Keyword: {}", keyword);
        
        List<Map<String, Object>> patients = tracabiliteService.searchPatients(keyword);
        
        log.info("✅ {} patients trouvés", patients.size());
        return ResponseEntity.ok(patients);
    }
    
    // ==================== HISTORIQUE PATIENT ====================
    
    @GetMapping("/patients/{numDossier}/history")
    public ResponseEntity<List<TraceElementDTO>> getPatientHistory(
            @PathVariable String numDossier) {
        
        log.info("📜 Requête historique patient: {}", numDossier);
        
        List<TraceElementDTO> history = tracabiliteService.getPatientHistory(numDossier);
        
        log.info("✅ Historique patient récupéré: {} éléments", history.size());
        return ResponseEntity.ok(history);
    }
    
    // ==================== ENDPOINTS DE SANTÉ ====================
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "OK",
            "service", "TracabiliteController",
            "timestamp", LocalDateTime.now(),
            "version", "1.0.0"
        ));
    }
    
    @GetMapping("/metrics")
public ResponseEntity<Map<String, Object>> getMetrics() {
    try {
        Map<String, Object> stats = tracabiliteService.getStatistiquesTraces();
        
        // Extraire les valeurs en utilisant getOrDefault avec conversion
        long totalProduits = getLongValue(stats, "totalProduits");
        long totalDemandes = getLongValue(stats, "totalDemandes");
        long totalLogs = getLongValue(stats, "totalLogs");
        
        Map<String, Object> response = new HashMap<>();
        response.put("entities", Map.of(
            "total", totalProduits + totalDemandes,
            "produits", totalProduits,
            "demandes", totalDemandes,
            "logs", totalLogs
        ));
        
        response.put("memory", Map.of(
            "used", Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory(),
            "max", Runtime.getRuntime().maxMemory(),
            "free", Runtime.getRuntime().freeMemory()
        ));
        
        response.put("system", Map.of(
            "processors", Runtime.getRuntime().availableProcessors(),
            "timestamp", LocalDateTime.now()
        ));
        
        return ResponseEntity.ok(response);
        
    } catch (Exception e) {
        return ResponseEntity.ok(Map.of(
            "error", "Impossible de récupérer les métriques",
            "details", e.getMessage(),
            "timestamp", LocalDateTime.now()
        ));
    }
}

// Méthode helper pour extraire les valeurs long
private long getLongValue(Map<String, Object> map, String key) {
    Object value = map.get(key);
    if (value instanceof Long) {
        return (Long) value;
    } else if (value instanceof Integer) {
        return ((Integer) value).longValue();
    } else if (value instanceof String) {
        try {
            return Long.parseLong((String) value);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
    return 0L;
}
    
    // ==================== GESTION DES ERREURS ====================
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("⚠️ Argument invalide: {}", e.getMessage());
        
        return ResponseEntity.badRequest().body(Map.of(
            "error", "Argument invalide",
            "message", e.getMessage(),
            "timestamp", LocalDateTime.now()
        ));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception e) {
        log.error("❌ Erreur inattendue dans TracabiliteController", e);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Erreur serveur");
        errorResponse.put("message", "Une erreur interne est survenue");
        errorResponse.put("details", e.getMessage() != null ? e.getMessage() : "N/A");
        errorResponse.put("cause", e.getCause() != null ? e.getCause().getMessage() : "N/A");
        errorResponse.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.status(500).body(errorResponse);
    }
}