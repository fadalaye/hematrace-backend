package com.hematrace.hematrace.controller;

import com.hematrace.hematrace.entite.Surveillance;
import com.hematrace.hematrace.entite.Transfusion;
import com.hematrace.hematrace.service.SurveillanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/surveillances")
@CrossOrigin(origins = "http://localhost:4200")
public class SurveillanceController {
    
    @Autowired
    private SurveillanceService surveillanceService;
    
    // NOUVELLE VERSION qui accepte transfusionId
    @PostMapping
    public ResponseEntity<?> creerSurveillance(@RequestBody Map<String, Object> requestData) {
        System.out.println("🚀 POST /api/surveillances - Données reçues:");
        System.out.println(requestData);
        
        try {
            // 1. Extraire transfusionId du JSON
            Long transfusionId = extractTransfusionId(requestData);
            
            if (transfusionId == null) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "transfusionId est requis")
                );
            }
            
            // 2. Créer l'entité Surveillance
            Surveillance surveillance = new Surveillance();
            
            // Définir la transfusion (juste l'ID pour l'instant)
            Transfusion transfusion = new Transfusion();
            transfusion.setId(transfusionId);
            surveillance.setTransfusion(transfusion);
            
            // 3. Définir l'heure
            if (requestData.containsKey("heure")) {
                String heureStr = requestData.get("heure").toString();
                surveillance.setHeure(LocalTime.parse(heureStr));
            } else {
                surveillance.setHeure(LocalTime.now());
            }
            
            // 4. Définir les autres champs avec gestion des null
            surveillance.setTension(getStringValue(requestData, "tension", ""));
            surveillance.setSignesCliniques(getStringValue(requestData, "signesCliniques", ""));
            surveillance.setObservations(getStringValue(requestData, "observations", ""));
            
            // 5. Définir les champs numériques
            if (requestData.containsKey("temperature")) {
                Object temp = requestData.get("temperature");
                if (temp != null) {
                    surveillance.setTemperature(Double.parseDouble(temp.toString()));
                }
            }
            
            if (requestData.containsKey("pouls")) {
                Object pouls = requestData.get("pouls");
                if (pouls != null) {
                    surveillance.setPouls(Integer.parseInt(pouls.toString()));
                }
            }
            
            System.out.println("🔄 Surveillance à créer: " + surveillance);
            
            // 6. Appeler le service
            Surveillance saved = surveillanceService.creerSurveillance(surveillance);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Surveillance créée avec succès",
                "id", saved.getId(),
                "heure", saved.getHeure().toString()
            ));
            
        } catch (Exception e) {
            System.out.println("❌ ERREUR dans creerSurveillance:");
            e.printStackTrace();
            
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getClass().getSimpleName(),
                "message", e.getMessage()
            ));
        }
    }
    
    // Méthode utilitaire pour extraire transfusionId
    private Long extractTransfusionId(Map<String, Object> data) {
        try {
            // Essayer transfusionId d'abord
            if (data.containsKey("transfusionId")) {
                Object value = data.get("transfusionId");
                if (value instanceof Integer) {
                    return ((Integer) value).longValue();
                } else if (value instanceof Long) {
                    return (Long) value;
                } else {
                    return Long.parseLong(value.toString());
                }
            }
            
            // Essayer transfusion object
            if (data.containsKey("transfusion")) {
                Object transfusionObj = data.get("transfusion");
                if (transfusionObj instanceof Map) {
                    Map<?, ?> transfusionMap = (Map<?, ?>) transfusionObj;
                    if (transfusionMap.containsKey("id")) {
                        Object id = transfusionMap.get("id");
                        if (id instanceof Integer) {
                            return ((Integer) id).longValue();
                        } else if (id instanceof Long) {
                            return (Long) id;
                        } else {
                            return Long.parseLong(id.toString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️ Erreur extraction transfusionId: " + e.getMessage());
        }
        
        return null;
    }
    
    // Méthode utilitaire pour obtenir une valeur string
    private String getStringValue(Map<String, Object> data, String key, String defaultValue) {
        if (data.containsKey(key)) {
            Object value = data.get(key);
            return value != null ? value.toString() : defaultValue;
        }
        return defaultValue;
    }
    
    // Gardez le endpoint debug pour le test
    @PostMapping("/debug")
    public ResponseEntity<?> debugEndpoint(@RequestBody String rawBody) {
        System.out.println("🔧 DEBUG - Corps brut reçu:");
        System.out.println(rawBody);
        
        return ResponseEntity.ok(Map.of(
            "received", rawBody,
            "length", rawBody.length()
        ));
    }
    
    // ========== LES AUTRES MÉTHODES RESTENT INCHANGÉES ==========
    
    @GetMapping
    public ResponseEntity<List<Surveillance>> getAllSurveillances() {
        return ResponseEntity.ok(surveillanceService.getAllSurveillances());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Surveillance> getSurveillanceById(@PathVariable Long id) {
        Optional<Surveillance> surveillance = surveillanceService.getSurveillanceById(id);
        return surveillance.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/transfusion/{transfusionId}")
    public ResponseEntity<List<Surveillance>> getSurveillancesByTransfusion(@PathVariable Long transfusionId) {
        return ResponseEntity.ok(surveillanceService.getSurveillancesByTransfusion(transfusionId));
    }
    
    @GetMapping("/heure/{heure}")
    public ResponseEntity<List<Surveillance>> getSurveillancesByHeure(
            @PathVariable @DateTimeFormat(pattern = "HH:mm:ss") LocalTime heure) {
        return ResponseEntity.ok(surveillanceService.getSurveillancesByHeure(heure));
    }
    
    @GetMapping("/heure-range")
    public ResponseEntity<List<Surveillance>> getSurveillancesByHeureRange(
            @RequestParam @DateTimeFormat(pattern = "HH:mm:ss") LocalTime startHeure,
            @RequestParam @DateTimeFormat(pattern = "HH:mm:ss") LocalTime endHeure) {
        return ResponseEntity.ok(surveillanceService.getSurveillancesByHeureRange(startHeure, endHeure));
    }
    
    @GetMapping("/temperature-range")
    public ResponseEntity<List<Surveillance>> getSurveillancesByTemperatureRange(
            @RequestParam Double minTemperature,
            @RequestParam Double maxTemperature) {
        return ResponseEntity.ok(surveillanceService.getSurveillancesByTemperatureRange(minTemperature, maxTemperature));
    }
    
    @GetMapping("/pouls-range")
    public ResponseEntity<List<Surveillance>> getSurveillancesByPoulsRange(
            @RequestParam Integer minPouls,
            @RequestParam Integer maxPouls) {
        return ResponseEntity.ok(surveillanceService.getSurveillancesByPoulsRange(minPouls, maxPouls));
    }
    
    @GetMapping("/signes-cliniques")
    public ResponseEntity<List<Surveillance>> getSurveillancesBySignesCliniquesContaining(
            @RequestParam String keyword) {
        return ResponseEntity.ok(surveillanceService.getSurveillancesBySignesCliniquesContaining(keyword));
    }
    
    @GetMapping("/statistiques/transfusion/{transfusionId}")
    public ResponseEntity<Long> countSurveillancesByTransfusion(@PathVariable Long transfusionId) {
        return ResponseEntity.ok(surveillanceService.countSurveillancesByTransfusion(transfusionId));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Surveillance> updateSurveillance(@PathVariable Long id, @RequestBody Surveillance surveillanceDetails) {
        try {
            return ResponseEntity.ok(surveillanceService.updateSurveillance(id, surveillanceDetails));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSurveillance(@PathVariable Long id) {
        try {
            surveillanceService.deleteSurveillance(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}