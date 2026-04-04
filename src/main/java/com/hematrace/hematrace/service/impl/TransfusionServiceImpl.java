package com.hematrace.hematrace.service.impl;

import com.hematrace.hematrace.dto.CorrectionCliniqueTransfusionRequest;
import com.hematrace.hematrace.dto.CreerTransfusionDTO;
import com.hematrace.hematrace.dto.TransfusionWithSurveillancesDTO;
import com.hematrace.hematrace.entite.*;
import com.hematrace.hematrace.repository.*;
import com.hematrace.hematrace.service.IncidentTransfusionnelService;
import com.hematrace.hematrace.service.TransfusionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransfusionServiceImpl implements TransfusionService {

    @Autowired
    private TransfusionRepository transfusionRepository;
    
    @Autowired
    private MedecinRepository medecinRepository;
    
    @Autowired
    private ProduitSanguinRepository produitSanguinRepository;

    @Autowired
    private IncidentTransfusionnelService incidentService;

    // ========== MÉTHODES DE CRÉATION ==========
    
    @Override
    public Transfusion creerTransfusion(Transfusion transfusion) {
        validateAndNormalizeTransfusion(transfusion);
        Transfusion savedTransfusion = transfusionRepository.save(transfusion);
        updateBidirectionalRelationships(savedTransfusion);
        return savedTransfusion;
    }
    
    @Override
    @Transactional
    public Transfusion creerTransfusion(CreerTransfusionDTO dto) {
        // 1. Validation des entités liées
        Medecin medecin = medecinRepository.findById(dto.getMedecinId())
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé avec l'id: " + dto.getMedecinId()));
        
        ProduitSanguin produitSanguin = produitSanguinRepository.findById(dto.getProduitSanguinId())
                .orElseThrow(() -> new RuntimeException("Produit sanguin non trouvé avec l'id: " + dto.getProduitSanguinId()));
        
        // 2. Validation : On ne peut transfuser que des produits DÉLIVRÉS
        if (!"DÉLIVRÉ".equalsIgnoreCase(produitSanguin.getEtat())) {
            throw new RuntimeException("Le produit sanguin n'est pas délivré. État: " + produitSanguin.getEtat() + 
                                     ". Seuls les produits délivrés peuvent être transfusés.");
        }
        
        // 3. Création de la transfusion
        Transfusion transfusion = new Transfusion();
        transfusion.setMedecin(medecin);
        transfusion.setProduitSanguin(produitSanguin);
        
        // 4. Copie des champs du DTO avec normalisation
        copyDtoToTransfusion(dto, transfusion);
        
        // 5. Validation des données
        validateTransfusionData(transfusion);
        
        // 6. Sauvegarde
        Transfusion savedTransfusion = transfusionRepository.save(transfusion);
        
        // 7. Mettre à jour les relations bidirectionnelles
        updateBidirectionalRelationships(savedTransfusion);
        
        return savedTransfusion;
    }

    @Override
    @Transactional
    public Transfusion creerTransfusionAvecSurveillances(TransfusionWithSurveillancesDTO dto) {
        // 1. Validation des entités liées
        Medecin medecin = medecinRepository.findById(dto.getMedecinId())
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé avec l'id: " + dto.getMedecinId()));
        
        ProduitSanguin produitSanguin = produitSanguinRepository.findById(dto.getProduitSanguinId())
                .orElseThrow(() -> new RuntimeException("Produit sanguin non trouvé avec l'id: " + dto.getProduitSanguinId()));
        
        // 2. Validation : On ne peut transfuser que des produits DÉLIVRÉS
        if (!"DÉLIVRÉ".equalsIgnoreCase(produitSanguin.getEtat())) {
            throw new RuntimeException("Le produit sanguin n'est pas délivré. État: " + produitSanguin.getEtat() + 
                                     ". Seuls les produits délivrés peuvent être transfusés.");
        }
        
        // 3. Création de la transfusion
        Transfusion transfusion = new Transfusion();
        transfusion.setMedecin(medecin);
        transfusion.setProduitSanguin(produitSanguin);
        
        // 4. Copie des champs du DTO avec normalisation
        copyDtoToTransfusion(dto, transfusion);
        
        // 5. Validation des données transfusion
        validateTransfusionData(transfusion);
        
        // 6. Sauvegarde initiale de la transfusion
        Transfusion savedTransfusion = transfusionRepository.save(transfusion);

        if (dto.getIncident() != null) {
            IncidentTransfusionnel incident = createIncidentFromDTO(dto.getIncident(), savedTransfusion);
            savedTransfusion.setIncidentTransfusionnel(incident);
        }

        // Création et association des surveillances si présentes
        if (dto.getSurveillances() != null && !dto.getSurveillances().isEmpty()) {
            for (TransfusionWithSurveillancesDTO.SurveillanceDTO surveillanceDTO : dto.getSurveillances()) {
                Surveillance surveillance = createSurveillanceFromDTO(surveillanceDTO, savedTransfusion);
                savedTransfusion.addSurveillance(surveillance);
            }
        }
        
        // 7. Mettre à jour les relations bidirectionnelles
        updateBidirectionalRelationships(savedTransfusion);
        
        // Sauvegarde finale
        return transfusionRepository.save(savedTransfusion);
    }

    private IncidentTransfusionnel createIncidentFromDTO(
        TransfusionWithSurveillancesDTO.IncidentTransfusionnelDTO dto, 
        Transfusion transfusion) {
    
        IncidentTransfusionnel incident = new IncidentTransfusionnel();
        
        incident.setTransfusion(transfusion);
        incident.setDateIncident(dto.getDateIncident());
        incident.setHeureIncident(dto.getHeureIncident());
        incident.setLieuIncident(dto.getLieuIncident());
        
        // Copier les informations du patient depuis la transfusion
        incident.setPatientPrenom(transfusion.getPatientPrenom());
        incident.setPatientNom(transfusion.getPatientNom());
        incident.setPatientDateNaissance(transfusion.getPatientDateNaissance());
        incident.setPatientNumDossier(transfusion.getPatientNumDossier());
        
        incident.setTypeProduitTransfuse(dto.getTypeProduitTransfuse());
        incident.setNumeroLotProduit(dto.getNumeroLotProduit());
        incident.setDatePeremptionProduit(dto.getDatePeremptionProduit());
        incident.setDescriptionIncident(dto.getDescriptionIncident());
        incident.setSignes(dto.getSignes());
        incident.setSymptomes(dto.getSymptomes());
        incident.setActionsImmediates(dto.getActionsImmediates());
        incident.setPersonnesInformees(dto.getPersonnesInformees());
        incident.setAnalysePreliminaire(dto.getAnalysePreliminaire());
        incident.setActionsCorrectives(dto.getActionsCorrectives());
        incident.setDateHeureDeclaration(LocalDateTime.now());
        incident.setNomDeclarant(dto.getNomDeclarant());
        incident.setFonctionDeclarant(dto.getFonctionDeclarant());
        incident.setRegistreHemovigilance(dto.getRegistreHemovigilance());
        
        return incident;
    }

    private Surveillance createSurveillanceFromDTO(
            TransfusionWithSurveillancesDTO.SurveillanceDTO dto, 
            Transfusion transfusion) {
        
        Surveillance surveillance = new Surveillance();
        surveillance.setTransfusion(transfusion);
        surveillance.setHeure(dto.getHeure());
        surveillance.setTension(dto.getTension().trim());
        surveillance.setTemperature(dto.getTemperature());
        surveillance.setPouls(dto.getPouls());
        surveillance.setSignesCliniques(dto.getSignesCliniques().trim());
        surveillance.setObservations(dto.getObservations() != null ? 
            dto.getObservations().trim() : "");
        
        validateSurveillanceData(surveillance);
        
        return surveillance;
    }

    /**
     * Met à jour les relations bidirectionnelles après la création d'une transfusion
     */
    private void updateBidirectionalRelationships(Transfusion transfusion) {
        // Mettre à jour l'état du produit sanguin
        if (transfusion.getProduitSanguin() != null) {
            ProduitSanguin produit = transfusion.getProduitSanguin();
            produit.setTransfusion(transfusion);
            produit.setEtat("UTILISÉ"); // Passe de "DÉLIVRÉ" à "UTILISÉ"
            produitSanguinRepository.save(produit);
        }
    }

    // ========== MÉTHODES DE RÉCUPÉRATION ==========
    
    @Override
    @Transactional(readOnly = true)
    public List<Transfusion> getAllTransfusions() {
        return transfusionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Transfusion> getTransfusionById(Long id) {
        return transfusionRepository.findById(id);
    }

    // ========== MÉTHODES DE RECHERCHE ==========
    
    @Override
    @Transactional(readOnly = true)
    public List<Transfusion> getTransfusionsByMedecin(Long medecinId) {
        return transfusionRepository.findByMedecinId(medecinId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transfusion> getTransfusionsByProduitSanguin(Long produitSanguinId) {
        return transfusionRepository.findByProduitSanguinId(produitSanguinId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transfusion> getTransfusionsByDate(LocalDate date) {
        return transfusionRepository.findByDateTransfusion(date);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transfusion> getTransfusionsByDateRange(LocalDate startDate, LocalDate endDate) {
        return transfusionRepository.findByDateTransfusionBetween(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transfusion> getTransfusionsByGroupeSanguin(String groupeSanguin) {
        return transfusionRepository.findByGroupeSanguinPatientIgnoreCase(groupeSanguin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transfusion> getTransfusionsByTolerance(String tolerance) {
        return transfusionRepository.findByToleranceIgnoreCase(tolerance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transfusion> getTransfusionsAvecEffetsIndesirables() {
        return transfusionRepository.findByEffetsIndesirablesTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transfusion> getTransfusionsByPatient(String nom, String prenom) {
        return transfusionRepository.findByPatientNomIgnoreCaseAndPatientPrenomIgnoreCase(nom, prenom);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transfusion> getTransfusionsByNumDossier(String numDossier) {
        return transfusionRepository.findByPatientNumDossierIgnoreCase(numDossier);
    }

    // ========== MÉTHODES STATISTIQUES ==========
    
    @Override
    @Transactional(readOnly = true)
    public long countTransfusionsByTolerance(String tolerance) {
        return transfusionRepository.countByToleranceIgnoreCase(tolerance);
    }

    @Override
    @Transactional(readOnly = true)
    public long countTransfusionsAvecEffetsIndesirables() {
        return transfusionRepository.countByEffetsIndesirablesTrue();
    }

    // ========== MÉTHODES DE MISE À JOUR ==========
    
    @Override
    @Transactional
    public Transfusion updateTransfusion(Long id, Transfusion transfusionDetails) {
        Transfusion transfusion = transfusionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transfusion non trouvée avec l'id: " + id));
        
        // Sauvegarder l'ancien produit sanguin pour mise à jour
        ProduitSanguin ancienProduit = transfusion.getProduitSanguin();
        
        // Mettre à jour les champs
        transfusion.setPatientPrenom(capitalizeFirstLetter(transfusionDetails.getPatientPrenom().trim()));
        transfusion.setPatientNom(transfusionDetails.getPatientNom().toUpperCase().trim());
        transfusion.setPatientDateNaissance(transfusionDetails.getPatientDateNaissance());
        transfusion.setPatientNumDossier(transfusionDetails.getPatientNumDossier().toUpperCase().trim());
        transfusion.setGroupeSanguinPatient(transfusionDetails.getGroupeSanguinPatient().toUpperCase().trim());
        transfusion.setHeureFin(transfusionDetails.getHeureFin());
        transfusion.setEtatPatientApres(capitalizeFirstLetter(transfusionDetails.getEtatPatientApres().trim()));
        transfusion.setTolerance(capitalizeFirstLetter(transfusionDetails.getTolerance().trim()));
        transfusion.setEffetsIndesirables(transfusionDetails.getEffetsIndesirables());
        
        if (transfusionDetails.getTypeEffet() != null && !transfusionDetails.getTypeEffet().trim().isEmpty()) {
            transfusion.setTypeEffet(capitalizeFirstLetter(transfusionDetails.getTypeEffet().trim()));
        } else {
            transfusion.setTypeEffet(null);
        }
        
        // Mettre à jour les nouveaux champs
        transfusion.setDateTransfusion(transfusionDetails.getDateTransfusion());
        transfusion.setHeureDebut(transfusionDetails.getHeureDebut());
        transfusion.setNotes(transfusionDetails.getNotes());
        transfusion.setVolumeMl(transfusionDetails.getVolumeMl());
        transfusion.setGraviteEffet(transfusionDetails.getGraviteEffet());
        
        transfusion.setPrenomDeclarant(capitalizeFirstLetter(transfusionDetails.getPrenomDeclarant().trim()));
        transfusion.setNomDeclarant(transfusionDetails.getNomDeclarant().toUpperCase().trim());
        transfusion.setFonctionDeclarant(capitalizeFirstLetter(transfusionDetails.getFonctionDeclarant().trim()));
        
        // Mettre à jour les relations si nécessaires
        if (transfusionDetails.getMedecin() != null && transfusionDetails.getMedecin().getId() != null) {
            Medecin medecin = medecinRepository.findById(transfusionDetails.getMedecin().getId())
                    .orElseThrow(() -> new RuntimeException("Médecin non trouvé"));
            transfusion.setMedecin(medecin);
        }
        
        // Mettre à jour le produit sanguin si changé
        if (transfusionDetails.getProduitSanguin() != null && transfusionDetails.getProduitSanguin().getId() != null) {
            ProduitSanguin nouveauProduit = produitSanguinRepository.findById(transfusionDetails.getProduitSanguin().getId())
                    .orElseThrow(() -> new RuntimeException("Produit sanguin non trouvé"));
            
            if (!nouveauProduit.getId().equals(ancienProduit.getId())) {
                // Validation : Le nouveau produit doit être DÉLIVRÉ
                if (!"DÉLIVRÉ".equalsIgnoreCase(nouveauProduit.getEtat())) {
                    throw new RuntimeException("Le nouveau produit n'est pas délivré. État: " + 
                                             nouveauProduit.getEtat() + 
                                             ". Seuls les produits délivrés peuvent être transfusés.");
                }
                
                // Libérer l'ancien produit (le remettre à DÉLIVRÉ car il n'est plus utilisé)
                ancienProduit.setTransfusion(null);
                ancienProduit.setEtat("DÉLIVRÉ"); // Retour à DÉLIVRÉ (pas DISPONIBLE)
                produitSanguinRepository.save(ancienProduit);
                
                // Associer le nouveau produit
                transfusion.setProduitSanguin(nouveauProduit);
                nouveauProduit.setTransfusion(transfusion);
                nouveauProduit.setEtat("UTILISÉ"); // Passe à UTILISÉ
                produitSanguinRepository.save(nouveauProduit);
            }
        }
        
        // Validation
        validateTransfusionData(transfusion);
        
        Transfusion savedTransfusion = transfusionRepository.save(transfusion);
        updateBidirectionalRelationships(savedTransfusion);
        
        return savedTransfusion;
    }

    @Override
    @Transactional
    public void deleteTransfusion(Long id) {
        Transfusion transfusion = transfusionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transfusion non trouvée avec l'id: " + id));
        
        // Libérer le produit sanguin
        if (transfusion.getProduitSanguin() != null) {
            ProduitSanguin produit = transfusion.getProduitSanguin();
            produit.setTransfusion(null);
            produit.setEtat("DÉLIVRÉ"); // Retour à DÉLIVRÉ (pas DISPONIBLE)
            produitSanguinRepository.save(produit);
        }
        
        transfusionRepository.delete(transfusion);
    }

    // ========== MÉTHODES UTILITAIRES PRIVÉES ==========
    
    /**
     * Copie les données d'un DTO vers une entité Transfusion avec normalisation
     */
    private void copyDtoToTransfusion(Object dto, Transfusion transfusion) {
        if (dto instanceof CreerTransfusionDTO) {
            CreerTransfusionDTO creerDto = (CreerTransfusionDTO) dto;
            copyBaseDtoFields(creerDto, transfusion);
            transfusion.setDateDeclaration(creerDto.getDateDeclaration() != null ? 
                creerDto.getDateDeclaration() : LocalDate.now());
        } 
        else if (dto instanceof TransfusionWithSurveillancesDTO) {
            TransfusionWithSurveillancesDTO avecSurvDto = (TransfusionWithSurveillancesDTO) dto;
            copyBaseDtoFields(avecSurvDto, transfusion);
            transfusion.setDateDeclaration(LocalDate.now());
        }
    }
    
    /**
     * Copie les champs communs des DTOs
     */
    private void copyBaseDtoFields(Object dto, Transfusion transfusion) {
        if (dto instanceof CreerTransfusionDTO) {
            CreerTransfusionDTO creerDto = (CreerTransfusionDTO) dto;
            copyFields(transfusion, 
                creerDto.getPatientPrenom(),
                creerDto.getPatientNom(),
                creerDto.getPatientDateNaissance(),
                creerDto.getPatientNumDossier(),
                creerDto.getGroupeSanguinPatient(),
                creerDto.getHeureFin(),
                creerDto.getEtatPatientApres(),
                creerDto.getTolerance(),
                creerDto.getEffetsIndesirables(),
                creerDto.getTypeEffet(),
                creerDto.getPrenomDeclarant(),
                creerDto.getNomDeclarant(),
                creerDto.getFonctionDeclarant(),
                creerDto.getDateTransfusion(),
                creerDto.getHeureDebut(),
                creerDto.getNotes(),
                creerDto.getVolumeMl(),
                creerDto.getGraviteEffet());
        }
        else if (dto instanceof TransfusionWithSurveillancesDTO) {
            TransfusionWithSurveillancesDTO avecSurvDto = (TransfusionWithSurveillancesDTO) dto;
            copyFields(transfusion, 
                avecSurvDto.getPatientPrenom(),
                avecSurvDto.getPatientNom(),
                avecSurvDto.getPatientDateNaissance(),
                avecSurvDto.getPatientNumDossier(),
                avecSurvDto.getGroupeSanguinPatient(),
                avecSurvDto.getHeureFin(),
                avecSurvDto.getEtatPatientApres(),
                avecSurvDto.getTolerance(),
                avecSurvDto.getEffetsIndesirables(),
                avecSurvDto.getTypeEffet(),
                avecSurvDto.getPrenomDeclarant(),
                avecSurvDto.getNomDeclarant(),
                avecSurvDto.getFonctionDeclarant(),
                avecSurvDto.getDateTransfusion(),
                avecSurvDto.getHeureDebut(),
                avecSurvDto.getNotes(),
                avecSurvDto.getVolumeMl(),
                avecSurvDto.getGraviteEffet());
        }
    }
    
    /**
     * Méthode utilitaire pour copier tous les champs
     */
    private void copyFields(Transfusion transfusion,
                           String patientPrenom,
                           String patientNom,
                           LocalDate patientDateNaissance,
                           String patientNumDossier,
                           String groupeSanguinPatient,
                           LocalTime heureFin,
                           String etatPatientApres,
                           String tolerance,
                           Boolean effetsIndesirables,
                           String typeEffet,
                           String prenomDeclarant,
                           String nomDeclarant,
                           String fonctionDeclarant,
                           LocalDate dateTransfusion,
                           LocalTime heureDebut,
                           String notes,
                           Integer volumeMl,
                           String graviteEffet) {
        
        transfusion.setPatientPrenom(capitalizeFirstLetter(patientPrenom.trim()));
        transfusion.setPatientNom(patientNom.toUpperCase().trim());
        transfusion.setPatientDateNaissance(patientDateNaissance);
        transfusion.setPatientNumDossier(patientNumDossier.toUpperCase().trim());
        transfusion.setGroupeSanguinPatient(groupeSanguinPatient.toUpperCase().trim());
        transfusion.setHeureFin(heureFin);
        transfusion.setEtatPatientApres(capitalizeFirstLetter(etatPatientApres.trim()));
        transfusion.setTolerance(capitalizeFirstLetter(tolerance.trim()));
        transfusion.setEffetsIndesirables(effetsIndesirables != null ? effetsIndesirables : false);
        
        if (typeEffet != null && !typeEffet.trim().isEmpty()) {
            transfusion.setTypeEffet(capitalizeFirstLetter(typeEffet.trim()));
        }
        
        transfusion.setPrenomDeclarant(capitalizeFirstLetter(prenomDeclarant.trim()));
        transfusion.setNomDeclarant(nomDeclarant.toUpperCase().trim());
        transfusion.setFonctionDeclarant(capitalizeFirstLetter(fonctionDeclarant.trim()));
        transfusion.setDateTransfusion(dateTransfusion);
        transfusion.setHeureDebut(heureDebut);
        
        if (notes != null && !notes.trim().isEmpty()) {
            transfusion.setNotes(notes.trim());
        }
        
        transfusion.setVolumeMl(volumeMl);
        
        if (graviteEffet != null && !graviteEffet.trim().isEmpty()) {
            transfusion.setGraviteEffet(capitalizeFirstLetter(graviteEffet.trim()));
        }
    }
    
    private void validateAndNormalizeTransfusion(Transfusion transfusion) {
        // Validation des relations obligatoires
        if (transfusion.getMedecin() == null || transfusion.getMedecin().getId() == null) {
            throw new RuntimeException("Un médecin doit être associé à la transfusion");
        }
        
        if (transfusion.getProduitSanguin() == null || transfusion.getProduitSanguin().getId() == null) {
            throw new RuntimeException("Un produit sanguin doit être associé à la transfusion");
        }
        
        // Vérifier que le médecin existe
        Medecin medecin = medecinRepository.findById(transfusion.getMedecin().getId())
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé avec l'id: " + transfusion.getMedecin().getId()));
        
        // Vérifier que le produit sanguin existe
        ProduitSanguin produitSanguin = produitSanguinRepository.findById(transfusion.getProduitSanguin().getId())
                .orElseThrow(() -> new RuntimeException("Produit sanguin non trouvé avec l'id: " + transfusion.getProduitSanguin().getId()));
        
        // Validation : On ne peut transfuser que des produits DÉLIVRÉS
        if (!"DÉLIVRÉ".equalsIgnoreCase(produitSanguin.getEtat())) {
            throw new RuntimeException("Le produit sanguin n'est pas délivré. État: " + produitSanguin.getEtat() + 
                                     ". Seuls les produits délivrés peuvent être transfusés.");
        }
        
        // Normaliser les données
        transfusion.setPatientPrenom(capitalizeFirstLetter(transfusion.getPatientPrenom().trim()));
        transfusion.setPatientNom(transfusion.getPatientNom().toUpperCase().trim());
        transfusion.setPatientNumDossier(transfusion.getPatientNumDossier().toUpperCase().trim());
        transfusion.setGroupeSanguinPatient(transfusion.getGroupeSanguinPatient().toUpperCase().trim());
        transfusion.setEtatPatientApres(capitalizeFirstLetter(transfusion.getEtatPatientApres().trim()));
        transfusion.setTolerance(capitalizeFirstLetter(transfusion.getTolerance().trim()));
        
        if (transfusion.getTypeEffet() != null && !transfusion.getTypeEffet().trim().isEmpty()) {
            transfusion.setTypeEffet(capitalizeFirstLetter(transfusion.getTypeEffet().trim()));
        }
        
        transfusion.setPrenomDeclarant(capitalizeFirstLetter(transfusion.getPrenomDeclarant().trim()));
        transfusion.setNomDeclarant(transfusion.getNomDeclarant().toUpperCase().trim());
        transfusion.setFonctionDeclarant(capitalizeFirstLetter(transfusion.getFonctionDeclarant().trim()));
        
        // Définir la date de déclaration si non présente
        if (transfusion.getDateDeclaration() == null) {
            transfusion.setDateDeclaration(LocalDate.now());
        }
        
        // Validation des données
        validateTransfusionData(transfusion);
    }
    
    private void validateTransfusionData(Transfusion transfusion) {
        if (transfusion.getPatientDateNaissance() != null && 
            transfusion.getPatientDateNaissance().isAfter(LocalDate.now())) {
            throw new RuntimeException("La date de naissance du patient ne peut pas être dans le futur");
        }
        
        if (transfusion.getDateTransfusion() != null && 
            transfusion.getDateTransfusion().isAfter(LocalDate.now())) {
            throw new RuntimeException("La date de transfusion ne peut pas être dans le futur");
        }
        
        // Validation améliorée pour heureFin (prend en compte la date)
        if (transfusion.getHeureFin() != null && transfusion.getDateTransfusion() != null) {
            // Si la date de transfusion est aujourd'hui, vérifier l'heure
            if (transfusion.getDateTransfusion().isEqual(LocalDate.now())) {
                if (transfusion.getHeureFin().isAfter(LocalTime.now())) {
                    throw new RuntimeException("L'heure de fin ne peut pas être dans le futur");
                }
            }
        }
        
        if (transfusion.getHeureDebut() != null && transfusion.getHeureFin() != null &&
            transfusion.getHeureFin().isBefore(transfusion.getHeureDebut())) {
            throw new RuntimeException("L'heure de fin ne peut pas être avant l'heure de début");
        }
        
        // Vérifier la cohérence des effets indésirables
        if (transfusion.getEffetsIndesirables() != null && transfusion.getEffetsIndesirables()) {
            if (transfusion.getTypeEffet() == null || transfusion.getTypeEffet().isEmpty()) {
                throw new RuntimeException("Le type d'effet indésirable doit être spécifié");
            }
        }
    }
    
    private void validateSurveillanceData(Surveillance surveillance) {
        if (surveillance.getHeure().isAfter(LocalTime.now())) {
            throw new RuntimeException("L'heure de surveillance ne peut pas être dans le futur");
        }
        
        if (surveillance.getTemperature() < 32.0 || surveillance.getTemperature() > 42.0) {
            throw new RuntimeException("La température doit être entre 32°C et 42°C");
        }
        
        if (surveillance.getPouls() < 30 || surveillance.getPouls() > 200) {
            throw new RuntimeException("Le pouls doit être entre 30 et 200 bpm");
        }
    }
    
    // Méthode utilitaire pour capitaliser la première lettre
    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    public List<Transfusion> getTransfusionsSansIncident() {
        List<Transfusion> transfusions = transfusionRepository.findAll();
        
        return transfusions.stream()
            .filter(transfusion -> {
                try {
                    incidentService.getIncidentByTransfusion(transfusion.getId());
                    return false; // A un incident
                } catch (RuntimeException e) {
                    return true; // N'a pas d'incident
                }
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Vérifie si une transfusion a déjà un incident
     */
    public boolean hasIncident(Long transfusionId) {
        try {
            incidentService.getIncidentByTransfusion(transfusionId);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
    
    /**
     * Récupère les transfusions compatibles pour déclarer un incident
     */
    public List<Transfusion> getTransfusionsCompatiblesIncident() {
        List<Transfusion> transfusions = transfusionRepository.findAll();
        
        return transfusions.stream()
            .filter(transfusion -> {
                try {
                    incidentService.getIncidentByTransfusion(transfusion.getId());
                    return false; // A un incident, donc non compatible
                } catch (RuntimeException e) {
                    return true; // N'a pas d'incident, donc compatible
                }
            })
            .collect(Collectors.toList());
    }

    public Transfusion corrigerCliniquement(Long id, CorrectionCliniqueTransfusionRequest request) {
    Transfusion transfusion = transfusionRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Transfusion introuvable"));

    transfusion.setTolerance(request.getTolerance());
    transfusion.setEtatPatientApres(request.getEtatPatientApres());
    transfusion.setEffetsIndesirables(request.getEffetsIndesirables());
    transfusion.setTypeEffet(request.getTypeEffet());
    transfusion.setGraviteEffet(request.getGraviteEffet());
    transfusion.setNotes(request.getNotes());

    // remplacer / synchroniser les surveillances
    // puis save

    return transfusionRepository.save(transfusion);
}
}