package com.hematrace.hematrace.service.impl;

import com.hematrace.hematrace.entite.Demande;
import com.hematrace.hematrace.entite.Medecin;
import com.hematrace.hematrace.entite.Personnel;
import com.hematrace.hematrace.repository.DemandeRepository;
import com.hematrace.hematrace.repository.MedecinRepository;
import com.hematrace.hematrace.repository.PersonnelRepository;
import com.hematrace.hematrace.service.DemandeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class DemandeServiceImpl implements DemandeService {

    private final DemandeRepository demandeRepository;
    private final MedecinRepository medecinRepository;
    private final PersonnelRepository personnelRepository;

    @Override
    public Demande creerDemande(Demande demande) {
        log.info("Création d'une nouvelle demande");
        
        // Validation du médecin
        if (demande.getMedecin() == null || demande.getMedecin().getId() == null) {
            throw new RuntimeException("Un médecin doit être associé à la demande");
        }
        
        Medecin medecin = medecinRepository.findById(demande.getMedecin().getId())
            .orElseThrow(() -> new RuntimeException("Médecin non trouvé"));
        demande.setMedecin(medecin);
        
        // Date de demande automatique
        if (demande.getDateHeureDemande() == null) {
            demande.setDateHeureDemande(LocalDateTime.now());
        }
        
        // Statut par défaut
        if (demande.getStatut() == null) {
            demande.setStatut("EN ATTENTE");
        }
        
        Demande savedDemande = demandeRepository.save(demande);
        log.info("Demande créée avec ID: {}", savedDemande.getId());
        
        return savedDemande;
    }

    @Override
    public List<Demande> getAllDemandes() {
        log.info("Récupération de toutes les demandes");
        try {
            // Utilise la méthode avec JOIN FETCH pour charger toutes les relations
            List<Demande> demandes = demandeRepository.findAllWithRelations();
            
            // Log de débogage
            log.debug("Nombre total de demandes récupérées: {}", demandes.size());
            
            // Vérifie les demandes validées
            List<Demande> demandesValidees = demandes.stream()
                .filter(d -> "VALIDÉE".equals(d.getStatut()))
                .collect(Collectors.toList());
            
            log.debug("Nombre de demandes validées: {}", demandesValidees.size());
            
            for (Demande demande : demandesValidees) {
                log.debug("Demande validée ID {} - Personnel présent: {}",
                    demande.getId(), demande.getPersonnel() != null);
                
                if (demande.getPersonnel() != null) {
                    log.debug("  Validateur: {} {} (ID: {})", 
                        demande.getPersonnel().getPrenom(),
                        demande.getPersonnel().getNom(),
                        demande.getPersonnel().getId());
                }
            }
            
            return demandes;
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des demandes: {}", e.getMessage());
            // Fallback sur la méthode standard
            return demandeRepository.findAll();
        }
    }

    @Override
    public Optional<Demande> getDemandeById(Long id) {
        log.info("Récupération de la demande ID: {}", id);
        // Utilise la méthode qui charge les relations
        return demandeRepository.findByIdWithRelations(id);
    }

    @Override
    public Demande updateDemande(Long id, Demande demandeDetails) {
        log.info("Mise à jour de la demande ID: {}", id);
        
        Demande demande = demandeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Demande non trouvée avec l'id: " + id));
        
        // Mise à jour des champs modifiables
        if (demandeDetails.getServiceDemandeur() != null) {
            demande.setServiceDemandeur(demandeDetails.getServiceDemandeur());
        }
        if (demandeDetails.getPatientPrenom() != null) {
            demande.setPatientPrenom(demandeDetails.getPatientPrenom());
        }
        if (demandeDetails.getPatientNom() != null) {
            demande.setPatientNom(demandeDetails.getPatientNom());
        }
        if (demandeDetails.getPatientDateNaissance() != null) {
            demande.setPatientDateNaissance(demandeDetails.getPatientDateNaissance());
        }
        if (demandeDetails.getPatientNumDossier() != null) {
            demande.setPatientNumDossier(demandeDetails.getPatientNumDossier());
        }
        if (demandeDetails.getGroupeSanguinPatient() != null) {
            demande.setGroupeSanguinPatient(demandeDetails.getGroupeSanguinPatient());
        }
        if (demandeDetails.getTypeProduitDemande() != null) {
            demande.setTypeProduitDemande(demandeDetails.getTypeProduitDemande());
        }
        if (demandeDetails.getQuantiteDemande() != null) {
            demande.setQuantiteDemande(demandeDetails.getQuantiteDemande());
        }
        if (demandeDetails.getIndicationTransfusion() != null) {
            demande.setIndicationTransfusion(demandeDetails.getIndicationTransfusion());
        }
        if (demandeDetails.getUrgence() != null) {
            demande.setUrgence(demandeDetails.getUrgence());
        }
        if (demandeDetails.getObservations() != null) {
            demande.setObservations(demandeDetails.getObservations());
        }
        
        Demande updatedDemande = demandeRepository.save(demande);
        log.info("Demande ID {} mise à jour avec succès", id);
        
        return updatedDemande;
    }

    @Override
    public void updateStatutDemande(Long id, String nouveauStatut) {
        log.info("Changement de statut pour la demande ID: {} -> {}", id, nouveauStatut);
        
        Demande demande = demandeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Demande non trouvée avec l'id: " + id));
        
        demande.setStatut(nouveauStatut);
        
        // Si on passe à un autre statut que "VALIDÉE", on enlève le personnel validateur
        if (!"VALIDÉE".equals(nouveauStatut)) {
            demande.setPersonnel(null);
        }
        
        demandeRepository.save(demande);
        log.info("Statut de la demande ID {} changé à {}", id, nouveauStatut);
    }

    @Override
    public Demande validerDemande(Long demandeId, Long personnelId) {
        log.info("Validation de la demande ID: {} par le personnel ID: {}", demandeId, personnelId);
        
        Demande demande = demandeRepository.findById(demandeId)
            .orElseThrow(() -> new RuntimeException("Demande non trouvée"));
            
        Personnel personnel = personnelRepository.findById(personnelId)
            .orElseThrow(() -> new RuntimeException("Personnel non trouvé"));
        
        if (!peutEtreValidee(demandeId)) {
            throw new RuntimeException("La demande ne peut pas être validée - stocks insuffisants");
        }
        
        demande.setStatut("VALIDÉE");
        demande.setPersonnel(personnel);
        demande.setDateHeureDemande(LocalDateTime.now()); // Mise à jour de la date
        
        Demande savedDemande = demandeRepository.save(demande);
        
        log.info("✅ Demande ID {} validée avec succès par {} {} (ID: {})", 
            demandeId, 
            personnel.getPrenom(), 
            personnel.getNom(),
            personnel.getId());
        
        return savedDemande;
    }

    @Override
    public void annulerDemande(Long demandeId) {
        log.info("Annulation de la demande ID: {}", demandeId);
        
        Demande demande = demandeRepository.findById(demandeId)
            .orElseThrow(() -> new RuntimeException("Demande non trouvée"));
        
        if ("LIVRÉE".equals(demande.getStatut())) {
            throw new RuntimeException("Impossible d'annuler une demande déjà livrée");
        }
        
        demande.setStatut("ANNULÉE");
        demande.setPersonnel(null); // Enlève le personnel validateur
        
        demandeRepository.save(demande);
        log.info("Demande ID {} annulée", demandeId);
    }

    @Override
    public void deleteDemande(Long id) {
        log.info("Suppression de la demande ID: {}", id);
        
        Demande demande = demandeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Demande non trouvée"));
            
        if (demande.getDelivrance() != null) {
            throw new RuntimeException("Impossible de supprimer une demande avec une délivrance associée");
        }
        
        demandeRepository.deleteById(id);
        log.info("Demande ID {} supprimée", id);
    }

    @Override
    public List<Demande> getDemandesByMedecin(Long medecinId) {
        log.info("Récupération des demandes pour le médecin ID: {}", medecinId);
        return demandeRepository.findByMedecinId(medecinId);
    }

    @Override
    public List<Demande> getDemandesByPersonnel(Long personnelId) {
        log.info("Récupération des demandes validées par le personnel ID: {}", personnelId);
        // Utilise la méthode avec relations pour éviter les problèmes de lazy loading
        return demandeRepository.findAllWithRelations().stream()
            .filter(d -> d.getPersonnel() != null && d.getPersonnel().getId().equals(personnelId))
            .collect(Collectors.toList());
    }

    @Override
    public List<Demande> getDemandesByStatut(String statut) {
        log.info("Récupération des demandes avec statut: {}", statut);
        return demandeRepository.findByStatut(statut);
    }

    @Override
    public List<Demande> getDemandesByDate(LocalDate date) {
        log.info("Récupération des demandes pour la date: {}", date);
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return demandeRepository.findByDate(start, end);
    }

    @Override
    public List<Demande> getDemandesByService(String service) {
        log.info("Récupération des demandes pour le service: {}", service);
        return demandeRepository.findByServiceDemandeur(service);
    }

    @Override
    public List<Demande> getDemandesByGroupeSanguin(String groupeSanguin) {
        log.info("Récupération des demandes pour le groupe sanguin: {}", groupeSanguin);
        // Utilise la méthode avec relations
        return demandeRepository.findAllWithRelations().stream()
            .filter(d -> d.getGroupeSanguinPatient().equalsIgnoreCase(groupeSanguin))
            .collect(Collectors.toList());
    }

    @Override
    public List<Demande> getDemandesByTypeProduit(String typeProduit) {
        log.info("Récupération des demandes pour le type de produit: {}", typeProduit);
        return demandeRepository.findByTypeProduitDemande(typeProduit);
    }

    @Override
    public List<Demande> getDemandesUrgentes() {
        log.info("Récupération des demandes urgentes");
        return demandeRepository.findByUrgence(true);
    }

    @Override
    public List<Demande> getDemandesNonUrgentes() {
        log.info("Récupération des demandes non urgentes");
        return demandeRepository.findByUrgence(false);
    }

    @Override
    public List<Demande> getDemandesByStatutAndUrgence(String statut, Boolean urgence) {
        log.info("Récupération des demandes - Statut: {}, Urgence: {}", statut, urgence);
        return demandeRepository.findByStatutAndUrgence(statut, urgence);
    }

    @Override
    public List<Demande> getDemandesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Récupération des demandes entre {} et {}", startDate, endDate);
        return demandeRepository.findAllWithRelations().stream()
            .filter(d -> !d.getDateHeureDemande().isBefore(startDate) && !d.getDateHeureDemande().isAfter(endDate))
            .collect(Collectors.toList());
    }

    @Override
    public boolean peutEtreValidee(Long demandeId) {
        log.debug("Vérification si la demande ID {} peut être validée", demandeId);
        // Pour l'instant, on retourne toujours true
        // À implémenter avec la logique de vérification des stocks
        return true;
    }

    @Override
    public boolean verifierStocksSuffisants(Long demandeId) {
        log.debug("Vérification des stocks pour la demande ID {}", demandeId);
        Demande demande = demandeRepository.findById(demandeId)
            .orElseThrow(() -> new RuntimeException("Demande non trouvée"));
        
        // Logique de vérification des stocks à implémenter
        // Retourne true pour l'instant
        return true;
    }

    @Override
    public long countDemandesByStatut(String statut) {
        return demandeRepository.countByStatut(statut);
    }

    @Override
    public long countDemandesUrgentes() {
        return demandeRepository.findByUrgence(true).size();
    }

    @Override
    public Map<String, Long> getStatistiquesDemandes() {
        log.info("Génération des statistiques des demandes");
        
        Map<String, Long> stats = new HashMap<>();
        stats.put("TOTAL", demandeRepository.count());
        stats.put("EN_ATTENTE", countDemandesByStatut("EN ATTENTE"));
        stats.put("VALIDEES", countDemandesByStatut("VALIDÉE"));
        stats.put("REJETEES", countDemandesByStatut("REJETÉE"));
        stats.put("LIVREES", countDemandesByStatut("LIVRÉE"));
        stats.put("ANNULEES", countDemandesByStatut("ANNULÉE"));
        stats.put("URGENTES", countDemandesUrgentes());
        
        return stats;
    }
    
    // Méthode utilitaire pour débogage
    public Map<String, Object> getDebugInfo() {
        Map<String, Object> debugInfo = new HashMap<>();
        
        List<Demande> demandes = demandeRepository.findAllWithRelations();
        debugInfo.put("totalDemandes", demandes.size());
        
        List<Demande> demandesValidees = demandes.stream()
            .filter(d -> "VALIDÉE".equals(d.getStatut()))
            .collect(Collectors.toList());
        
        debugInfo.put("demandesValidees", demandesValidees.size());
        
        List<Map<String, Object>> detailsValidees = demandesValidees.stream()
            .map(d -> {
                Map<String, Object> detail = new HashMap<>();
                detail.put("id", d.getId());
                detail.put("patient", d.getPatientNom() + " " + d.getPatientPrenom());
                detail.put("date", d.getDateHeureDemande());
                detail.put("personnelPresent", d.getPersonnel() != null);
                if (d.getPersonnel() != null) {
                    detail.put("personnelId", d.getPersonnel().getId());
                    detail.put("personnelNom", d.getPersonnel().getNom() + " " + d.getPersonnel().getPrenom());
                }
                return detail;
            })
            .collect(Collectors.toList());
        
        debugInfo.put("detailsValidees", detailsValidees);
        
        return debugInfo;
    }
}