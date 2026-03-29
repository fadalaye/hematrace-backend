package com.hematrace.hematrace.service;

import com.hematrace.hematrace.entite.IncidentTransfusionnel;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IncidentTransfusionnelService {
    IncidentTransfusionnel creerIncident(IncidentTransfusionnel incident);
    List<IncidentTransfusionnel> getAllIncidents();
    Optional<IncidentTransfusionnel> getIncidentById(Long id);
    IncidentTransfusionnel getIncidentByTransfusion(Long transfusionId);
    IncidentTransfusionnel updateIncident(Long id, IncidentTransfusionnel incidentDetails);
    void validerIncident(Long id, String signatureResponsableQualite);
    void deleteIncident(Long id);
    List<IncidentTransfusionnel> getIncidentsByDate(LocalDate date);
    List<IncidentTransfusionnel> getIncidentsByDateRange(LocalDate startDate, LocalDate endDate);
    List<IncidentTransfusionnel> getIncidentsByPatient(String nom, String prenom);
    List<IncidentTransfusionnel> getIncidentsByNumDossier(String numDossier);
    List<IncidentTransfusionnel> getIncidentsByTypeProduit(String typeProduit);
    List<IncidentTransfusionnel> getIncidentsNonValides();
    List<IncidentTransfusionnel> getIncidentsValides();
    long countIncidentsByStatutValidation(boolean valide);

    
}