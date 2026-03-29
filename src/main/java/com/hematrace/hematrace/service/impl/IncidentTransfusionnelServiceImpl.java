package com.hematrace.hematrace.service.impl;

import com.hematrace.hematrace.entite.IncidentTransfusionnel;
import com.hematrace.hematrace.entite.Transfusion;
import com.hematrace.hematrace.repository.IncidentTransfusionnelRepository;
import com.hematrace.hematrace.repository.TransfusionRepository;
import com.hematrace.hematrace.service.IncidentTransfusionnelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class IncidentTransfusionnelServiceImpl implements IncidentTransfusionnelService {

    @Autowired
    private IncidentTransfusionnelRepository incidentRepository;
    
    @Autowired
    private TransfusionRepository transfusionRepository;

    @Override
    public IncidentTransfusionnel creerIncident(IncidentTransfusionnel incident) {
        // Validation des relations obligatoires
        if (incident.getTransfusion() == null || incident.getTransfusion().getId() == null) {
            throw new RuntimeException("Une transfusion doit être associée à l'incident");
        }
        
        // Vérifier que la transfusion existe
        Transfusion transfusion = transfusionRepository.findById(incident.getTransfusion().getId())
                .orElseThrow(() -> new RuntimeException("Transfusion non trouvée avec l'id: " + incident.getTransfusion().getId()));
        
        // Définir les dates si non spécifiées
        if (incident.getDateIncident() == null) {
            incident.setDateIncident(LocalDate.now());
        }
        
        if (incident.getHeureIncident() == null) {
            incident.setHeureIncident(java.time.LocalTime.now());
        }
        
        if (incident.getDateHeureDeclaration() == null) {
            incident.setDateHeureDeclaration(LocalDateTime.now());
        }
        
        // Normaliser les données
        incident.setLieuIncident(capitalizeFirstLetter(incident.getLieuIncident().trim()));
        incident.setPatientPrenom(capitalizeFirstLetter(incident.getPatientPrenom().trim()));
        incident.setPatientNom(incident.getPatientNom().toUpperCase().trim());
        incident.setPatientNumDossier(incident.getPatientNumDossier().toUpperCase().trim());
        incident.setTypeProduitTransfuse(capitalizeFirstLetter(incident.getTypeProduitTransfuse().trim()));
        incident.setNumeroLotProduit(incident.getNumeroLotProduit().toUpperCase().trim());
        incident.setNomDeclarant(incident.getNomDeclarant().toUpperCase().trim());
        incident.setFonctionDeclarant(capitalizeFirstLetter(incident.getFonctionDeclarant().trim()));
        
        if (incident.getDescriptionIncident() != null) {
            incident.setDescriptionIncident(incident.getDescriptionIncident().trim());
        }
        
        if (incident.getSignes() != null) {
            incident.setSignes(incident.getSignes().trim());
        }
        
        if (incident.getSymptomes() != null) {
            incident.setSymptomes(incident.getSymptomes().trim());
        }
        
        if (incident.getActionsImmediates() != null) {
            incident.setActionsImmediates(incident.getActionsImmediates().trim());
        }
        
        if (incident.getPersonnesInformees() != null) {
            incident.setPersonnesInformees(incident.getPersonnesInformees().trim());
        }
        
        if (incident.getRegistreHemovigilance() != null) {
            incident.setRegistreHemovigilance(incident.getRegistreHemovigilance().trim());
        }
        
        if (incident.getAnalysePreliminaire() != null) {
            incident.setAnalysePreliminaire(incident.getAnalysePreliminaire().trim());
        }
        
        if (incident.getActionsCorrectives() != null) {
            incident.setActionsCorrectives(incident.getActionsCorrectives().trim());
        }
        
        if (incident.getSignatureDeclarant() != null) {
            incident.setSignatureDeclarant(incident.getSignatureDeclarant().trim());
        }
        
        if (incident.getSignatureResponsableQualite() != null) {
            incident.setSignatureResponsableQualite(incident.getSignatureResponsableQualite().trim());
        }
        
        // Validation des dates
        if (incident.getPatientDateNaissance() != null && 
            incident.getPatientDateNaissance().isAfter(LocalDate.now())) {
            throw new RuntimeException("La date de naissance du patient ne peut pas être dans le futur");
        }
        
        if (incident.getDatePeremptionProduit() != null && 
            incident.getDatePeremptionProduit().isBefore(LocalDate.now())) {
            throw new RuntimeException("Le produit transfusé ne peut pas être périmé");
        }
        
        if (incident.getDateHeureDeclaration() != null && 
            incident.getDateHeureDeclaration().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("La date de déclaration ne peut pas être dans le futur");
        }
        
        return incidentRepository.save(incident);
    }

    @Override
    public List<IncidentTransfusionnel> getAllIncidents() {
        return incidentRepository.findAll();
    }

    @Override
    public Optional<IncidentTransfusionnel> getIncidentById(Long id) {
        return incidentRepository.findById(id);
    }

    @Override
    public IncidentTransfusionnel getIncidentByTransfusion(Long transfusionId) {
        return incidentRepository.findAll().stream()
                .filter(incident -> incident.getTransfusion() != null && 
                        incident.getTransfusion().getId().equals(transfusionId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Aucun incident trouvé pour la transfusion avec l'id: " + transfusionId));
    }


    @Override
    public IncidentTransfusionnel updateIncident(Long id, IncidentTransfusionnel incidentDetails) {
        IncidentTransfusionnel incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident non trouvé avec l'id: " + id));
        
        // Mettre à jour les champs
        incident.setDateIncident(incidentDetails.getDateIncident());
        incident.setHeureIncident(incidentDetails.getHeureIncident());
        incident.setLieuIncident(capitalizeFirstLetter(incidentDetails.getLieuIncident().trim()));
        incident.setPatientPrenom(capitalizeFirstLetter(incidentDetails.getPatientPrenom().trim()));
        incident.setPatientNom(incidentDetails.getPatientNom().toUpperCase().trim());
        incident.setPatientDateNaissance(incidentDetails.getPatientDateNaissance());
        incident.setPatientNumDossier(incidentDetails.getPatientNumDossier().toUpperCase().trim());
        incident.setTypeProduitTransfuse(capitalizeFirstLetter(incidentDetails.getTypeProduitTransfuse().trim()));
        incident.setNumeroLotProduit(incidentDetails.getNumeroLotProduit().toUpperCase().trim());
        incident.setDatePeremptionProduit(incidentDetails.getDatePeremptionProduit());
        
        incident.setDescriptionIncident(incidentDetails.getDescriptionIncident() != null ? 
                incidentDetails.getDescriptionIncident().trim() : null);
        incident.setSignes(incidentDetails.getSignes() != null ? 
                incidentDetails.getSignes().trim() : null);
        incident.setSymptomes(incidentDetails.getSymptomes() != null ? 
                incidentDetails.getSymptomes().trim() : null);
        incident.setActionsImmediates(incidentDetails.getActionsImmediates() != null ? 
                incidentDetails.getActionsImmediates().trim() : null);
        
        incident.setNomDeclarant(incidentDetails.getNomDeclarant().toUpperCase().trim());
        incident.setFonctionDeclarant(capitalizeFirstLetter(incidentDetails.getFonctionDeclarant().trim()));
        incident.setDateHeureDeclaration(incidentDetails.getDateHeureDeclaration());
        
        incident.setPersonnesInformees(incidentDetails.getPersonnesInformees() != null ? 
                incidentDetails.getPersonnesInformees().trim() : null);
        incident.setRegistreHemovigilance(incidentDetails.getRegistreHemovigilance() != null ? 
                incidentDetails.getRegistreHemovigilance().trim() : null);
        incident.setAnalysePreliminaire(incidentDetails.getAnalysePreliminaire() != null ? 
                incidentDetails.getAnalysePreliminaire().trim() : null);
        incident.setActionsCorrectives(incidentDetails.getActionsCorrectives() != null ? 
                incidentDetails.getActionsCorrectives().trim() : null);
        
        incident.setSignatureDeclarant(incidentDetails.getSignatureDeclarant() != null ? 
                incidentDetails.getSignatureDeclarant().trim() : null);
        incident.setSignatureResponsableQualite(incidentDetails.getSignatureResponsableQualite() != null ? 
                incidentDetails.getSignatureResponsableQualite().trim() : null);
        incident.setDateValidation(incidentDetails.getDateValidation());
        
        // Mettre à jour la relation si nécessaire
        if (incidentDetails.getTransfusion() != null && incidentDetails.getTransfusion().getId() != null) {
            Transfusion transfusion = transfusionRepository.findById(incidentDetails.getTransfusion().getId())
                    .orElseThrow(() -> new RuntimeException("Transfusion non trouvée"));
            incident.setTransfusion(transfusion);
        }
        
        return incidentRepository.save(incident);
    }

    @Override
    public void validerIncident(Long id, String signatureResponsableQualite) {
        IncidentTransfusionnel incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident non trouvé avec l'id: " + id));
        
        incident.setSignatureResponsableQualite(signatureResponsableQualite.trim());
        incident.setDateValidation(LocalDate.now());
        incidentRepository.save(incident);
    }

    @Override
    public void deleteIncident(Long id) {
        IncidentTransfusionnel incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident non trouvé avec l'id: " + id));
        
        incidentRepository.delete(incident);
    }

    @Override
    public List<IncidentTransfusionnel> getIncidentsByDate(LocalDate date) {
        return incidentRepository.findAll().stream()
                .filter(incident -> incident.getDateIncident().equals(date))
                .collect(Collectors.toList());
    }

    @Override
    public List<IncidentTransfusionnel> getIncidentsByDateRange(LocalDate startDate, LocalDate endDate) {
        return incidentRepository.findAll().stream()
                .filter(incident -> !incident.getDateIncident().isBefore(startDate) && 
                        !incident.getDateIncident().isAfter(endDate))
                .collect(Collectors.toList());
    }

    @Override
    public List<IncidentTransfusionnel> getIncidentsByPatient(String nom, String prenom) {
        return incidentRepository.findAll().stream()
                .filter(incident -> incident.getPatientNom().equalsIgnoreCase(nom) && 
                        incident.getPatientPrenom().equalsIgnoreCase(prenom))
                .collect(Collectors.toList());
    }

    @Override
    public List<IncidentTransfusionnel> getIncidentsByNumDossier(String numDossier) {
        return incidentRepository.findAll().stream()
                .filter(incident -> incident.getPatientNumDossier().equalsIgnoreCase(numDossier))
                .collect(Collectors.toList());
    }

    @Override
    public List<IncidentTransfusionnel> getIncidentsByTypeProduit(String typeProduit) {
        return incidentRepository.findAll().stream()
                .filter(incident -> incident.getTypeProduitTransfuse().equalsIgnoreCase(typeProduit))
                .collect(Collectors.toList());
    }

    @Override
    public List<IncidentTransfusionnel> getIncidentsNonValides() {
        return incidentRepository.findAll().stream()
                .filter(incident -> incident.getDateValidation() == null)
                .collect(Collectors.toList());
    }

    @Override
    public List<IncidentTransfusionnel> getIncidentsValides() {
        return incidentRepository.findAll().stream()
                .filter(incident -> incident.getDateValidation() != null)
                .collect(Collectors.toList());
    }

    @Override
    public long countIncidentsByStatutValidation(boolean valide) {
        return incidentRepository.findAll().stream()
                .filter(incident -> valide ? incident.getDateValidation() != null : incident.getDateValidation() == null)
                .count();
    }
    
    // Méthode utilitaire pour capitaliser la première lettre
    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }


}