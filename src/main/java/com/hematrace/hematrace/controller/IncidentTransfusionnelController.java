package com.hematrace.hematrace.controller;

import com.hematrace.hematrace.entite.IncidentTransfusionnel;
import com.hematrace.hematrace.entite.Transfusion;
import com.hematrace.hematrace.service.IncidentTransfusionnelService;
import com.hematrace.hematrace.service.StatistiquesService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/incidents-transfusionnels")
@CrossOrigin(origins = "http://localhost:4200")
public class IncidentTransfusionnelController {
    
    @Autowired
    private IncidentTransfusionnelService incidentService;
    
    @Autowired
    private StatistiquesService statistiquesService;
    
    /**
     * Crée un nouvel incident transfusionnel
     * Accepte les données JSON avec transfusionId au lieu d'un objet transfusion complet
     */
    @PostMapping
    public ResponseEntity<IncidentTransfusionnel> creerIncident(@RequestBody Map<String, Object> requestData) {
        try {
            System.out.println("📥 [CONTROLLER] Données reçues pour création d'incident");
            System.out.println("📥 Clés disponibles: " + requestData.keySet());
            
            // Convertir Map en IncidentTransfusionnel
            IncidentTransfusionnel incident = new IncidentTransfusionnel();
            
            // === VALIDATION ET MAPPAGE DES CHAMPS OBLIGATOIRES ===
            
            // 1. Transfusion ID (OBLIGATOIRE)
            Long transfusionId = null;
            if (requestData.get("transfusionId") != null) {
                transfusionId = Long.valueOf(requestData.get("transfusionId").toString());
                System.out.println("✅ TransfusionId trouvé directement: " + transfusionId);
            } else if (requestData.get("transfusion") != null) {
                // Compatibilité avec l'ancien format
                Map<String, Object> transfusionMap = (Map<String, Object>) requestData.get("transfusion");
                if (transfusionMap != null && transfusionMap.get("id") != null) {
                    transfusionId = Long.valueOf(transfusionMap.get("id").toString());
                    System.out.println("✅ TransfusionId extrait de l'objet transfusion: " + transfusionId);
                }
            }
            
            if (transfusionId == null) {
                System.err.println("❌ ERREUR: Aucun transfusionId trouvé dans les données!");
                return ResponseEntity.badRequest().body(null);
            }
            
            // Créer un objet Transfusion minimal avec juste l'ID
            Transfusion transfusion = new Transfusion();
            transfusion.setId(transfusionId);
            incident.setTransfusion(transfusion);
            
            // 2. Date incident (OBLIGATOIRE)
            if (requestData.get("dateIncident") != null) {
                incident.setDateIncident(LocalDate.parse(requestData.get("dateIncident").toString()));
            } else {
                System.err.println("❌ ERREUR: dateIncident manquant");
                return ResponseEntity.badRequest().body(null);
            }
            
            // 3. Heure incident (OBLIGATOIRE)
            if (requestData.get("heureIncident") != null) {
                String heureStr = requestData.get("heureIncident").toString();
                // S'assurer que l'heure est au format HH:mm:ss
                if (heureStr.length() == 5) { // Format HH:mm
                    heureStr += ":00";
                }
                incident.setHeureIncident(LocalTime.parse(heureStr));
            } else {
                System.err.println("❌ ERREUR: heureIncident manquant");
                return ResponseEntity.badRequest().body(null);
            }
            
            // 4. Lieu incident (OBLIGATOIRE)
            if (requestData.get("lieuIncident") != null) {
                incident.setLieuIncident(requestData.get("lieuIncident").toString());
            } else {
                System.err.println("❌ ERREUR: lieuIncident manquant");
                return ResponseEntity.badRequest().body(null);
            }
            
            // 5. Informations patient (OBLIGATOIRES)
            if (requestData.get("patientPrenom") != null) {
                incident.setPatientPrenom(requestData.get("patientPrenom").toString());
            } else {
                System.err.println("❌ ERREUR: patientPrenom manquant");
                return ResponseEntity.badRequest().body(null);
            }
            
            if (requestData.get("patientNom") != null) {
                incident.setPatientNom(requestData.get("patientNom").toString());
            } else {
                System.err.println("❌ ERREUR: patientNom manquant");
                return ResponseEntity.badRequest().body(null);
            }
            
            if (requestData.get("patientDateNaissance") != null) {
                incident.setPatientDateNaissance(LocalDate.parse(requestData.get("patientDateNaissance").toString()));
            } else {
                System.err.println("❌ ERREUR: patientDateNaissance manquant");
                return ResponseEntity.badRequest().body(null);
            }
            
            if (requestData.get("patientNumDossier") != null) {
                incident.setPatientNumDossier(requestData.get("patientNumDossier").toString());
            } else {
                System.err.println("❌ ERREUR: patientNumDossier manquant");
                return ResponseEntity.badRequest().body(null);
            }
            
            // 6. Informations produit (OBLIGATOIRES)
            if (requestData.get("typeProduitTransfuse") != null) {
                incident.setTypeProduitTransfuse(requestData.get("typeProduitTransfuse").toString());
            } else {
                System.err.println("❌ ERREUR: typeProduitTransfuse manquant");
                return ResponseEntity.badRequest().body(null);
            }
            
            if (requestData.get("numeroLotProduit") != null) {
                incident.setNumeroLotProduit(requestData.get("numeroLotProduit").toString());
            } else {
                System.err.println("❌ ERREUR: numeroLotProduit manquant");
                return ResponseEntity.badRequest().body(null);
            }
            
            if (requestData.get("datePeremptionProduit") != null) {
                incident.setDatePeremptionProduit(LocalDate.parse(requestData.get("datePeremptionProduit").toString()));
            } else {
                System.err.println("❌ ERREUR: datePeremptionProduit manquant");
                return ResponseEntity.badRequest().body(null);
            }
            
            // 7. Informations déclarant (OBLIGATOIRES)
            if (requestData.get("nomDeclarant") != null) {
                incident.setNomDeclarant(requestData.get("nomDeclarant").toString());
            } else {
                System.err.println("❌ ERREUR: nomDeclarant manquant");
                return ResponseEntity.badRequest().body(null);
            }
            
            if (requestData.get("fonctionDeclarant") != null) {
                incident.setFonctionDeclarant(requestData.get("fonctionDeclarant").toString());
            } else {
                System.err.println("❌ ERREUR: fonctionDeclarant manquant");
                return ResponseEntity.badRequest().body(null);
            }
            
            // === CHAMPS OPTIONNELS ===
            
            if (requestData.get("descriptionIncident") != null) {
                incident.setDescriptionIncident(requestData.get("descriptionIncident").toString());
            }
            
            if (requestData.get("signes") != null) {
                incident.setSignes(requestData.get("signes").toString());
            }
            
            if (requestData.get("symptomes") != null) {
                incident.setSymptomes(requestData.get("symptomes").toString());
            }
            
            if (requestData.get("actionsImmediates") != null) {
                incident.setActionsImmediates(requestData.get("actionsImmediates").toString());
            }
            
            if (requestData.get("personnesInformees") != null) {
                incident.setPersonnesInformees(requestData.get("personnesInformees").toString());
            }
            
            if (requestData.get("analysePreliminaire") != null) {
                incident.setAnalysePreliminaire(requestData.get("analysePreliminaire").toString());
            }
            
            if (requestData.get("actionsCorrectives") != null) {
                incident.setActionsCorrectives(requestData.get("actionsCorrectives").toString());
            }
            
            if (requestData.get("registreHemovigilance") != null) {
                incident.setRegistreHemovigilance(requestData.get("registreHemovigilance").toString());
            }
            
            if (requestData.get("signatureDeclarant") != null) {
                incident.setSignatureDeclarant(requestData.get("signatureDeclarant").toString());
            }
            
            System.out.println("✅ [CONTROLLER] Incident préparé avec transfusion ID: " + transfusionId);
            System.out.println("✅ Patient: " + incident.getPatientPrenom() + " " + incident.getPatientNom());
            System.out.println("✅ Produit: " + incident.getTypeProduitTransfuse());
            System.out.println("✅ Déclarant: " + incident.getNomDeclarant());
            
            // Appeler le service
            IncidentTransfusionnel saved = incidentService.creerIncident(incident);
            System.out.println("✅ [CONTROLLER] Incident créé avec succès, ID: " + saved.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
            
        } catch (NumberFormatException e) {
            System.err.println("❌ ERREUR: Format numérique invalide pour transfusionId");
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        } catch (RuntimeException e) {
            System.err.println("❌ ERREUR RuntimeException: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            System.err.println("❌ ERREUR inattendue lors de la création de l'incident: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    @GetMapping
    public ResponseEntity<List<IncidentTransfusionnel>> getAllIncidents() {
        try {
            List<IncidentTransfusionnel> incidents = incidentService.getAllIncidents();
            System.out.println("📊 Nombre d'incidents récupérés: " + incidents.size());
            return ResponseEntity.ok(incidents);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des incidents: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<IncidentTransfusionnel> getIncidentById(@PathVariable Long id) {
        try {
            Optional<IncidentTransfusionnel> incident = incidentService.getIncidentById(id);
            if (incident.isPresent()) {
                System.out.println("✅ Incident trouvé avec ID: " + id);
                return ResponseEntity.ok(incident.get());
            } else {
                System.out.println("⚠️ Incident non trouvé avec ID: " + id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération de l'incident " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/transfusion/{transfusionId}")
    public ResponseEntity<IncidentTransfusionnel> getIncidentByTransfusion(@PathVariable Long transfusionId) {
        try {
            IncidentTransfusionnel incident = incidentService.getIncidentByTransfusion(transfusionId);
            System.out.println("✅ Incident trouvé pour transfusion ID: " + transfusionId);
            return ResponseEntity.ok(incident);
        } catch (RuntimeException e) {
            System.out.println("⚠️ Aucun incident trouvé pour transfusion ID: " + transfusionId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la recherche par transfusion: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/date/{date}")
    public ResponseEntity<List<IncidentTransfusionnel>> getIncidentsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<IncidentTransfusionnel> incidents = incidentService.getIncidentsByDate(date);
            System.out.println("📅 Incidents pour la date " + date + ": " + incidents.size());
            return ResponseEntity.ok(incidents);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la recherche par date: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/date-range")
    public ResponseEntity<List<IncidentTransfusionnel>> getIncidentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<IncidentTransfusionnel> incidents = incidentService.getIncidentsByDateRange(startDate, endDate);
            System.out.println("📅 Incidents du " + startDate + " au " + endDate + ": " + incidents.size());
            return ResponseEntity.ok(incidents);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la recherche par plage de dates: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/patient")
    public ResponseEntity<List<IncidentTransfusionnel>> getIncidentsByPatient(
            @RequestParam String nom, 
            @RequestParam String prenom) {
        try {
            List<IncidentTransfusionnel> incidents = incidentService.getIncidentsByPatient(nom, prenom);
            System.out.println("👤 Incidents pour " + prenom + " " + nom + ": " + incidents.size());
            return ResponseEntity.ok(incidents);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la recherche par patient: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/dossier/{numDossier}")
    public ResponseEntity<List<IncidentTransfusionnel>> getIncidentsByNumDossier(@PathVariable String numDossier) {
        try {
            List<IncidentTransfusionnel> incidents = incidentService.getIncidentsByNumDossier(numDossier);
            System.out.println("📁 Incidents pour dossier " + numDossier + ": " + incidents.size());
            return ResponseEntity.ok(incidents);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la recherche par numéro de dossier: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/type-produit/{typeProduit}")
    public ResponseEntity<List<IncidentTransfusionnel>> getIncidentsByTypeProduit(@PathVariable String typeProduit) {
        try {
            List<IncidentTransfusionnel> incidents = incidentService.getIncidentsByTypeProduit(typeProduit);
            System.out.println("💉 Incidents pour type de produit " + typeProduit + ": " + incidents.size());
            return ResponseEntity.ok(incidents);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la recherche par type de produit: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/non-valides")
    public ResponseEntity<List<IncidentTransfusionnel>> getIncidentsNonValides() {
        try {
            List<IncidentTransfusionnel> incidents = incidentService.getIncidentsNonValides();
            System.out.println("❌ Incidents non validés: " + incidents.size());
            return ResponseEntity.ok(incidents);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des incidents non validés: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/valides")
    public ResponseEntity<List<IncidentTransfusionnel>> getIncidentsValides() {
        try {
            List<IncidentTransfusionnel> incidents = incidentService.getIncidentsValides();
            System.out.println("✅ Incidents validés: " + incidents.size());
            return ResponseEntity.ok(incidents);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des incidents validés: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<IncidentTransfusionnel> updateIncident(@PathVariable Long id, @RequestBody IncidentTransfusionnel incidentDetails) {
        try {
            System.out.println("🔄 Mise à jour de l'incident ID: " + id);
            IncidentTransfusionnel updated = incidentService.updateIncident(id, incidentDetails);
            System.out.println("✅ Incident " + id + " mis à jour avec succès");
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            System.err.println("❌ Erreur lors de la mise à jour de l'incident " + id + ": " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue lors de la mise à jour: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PatchMapping("/{id}/validation")
    public ResponseEntity<Void> validerIncident(@PathVariable Long id, @RequestParam String signatureResponsableQualite) {
        try {
            System.out.println("✅ Validation de l'incident ID: " + id);
            incidentService.validerIncident(id, signatureResponsableQualite);
            System.out.println("✅ Incident " + id + " validé avec succès");
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            System.err.println("❌ Erreur lors de la validation de l'incident " + id + ": " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue lors de la validation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncident(@PathVariable Long id) {
        try {
            System.out.println("🗑️ Suppression de l'incident ID: " + id);
            incidentService.deleteIncident(id);
            System.out.println("✅ Incident " + id + " supprimé avec succès");
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            System.err.println("❌ Erreur lors de la suppression de l'incident " + id + ": " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue lors de la suppression: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Récupère les statistiques globales des incidents
     */
    @GetMapping("/statistiques/global")
    public ResponseEntity<Map<String, Object>> getStatistiquesGlobales() {
        try {
            System.out.println("📊 Récupération des statistiques globales");
            Map<String, Object> statistiques = statistiquesService.getStatistiquesIncidentsGlobale();
            System.out.println("✅ Statistiques globales récupérées");
            return ResponseEntity.ok(statistiques);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des statistiques globales: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Récupère les statistiques par type de produit
     */
    @GetMapping("/statistiques/type-produit")
    public ResponseEntity<Map<String, Long>> getStatistiquesParTypeProduit() {
        try {
            System.out.println("📊 Récupération des statistiques par type de produit");
            Map<String, Long> statistiques = statistiquesService.getStatistiquesIncidentsParTypeProduit();
            System.out.println("✅ Statistiques par type de produit récupérées");
            return ResponseEntity.ok(statistiques);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des statistiques par type de produit: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Récupère les statistiques par mois pour une année donnée
     */
    @GetMapping("/statistiques/mois/{annee}")
    public ResponseEntity<Map<String, Long>> getStatistiquesParMois(@PathVariable int annee) {
        try {
            System.out.println("📊 Récupération des statistiques par mois pour l'année " + annee);
            Map<String, Long> statistiques = statistiquesService.getStatistiquesIncidentsParMois(annee);
            System.out.println("✅ Statistiques par mois récupérées");
            return ResponseEntity.ok(statistiques);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des statistiques par mois: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Récupère les statistiques de validation
     */
    @GetMapping("/statistiques/validation")
    public ResponseEntity<Map<String, Long>> getStatistiquesValidation() {
        try {
            System.out.println("📊 Récupération des statistiques de validation");
            Map<String, Long> statistiques = statistiquesService.getStatistiquesValidation();
            System.out.println("✅ Statistiques de validation récupérées");
            return ResponseEntity.ok(statistiques);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des statistiques de validation: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}