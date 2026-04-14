package com.hematrace.hematrace.service;

import com.hematrace.hematrace.dto.TraceElementDTO;
import com.hematrace.hematrace.entite.*;
import com.hematrace.hematrace.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TracabiliteService {
    
    private final ProduitSanguinRepository produitSanguinRepository;
    private final DemandeRepository demandeRepository;
    private final DelivranceRepository delivranceRepository;
    private final TransfusionRepository transfusionRepository;
    private final IncidentTransfusionnelRepository incidentRepository;
    private final SurveillanceRepository surveillanceRepository;
    private final TracabiliteRepository tracabiliteRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // ========== RECHERCHE GLOBALE ==========
    
    @Transactional(readOnly = true)
    public List<TraceElementDTO> searchTraces(
            String type,
            Long id,
            String reference,
            LocalDate dateDebut,
            LocalDate dateFin,
            String utilisateur,
            String statut,
            String query) {
        
        log.info("🔍 Recherche traçabilité - Type: {}, ID: {}, Ref: {}, Date: {} à {}, User: {}, Statut: {}, Query: {}", 
                type, id, reference, dateDebut, dateFin, utilisateur, statut, query);
        
        List<TraceElementDTO> results = new ArrayList<>();
        
        try {
            // Si un type spécifique est demandé
            if (type != null && !type.isEmpty()) {
                results.addAll(searchByType(type, id, reference, dateDebut, dateFin, utilisateur, statut, query));
            } else {
                // Recherche dans tous les types
                results.addAll(searchAllTypes(id, reference, dateDebut, dateFin, utilisateur, statut, query));
            }
            
            // Trier par date (plus récent en premier)
            results.sort((a, b) -> b.getDate().compareTo(a.getDate()));
            
            log.info("✅ {} résultats trouvés", results.size());
            
        } catch (Exception e) {
            log.error("❌ Erreur lors de la recherche globale: {}", e.getMessage(), e);
        }
        
        return results;
    }
    
    private List<TraceElementDTO> searchByType(
            String type,
            Long id,
            String reference,
            LocalDate dateDebut,
            LocalDate dateFin,
            String utilisateur,
            String statut,
            String query) {
        
        try {
            switch (type.toLowerCase()) {
                case "produit":
                case "produits":
                case "produit-sanguin":
                    return searchProduitsSanguins(id, reference, dateDebut, dateFin, utilisateur, statut, query);
                    
                case "demande":
                case "demandes":
                    return searchDemandes(id, reference, dateDebut, dateFin, utilisateur, statut, query);
                    
                case "delivrance":
                case "delivrances":
                    return searchDelivrances(id, reference, dateDebut, dateFin, utilisateur, statut, query);
                    
                case "transfusion":
                case "transfusions":
                    return searchTransfusions(id, reference, dateDebut, dateFin, utilisateur, statut, query);
                    
                case "incident":
                case "incidents":
                    return searchIncidents(id, reference, dateDebut, dateFin, utilisateur, statut, query);
                    
                case "surveillance":
                case "surveillances":
                    return searchSurveillances(id, reference, dateDebut, dateFin, utilisateur, statut, query);
                    
                case "log":
                case "logs":
                    return searchTraceLogs(id, reference, dateDebut, dateFin, utilisateur, statut, query);
                    
                default:
                    log.warn("⚠️ Type de recherche non reconnu: {}", type);
                    return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("❌ Erreur dans searchByType pour type {}: {}", type, e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    private List<TraceElementDTO> searchAllTypes(
            Long id,
            String reference,
            LocalDate dateDebut,
            LocalDate dateFin,
            String utilisateur,
            String statut,
            String query) {
        
        List<TraceElementDTO> allResults = new ArrayList<>();
        
        try {
            allResults.addAll(searchProduitsSanguins(id, reference, dateDebut, dateFin, utilisateur, statut, query));
            allResults.addAll(searchDemandes(id, reference, dateDebut, dateFin, utilisateur, statut, query));
            allResults.addAll(searchDelivrances(id, reference, dateDebut, dateFin, utilisateur, statut, query));
            allResults.addAll(searchTransfusions(id, reference, dateDebut, dateFin, utilisateur, statut, query));
            allResults.addAll(searchIncidents(id, reference, dateDebut, dateFin, utilisateur, statut, query));
            allResults.addAll(searchSurveillances(id, reference, dateDebut, dateFin, utilisateur, statut, query));
        } catch (Exception e) {
            log.error("❌ Erreur dans searchAllTypes: {}", e.getMessage(), e);
        }
        
        return allResults;
    }
    
    // ========== RECHERCHES SPÉCIFIQUES ==========
    
    private List<TraceElementDTO> searchProduitsSanguins(
            Long id,
            String reference,
            LocalDate dateDebut,
            LocalDate dateFin,
            String utilisateur,
            String statut,
            String query) {
        
        try {
            Specification<ProduitSanguin> spec = (root, criteriaQuery, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();
                
                if (id != null) {
                    predicates.add(criteriaBuilder.equal(root.get("id"), id));
                }
                
                if (reference != null && !reference.isEmpty()) {
                    predicates.add(criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("codeProduit")),
                        "%" + reference.toUpperCase() + "%"
                    ));
                }
                
                if (dateDebut != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("datePrelevement"), dateDebut
                    ));
                }
                
                if (dateFin != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("datePrelevement"), dateFin
                    ));
                }
                
                if (statut != null && !statut.isEmpty()) {
                    predicates.add(criteriaBuilder.equal(root.get("etat"), statut));
                }
                
                if (query != null && !query.isEmpty()) {
                    Predicate codePredicate = criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("codeProduit")),
                        "%" + query.toUpperCase() + "%"
                    );
                    
                    Predicate typePredicate = criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("typeProduit")),
                        "%" + query.toUpperCase() + "%"
                    );
                    
                    Predicate groupePredicate = criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("groupeSanguin")),
                        "%" + query.toUpperCase() + "%"
                    );
                    
                    predicates.add(criteriaBuilder.or(codePredicate, typePredicate, groupePredicate));
                }
                
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            };
            
            return produitSanguinRepository.findAll(spec).stream()
                .map(this::mapProduitToDTO)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("❌ Erreur dans searchProduitsSanguins: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    private List<TraceElementDTO> searchDemandes(
            Long id,
            String reference,
            LocalDate dateDebut,
            LocalDate dateFin,
            String utilisateur,
            String statut,
            String query) {
        
        try {
            Specification<Demande> spec = (root, queryCriteria, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();
                
                if (id != null) {
                    predicates.add(criteriaBuilder.equal(root.get("id"), id));
                }
                
                if (reference != null && !reference.isEmpty()) {
                    Predicate dossierPredicate = criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("patientNumDossier")),
                        "%" + reference.toUpperCase() + "%"
                    );
                    
                    Predicate idPredicate = criteriaBuilder.like(
                        criteriaBuilder.concat("D-", root.get("id").as(String.class)),
                        "%" + reference.toUpperCase() + "%"
                    );
                    
                    predicates.add(criteriaBuilder.or(dossierPredicate, idPredicate));
                }
                
                if (dateDebut != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("dateHeureDemande"), dateDebut.atStartOfDay()
                    ));
                }
                
                if (dateFin != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("dateHeureDemande"), dateFin.atTime(23, 59, 59)
                    ));
                }
                
                if (statut != null && !statut.isEmpty()) {
                    predicates.add(criteriaBuilder.equal(root.get("statut"), statut));
                }
                
                if (utilisateur != null && !utilisateur.isEmpty()) {
                    Join<Demande, Personnel> personnelJoin = root.join("personnel", JoinType.LEFT);
                    predicates.add(criteriaBuilder.like(
                        criteriaBuilder.upper(personnelJoin.get("nom")),
                        "%" + utilisateur.toUpperCase() + "%"
                    ));
                }
                
                if (query != null && !query.isEmpty()) {
                    Predicate numDossierPredicate = criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("patientNumDossier")),
                        "%" + query.toUpperCase() + "%"
                    );
                    
                    Predicate nomPredicate = criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("patientNom")),
                        "%" + query.toUpperCase() + "%"
                    );
                    
                    Predicate prenomPredicate = criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("patientPrenom")),
                        "%" + query.toUpperCase() + "%"
                    );
                    
                    Predicate groupeSanguinPredicate = criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("groupeSanguinPatient")),
                        "%" + query.toUpperCase() + "%"
                    );
                    
                    Predicate typeProduitPredicate = criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("typeProduitDemande")),
                        "%" + query.toUpperCase() + "%"
                    );
                    
                    predicates.add(criteriaBuilder.or(
                        numDossierPredicate, 
                        nomPredicate, 
                        prenomPredicate,
                        groupeSanguinPredicate,
                        typeProduitPredicate
                    ));
                }
                
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            };
            
            return demandeRepository.findAll(spec).stream()
                .map(this::mapDemandeToDTO)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("❌ Erreur dans searchDemandes: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    private List<TraceElementDTO> searchDelivrances(
            Long id,
            String reference,
            LocalDate dateDebut,
            LocalDate dateFin,
            String utilisateur,
            String statut,
            String query) {
        
        try {
            Specification<Delivrance> spec = (root, queryCriteria, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();
                
                if (id != null) {
                    predicates.add(criteriaBuilder.equal(root.get("id"), id));
                }
                
                if (reference != null && !reference.isEmpty()) {
                    predicates.add(criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("destination")),
                        "%" + reference.toUpperCase() + "%"
                    ));
                }
                
                if (dateDebut != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("dateHeureDelivrance"), dateDebut.atStartOfDay()
                    ));
                }
                
                if (dateFin != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("dateHeureDelivrance"), dateFin.atTime(23, 59, 59)
                    ));
                }
                
                if (utilisateur != null && !utilisateur.isEmpty()) {
                    Join<Delivrance, Personnel> personnelJoin = root.join("personnel", JoinType.LEFT);
                    predicates.add(criteriaBuilder.like(
                        criteriaBuilder.upper(personnelJoin.get("nom")),
                        "%" + utilisateur.toUpperCase() + "%"
                    ));
                }
                
                if (query != null && !query.isEmpty()) {
                    Predicate destPredicate = criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("destination")),
                        "%" + query.toUpperCase() + "%"
                    );
                    
                    Predicate transportPredicate = criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("modeTransport")),
                        "%" + query.toUpperCase() + "%"
                    );
                    
                    predicates.add(criteriaBuilder.or(destPredicate, transportPredicate));
                }
                
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            };
            
            return delivranceRepository.findAll(spec).stream()
                .map(this::mapDelivranceToDTO)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("❌ Erreur dans searchDelivrances: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    private List<TraceElementDTO> searchTransfusions(
            Long id,
            String reference,
            LocalDate dateDebut,
            LocalDate dateFin,
            String utilisateur,
            String statut,
            String query) {
        
        try {
            Specification<Transfusion> spec = (root, queryCriteria, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();
                
                if (id != null) {
                    predicates.add(criteriaBuilder.equal(root.get("id"), id));
                }
                
                if (reference != null && !reference.isEmpty()) {
                    predicates.add(criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("patientNumDossier")),
                        "%" + reference.toUpperCase() + "%"
                    ));
                }
                
                if (dateDebut != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("dateTransfusion"), dateDebut
                    ));
                }
                
                if (dateFin != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("dateTransfusion"), dateFin
                    ));
                }
                
                if (statut != null && !statut.isEmpty()) {
                    predicates.add(criteriaBuilder.equal(root.get("tolerance"), statut));
                }
                
                if (utilisateur != null && !utilisateur.isEmpty()) {
                    Join<Transfusion, Medecin> medecinJoin = root.join("medecin", JoinType.LEFT);
                    predicates.add(criteriaBuilder.like(
                        criteriaBuilder.upper(medecinJoin.get("nom")),
                        "%" + utilisateur.toUpperCase() + "%"
                    ));
                }
                
                if (query != null && !query.isEmpty()) {
                    Predicate patientPredicate = criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("patientNom")),
                        "%" + query.toUpperCase() + "%"
                    );
                    
                    Predicate dossierPredicate = criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("patientNumDossier")),
                        "%" + query.toUpperCase() + "%"
                    );
                    
                    predicates.add(criteriaBuilder.or(patientPredicate, dossierPredicate));
                }
                
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            };
            
            return transfusionRepository.findAll(spec).stream()
                .map(this::mapTransfusionToDTO)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("❌ Erreur dans searchTransfusions: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    private List<TraceElementDTO> searchIncidents(
            Long id,
            String reference,
            LocalDate dateDebut,
            LocalDate dateFin,
            String utilisateur,
            String statut,
            String query) {
        
        try {
            Specification<IncidentTransfusionnel> spec = (root, queryCriteria, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();
                
                if (id != null) {
                    predicates.add(criteriaBuilder.equal(root.get("id"), id));
                }
                
                if (reference != null && !reference.isEmpty()) {
                    predicates.add(criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("patientNumDossier")),
                        "%" + reference.toUpperCase() + "%"
                    ));
                }
                
                if (dateDebut != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("dateIncident"), dateDebut
                    ));
                }
                
                if (dateFin != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("dateIncident"), dateFin
                    ));
                }
                
                if (statut != null && !statut.isEmpty()) {
                    if ("VALIDE".equals(statut)) {
                        predicates.add(criteriaBuilder.isNotNull(root.get("dateValidation")));
                    } else if ("NON_VALIDE".equals(statut)) {
                        predicates.add(criteriaBuilder.isNull(root.get("dateValidation")));
                    }
                }
                
                if (utilisateur != null && !utilisateur.isEmpty()) {
                    predicates.add(criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("nomDeclarant")),
                        "%" + utilisateur.toUpperCase() + "%"
                    ));
                }
                
                if (query != null && !query.isEmpty()) {
                    Predicate patientPredicate = criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("patientNom")),
                        "%" + query.toUpperCase() + "%"
                    );
                    
                    Predicate produitPredicate = criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("typeProduitTransfuse")),
                        "%" + query.toUpperCase() + "%"
                    );
                    
                    Predicate declarantPredicate = criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("nomDeclarant")),
                        "%" + query.toUpperCase() + "%"
                    );
                    
                    predicates.add(criteriaBuilder.or(patientPredicate, produitPredicate, declarantPredicate));
                }
                
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            };
            
            return incidentRepository.findAll(spec).stream()
                .map(this::mapIncidentToDTO)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("❌ Erreur dans searchIncidents: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    private List<TraceElementDTO> searchSurveillances(
            Long id,
            String reference,
            LocalDate dateDebut,
            LocalDate dateFin,
            String utilisateur,
            String statut,
            String query) {
        
        try {
            Specification<Surveillance> spec = (root, queryCriteria, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();
                
                if (id != null) {
                    predicates.add(criteriaBuilder.equal(root.get("id"), id));
                }
                
                if (dateDebut != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("heure"), dateDebut.atStartOfDay().toLocalTime()
                    ));
                }
                
                if (dateFin != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("heure"), dateFin.atTime(23, 59, 59).toLocalTime()
                    ));
                }
                
                if (query != null && !query.isEmpty()) {
                    Predicate signesPredicate = criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("signesCliniques")),
                        "%" + query.toUpperCase() + "%"
                    );
                    
                    Predicate observationsPredicate = criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("observations")),
                        "%" + query.toUpperCase() + "%"
                    );
                    
                    predicates.add(criteriaBuilder.or(signesPredicate, observationsPredicate));
                }
                
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            };
            
            return surveillanceRepository.findAll(spec).stream()
                .map(this::mapSurveillanceToDTO)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("❌ Erreur dans searchSurveillances: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    private List<TraceElementDTO> searchTraceLogs(
            Long id,
            String reference,
            LocalDate dateDebut,
            LocalDate dateFin,
            String utilisateur,
            String statut,
            String query) {
        
        try {
            LocalDateTime startDateTime = dateDebut != null ? dateDebut.atStartOfDay() : null;
            LocalDateTime endDateTime = dateFin != null ? dateFin.atTime(23, 59, 59) : null;
            
            Long userId = null;
            if (utilisateur != null && !utilisateur.isEmpty()) {
                try {
                    userId = Long.parseLong(utilisateur);
                } catch (NumberFormatException e) {
                    log.warn("⚠️ Utilisateur invalide pour la recherche de logs: {}", utilisateur);
                }
            }
            
            // Si query n'est pas vide, recherche générique
            if (query != null && !query.isEmpty()) {
                return tracabiliteRepository.searchByKeywordAndDate(
                    query, startDateTime, endDateTime
                ).stream()
                .map(this::mapTraceLogToDTO)
                .collect(Collectors.toList());
            }
            
            // Sinon recherche par critères spécifiques
            return tracabiliteRepository.searchLogs(
                reference,  // entityType = reference
                statut,     // action = statut
                userId,     // userId
                startDateTime,
                endDateTime
            ).stream()
            .map(this::mapTraceLogToDTO)
            .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("❌ Erreur dans searchTraceLogs: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    // ========== HISTORIQUE D'ENTITÉ ==========
    
    @Transactional(readOnly = true)
    public List<TraceElementDTO> getHistoriqueEntite(String type, Long id) {
        log.info("📜 Historique pour {}/{}", type, id);
        
        try {
            List<TraceElementDTO> historique = new ArrayList<>();
            
            // Récupérer l'entité principale
            TraceElementDTO entiteTrace = getEntiteByIdSafe(type, id);
            if (entiteTrace != null) {
                historique.add(entiteTrace);
            }
            
            // Ajouter les logs de traçabilité
            historique.addAll(getTraceLogsForEntitySafe(type, id));
            
            // Trier par date
            historique.sort(Comparator.comparing(TraceElementDTO::getDate));
            
            log.info("✅ Historique récupéré: {} éléments", historique.size());
            return historique;
            
        } catch (Exception e) {
            log.error("❌ Erreur dans getHistoriqueEntite pour {}/{}: {}", type, id, e.getMessage(), e);
            return getHistoriqueMinimal(type, id);
        }
    }
    
    private List<TraceElementDTO> getHistoriqueMinimal(String type, Long id) {
        List<TraceElementDTO> minimal = new ArrayList<>();
        
        try {
            // Ajouter seulement l'entité de base
            TraceElementDTO entite = getEntiteByIdSafe(type, id);
            if (entite != null) {
                minimal.add(entite);
            }
        } catch (Exception e) {
            log.debug("Impossible d'ajouter l'entité à l'historique minimal");
        }
        
        return minimal;
    }
    
    // ========== CHAÎNE COMPLÈTE ==========
    
    
    // ========== CHAÎNE MÉTIER COMPLÈTE ==========

    @Transactional(readOnly = true)
    public List<TraceElementDTO> getEntityChain(String type, Long id) {
        log.info("⛓️ Reconstruction de la chaîne métier pour {}/{}", type, id);

        try {
            LinkedHashMap<String, TraceElementDTO> chainMap = new LinkedHashMap<>();
            String normalizedType = normalizeTraceType(type);

            switch (normalizedType) {
                case "incident" -> buildChainFromIncident(id, chainMap);
                case "transfusion" -> buildChainFromTransfusion(id, chainMap);
                case "surveillance" -> buildChainFromSurveillance(id, chainMap);
                case "produit" -> buildChainFromProduit(id, chainMap);
                case "delivrance" -> buildChainFromDelivrance(id, chainMap);
                case "demande" -> buildChainFromDemande(id, chainMap);
                default -> {
                    log.warn("⚠️ Type non pris en charge pour la chaîne métier: {}", type);
                    TraceElementDTO entite = getEntiteByIdSafe(type, id);
                    if (entite != null) {
                        entite.setRelation("Élément source");
                        chainMap.put(buildTraceKey(entite.getType(), entite.getId()), entite);
                    }
                }
            }

            List<TraceElementDTO> chain = new ArrayList<>(chainMap.values());
            for (int i = 0; i < chain.size(); i++) {
                TraceElementDTO item = chain.get(i);
                item.setEtape(i + 1);
                if (item.getDetails() == null || item.getDetails().isBlank()) {
                    item.setDetails("Étape " + (i + 1) + " de la chaîne métier");
                }
            }

            log.info("✅ Chaîne métier reconstruite: {} éléments", chain.size());
            return chain;

        } catch (Exception e) {
            log.error("❌ Erreur dans getEntityChain pour {}/{}: {}", type, id, e.getMessage(), e);
            return getChainSimplifiee(type, id);
        }
    }

    private void buildChainFromIncident(Long incidentId, LinkedHashMap<String, TraceElementDTO> chainMap) {
        incidentRepository.findById(incidentId).ifPresent(incident -> {
            Transfusion transfusion = incident.getTransfusion();
            ProduitSanguin produit = transfusion != null ? transfusion.getProduitSanguin() : null;
            Delivrance delivrance = produit != null ? produit.getDelivrance() : null;
            Demande demande = delivrance != null ? delivrance.getDemande() : null;

            addDemandeNode(chainMap, demande, "Demande à l'origine du parcours");
            addDelivranceNode(chainMap, delivrance, "Délivrance liée à la demande");
            addProduitsForDelivrance(chainMap, delivrance, produit != null ? produit.getId() : null);
            addTransfusionsForDelivrance(chainMap, delivrance, transfusion != null ? transfusion.getId() : null);
            addTransfusionNode(chainMap, transfusion, "Transfusion concernée par l'incident", true);
            addSurveillancesForTransfusion(chainMap, transfusion);
            addIncidentNode(chainMap, incident, "Élément source : incident transfusionnel", true);
        });
    }

    private void buildChainFromTransfusion(Long transfusionId, LinkedHashMap<String, TraceElementDTO> chainMap) {
        transfusionRepository.findById(transfusionId).ifPresent(transfusion -> {
            ProduitSanguin produit = transfusion.getProduitSanguin();
            Delivrance delivrance = produit != null ? produit.getDelivrance() : null;
            Demande demande = delivrance != null ? delivrance.getDemande() : null;

            addDemandeNode(chainMap, demande, "Demande à l'origine du parcours");
            addDelivranceNode(chainMap, delivrance, "Délivrance associée");
            addProduitsForDelivrance(chainMap, delivrance, produit != null ? produit.getId() : null);
            addTransfusionNode(chainMap, transfusion, "Élément source : transfusion", true);
            addSurveillancesForTransfusion(chainMap, transfusion);
            addIncidentNode(chainMap, transfusion.getIncidentTransfusionnel(), "Incident lié à la transfusion", false);
        });
    }

    private void buildChainFromSurveillance(Long surveillanceId, LinkedHashMap<String, TraceElementDTO> chainMap) {
        surveillanceRepository.findById(surveillanceId).ifPresent(surveillance -> {
            Transfusion transfusion = surveillance.getTransfusion();
            ProduitSanguin produit = transfusion != null ? transfusion.getProduitSanguin() : null;
            Delivrance delivrance = produit != null ? produit.getDelivrance() : null;
            Demande demande = delivrance != null ? delivrance.getDemande() : null;

            addDemandeNode(chainMap, demande, "Demande à l'origine du parcours");
            addDelivranceNode(chainMap, delivrance, "Délivrance associée");
            addProduitsForDelivrance(chainMap, delivrance, produit != null ? produit.getId() : null);
            addTransfusionNode(chainMap, transfusion, "Transfusion surveillée", false);
            addSurveillancesForTransfusion(chainMap, transfusion, surveillanceId);
            addIncidentNode(chainMap, transfusion != null ? transfusion.getIncidentTransfusionnel() : null, "Incident éventuellement lié", false);
        });
    }

    private void buildChainFromProduit(Long produitId, LinkedHashMap<String, TraceElementDTO> chainMap) {
        produitSanguinRepository.findById(produitId).ifPresent(produit -> {
            Delivrance delivrance = produit.getDelivrance();
            Demande demande = delivrance != null ? delivrance.getDemande() : null;
            Transfusion transfusion = produit.getTransfusion();

            addDemandeNode(chainMap, demande, "Demande à l'origine du parcours");
            addDelivranceNode(chainMap, delivrance, "Délivrance contenant le produit");
            addProduitsForDelivrance(chainMap, delivrance, produitId);
            addTransfusionNode(chainMap, transfusion, "Transfusion du produit source", false);
            addSurveillancesForTransfusion(chainMap, transfusion);
            addIncidentNode(chainMap, transfusion != null ? transfusion.getIncidentTransfusionnel() : null, "Incident éventuellement lié", false);

            TraceElementDTO produitNode = chainMap.get(buildTraceKey("produit", produitId));
            if (produitNode != null) {
                produitNode.setRelation("Élément source : produit sanguin");
                produitNode.setDetails("Produit sélectionné dans la chaîne métier");
            }
        });
    }

    private void buildChainFromDelivrance(Long delivranceId, LinkedHashMap<String, TraceElementDTO> chainMap) {
        delivranceRepository.findByIdWithDetails(delivranceId).ifPresent(delivrance -> {
            addDemandeNode(chainMap, delivrance.getDemande(), "Demande à l'origine du parcours");
            addDelivranceNode(chainMap, delivrance, "Élément source : délivrance");
            addProduitsForDelivrance(chainMap, delivrance, null);
            addTransfusionsForDelivrance(chainMap, delivrance, null);
        });
    }

    private void buildChainFromDemande(Long demandeId, LinkedHashMap<String, TraceElementDTO> chainMap) {
        demandeRepository.findByIdWithRelations(demandeId).ifPresent(demande -> {
            addDemandeNode(chainMap, demande, "Élément source : demande");
            Delivrance delivrance = demande.getDelivrance();
            addDelivranceNode(chainMap, delivrance, "Délivrance issue de la demande");
            addProduitsForDelivrance(chainMap, delivrance, null);
            addTransfusionsForDelivrance(chainMap, delivrance, null);
        });
    }

    private void addDemandeNode(LinkedHashMap<String, TraceElementDTO> chainMap, Demande demande, String relation) {
        if (demande == null) return;
        TraceElementDTO dto = mapDemandeToDTO(demande);
        dto.setRelation(relation);
        dto.setDetails("Début du parcours métier");
        chainMap.putIfAbsent(buildTraceKey(dto.getType(), dto.getId()), dto);
    }

    private void addDelivranceNode(LinkedHashMap<String, TraceElementDTO> chainMap, Delivrance delivrance, String relation) {
        if (delivrance == null) return;
        TraceElementDTO dto = mapDelivranceToDTO(delivrance);
        dto.setRelation(relation);
        dto.setDetails("Étape de mise à disposition / délivrance");
        chainMap.putIfAbsent(buildTraceKey(dto.getType(), dto.getId()), dto);
    }

    private void addProduitsForDelivrance(LinkedHashMap<String, TraceElementDTO> chainMap, Delivrance delivrance, Long highlightedProduitId) {
        if (delivrance == null || delivrance.getProduitsSanguins() == null) return;

        List<ProduitSanguin> produits = new ArrayList<>(delivrance.getProduitsSanguins());
        produits.sort(Comparator.comparing(ProduitSanguin::getId));

        for (ProduitSanguin produit : produits) {
            TraceElementDTO dto = mapProduitToDTO(produit);
            boolean highlighted = Objects.equals(produit.getId(), highlightedProduitId);
            dto.setRelation(highlighted
                    ? "Produit concerné dans cette chaîne"
                    : "Autre produit délivré dans la même délivrance");
            dto.setDetails(highlighted
                    ? "Produit directement lié à l'élément source"
                    : "Produit frère de la même délivrance");
            chainMap.putIfAbsent(buildTraceKey(dto.getType(), dto.getId()), dto);
        }
    }

    private void addTransfusionsForDelivrance(LinkedHashMap<String, TraceElementDTO> chainMap, Delivrance delivrance, Long highlightedTransfusionId) {
        if (delivrance == null || delivrance.getId() == null) return;

        List<Transfusion> transfusions = transfusionRepository.findByDelivranceId(delivrance.getId());
        transfusions.sort(Comparator.comparing(Transfusion::getId));

        for (Transfusion transfusion : transfusions) {
            boolean highlighted = Objects.equals(transfusion.getId(), highlightedTransfusionId);
            addTransfusionNode(chainMap, transfusion,
                    highlighted ? "Transfusion concernée dans cette chaîne" : "Transfusion issue de cette délivrance",
                    highlighted);

            addSurveillancesForTransfusion(chainMap, transfusion);
            addIncidentNode(chainMap, transfusion.getIncidentTransfusionnel(),
                    highlighted ? "Incident lié à la transfusion source" : "Incident lié à cette transfusion",
                    false);
        }
    }

    private void addTransfusionNode(LinkedHashMap<String, TraceElementDTO> chainMap, Transfusion transfusion, String relation, boolean source) {
        if (transfusion == null) return;
        TraceElementDTO dto = mapTransfusionToDTO(transfusion);
        dto.setRelation(relation);
        dto.setDetails(source ? "Élément clé de la chaîne métier" : "Étape de réalisation de la transfusion");
        chainMap.putIfAbsent(buildTraceKey(dto.getType(), dto.getId()), dto);
    }

    private void addSurveillancesForTransfusion(LinkedHashMap<String, TraceElementDTO> chainMap, Transfusion transfusion) {
        addSurveillancesForTransfusion(chainMap, transfusion, null);
    }

    private void addSurveillancesForTransfusion(LinkedHashMap<String, TraceElementDTO> chainMap, Transfusion transfusion, Long highlightedSurveillanceId) {
        if (transfusion == null || transfusion.getSurveillances() == null) return;

        List<Surveillance> surveillances = new ArrayList<>(transfusion.getSurveillances());
        surveillances.sort(Comparator.comparing(Surveillance::getId));

        for (Surveillance surveillance : surveillances) {
            TraceElementDTO dto = mapSurveillanceToDTO(surveillance);
            boolean highlighted = Objects.equals(surveillance.getId(), highlightedSurveillanceId);
            dto.setRelation(highlighted
                    ? "Surveillance source dans la chaîne"
                    : "Surveillance réalisée pendant la transfusion");
            dto.setDetails(highlighted
                    ? "Élément source : surveillance"
                    : "Contrôle clinique rattaché à la transfusion");
            chainMap.putIfAbsent(buildTraceKey(dto.getType(), dto.getId()), dto);
        }
    }

    private void addIncidentNode(LinkedHashMap<String, TraceElementDTO> chainMap, IncidentTransfusionnel incident, String relation, boolean source) {
        if (incident == null) return;
        TraceElementDTO dto = mapIncidentToDTO(incident);
        dto.setRelation(relation);
        dto.setDetails(source ? "Élément source : incident transfusionnel" : "Événement indésirable lié à la transfusion");
        chainMap.putIfAbsent(buildTraceKey(dto.getType(), dto.getId()), dto);
    }

    private String buildTraceKey(String type, Long id) {
        return normalizeTraceType(type) + "-" + id;
    }

    private String normalizeTraceType(String type) {
        if (type == null) return "";
        String value = type.trim().toLowerCase();
        return switch (value) {
            case "produits", "produit-sanguin", "produitsanguin" -> "produit";
            case "demandes" -> "demande";
            case "delivrances" -> "delivrance";
            case "transfusions" -> "transfusion";
            case "incidents" -> "incident";
            case "surveillances" -> "surveillance";
            case "logs" -> "log";
            default -> value;
        };
    }

    private List<TraceElementDTO> getChainSimplifiee(String type, Long id) {
        List<TraceElementDTO> chain = new ArrayList<>();

        try {
            TraceElementDTO entite = getEntiteByIdSafe(type, id);
            if (entite != null) {
                entite.setEtape(1);
                entite.setRelation("Élément source");
                entite.setDetails("Chaîne métier partielle - seules les informations de base sont disponibles");
                chain.add(entite);
            }
        } catch (Exception e) {
            log.debug("Impossible d'ajouter l'entité à la chaîne simplifiée");
        }

        return chain;
    }

    
    // ========== STATISTIQUES ==========
    
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistiquesTraces() {
        log.info("📊 Calcul des statistiques traçabilité");
        
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Totaux par type d'entité
            stats.put("totalProduits", produitSanguinRepository.count());
            stats.put("totalDemandes", demandeRepository.count());
            stats.put("totalDelivrances", delivranceRepository.count());
            stats.put("totalTransfusions", transfusionRepository.count());
            stats.put("totalIncidents", incidentRepository.count());
            stats.put("totalSurveillances", surveillanceRepository.count());
            stats.put("totalLogs", tracabiliteRepository.count());
            
            // Totaux par statut
            stats.put("produitsDisponibles", produitSanguinRepository.countByEtat("DISPONIBLE"));
            stats.put("demandesEnAttente", demandeRepository.countByStatut("EN ATTENTE"));
            stats.put("demandesValidees", demandeRepository.countByStatut("VALIDÉE"));
            stats.put("incidentsNonValides", incidentRepository.countByDateValidationIsNull());
            stats.put("incidentsValides", incidentRepository.countByDateValidationIsNotNull());
            
            // Statistiques des logs
            List<Object[]> logStats = tracabiliteRepository.countLogsByEntityType();
            Map<String, Long> logsParType = new HashMap<>();
            logStats.forEach(stat -> {
                String type = (String) stat[0];
                Long count = (Long) stat[1];
                logsParType.put(type, count);
            });
            stats.put("logsParType", logsParType);
            
            // Activité des 7 derniers jours
            LocalDate septJours = LocalDate.now().minusDays(7);
            Map<String, Long> activiteParJour = new HashMap<>();
            
            for (int i = 0; i < 7; i++) {
                LocalDate jour = septJours.plusDays(i);
                String dateStr = jour.format(DATE_FORMATTER);
                
                long count = demandeRepository.countByDateHeureDemandeBetween(
                    jour.atStartOfDay(),
                    jour.atTime(23, 59, 59)
                );
                
                activiteParJour.put(dateStr, count);
            }
            stats.put("activiteParJour", activiteParJour);
            
            // Activité par heure aujourd'hui
            List<Object[]> activiteParHeure = tracabiliteRepository.getTodayActivityByHour();
            Map<String, Long> activiteHeure = new HashMap<>();
            activiteParHeure.forEach(stat -> {
                Integer hour = (Integer) stat[0];
                Long count = (Long) stat[1];
                activiteHeure.put(String.format("%02dh", hour), count);
            });
            stats.put("activiteParHeure", activiteHeure);
            
            // Top utilisateurs
            LocalDateTime dernierMois = LocalDateTime.now().minusMonths(1);
            List<Object[]> topUsersStats = tracabiliteRepository.getTopUsersByActivity(
                dernierMois, LocalDateTime.now());
            
            Map<String, Long> topUtilisateurs = new HashMap<>();
            topUsersStats.forEach(stat -> {
                Long userId = (Long) stat[0];
                Long count = (Long) stat[1];
                topUtilisateurs.put("Utilisateur " + userId, count);
            });
            stats.put("topUtilisateurs", topUtilisateurs);
            
            // Activité récente (10 dernières actions)
            List<TraceLog> logsRecents = tracabiliteRepository.findByTimestampBetween(
                LocalDateTime.now().minusDays(1), LocalDateTime.now());
            
            List<TraceElementDTO> activiteRecente = logsRecents.stream()
                .limit(10)
                .map(this::mapTraceLogToDTO)
                .collect(Collectors.toList());
            
            stats.put("activiteRecente", activiteRecente);
            
            log.info("✅ Statistiques calculées avec succès");
            
        } catch (Exception e) {
            log.error("❌ Erreur lors du calcul des statistiques: {}", e.getMessage(), e);
            stats.put("error", "Impossible de calculer les statistiques");
            stats.put("message", e.getMessage());
        }
        
        return stats;
    }
    
    // ========== MÉTHODES UTILITAIRES POUR PATIENTS ==========
    
    private Long getNombrePatientsDistincts() {
        try {
            List<String> numerosDossiers = demandeRepository.findAll().stream()
                .map(Demande::getPatientNumDossier)
                .distinct()
                .collect(Collectors.toList());
            return (long) numerosDossiers.size();
        } catch (Exception e) {
            log.error("Erreur getNombrePatientsDistincts: {}", e.getMessage());
            return 0L;
        }
    }
    
    private Map<String, Long> getTopGroupesSanguins() {
        try {
            return demandeRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                    Demande::getGroupeSanguinPatient,
                    Collectors.counting()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (e1, e2) -> e1,
                    LinkedHashMap::new
                ));
        } catch (Exception e) {
            log.error("Erreur getTopGroupesSanguins: {}", e.getMessage());
            return new HashMap<>();
        }
    }
    
    // ========== MÉTHODES SAFE POUR LA RÉCUPÉRATION DES DONNÉES ==========
    
    private TraceElementDTO getEntiteByIdSafe(String type, Long id) {
        try {
            if (id == null) {
                log.warn("ID null pour getEntiteByIdSafe type: {}", type);
                return null;
            }
            
            switch (type.toLowerCase()) {
                case "produit":
                    return produitSanguinRepository.findById(id)
                        .map(this::mapProduitToDTO)
                        .orElse(null);
                        
                case "demande":
                    return demandeRepository.findById(id)
                        .map(this::mapDemandeToDTO)
                        .orElse(null);
                        
                case "delivrance":
                    return delivranceRepository.findById(id)
                        .map(this::mapDelivranceToDTO)
                        .orElse(null);
                        
                case "transfusion":
                    return transfusionRepository.findById(id)
                        .map(this::mapTransfusionToDTO)
                        .orElse(null);
                        
                case "incident":
                    return incidentRepository.findById(id)
                        .map(this::mapIncidentToDTO)
                        .orElse(null);
                        
                case "surveillance":
                    return surveillanceRepository.findById(id)
                        .map(this::mapSurveillanceToDTO)
                        .orElse(null);
                        
                default:
                    log.warn("Type non reconnu pour getEntiteByIdSafe: {}", type);
                    return null;
            }
        } catch (Exception e) {
            log.error("❌ Erreur dans getEntiteByIdSafe pour {}/{}: {}", type, id, e.getMessage());
            return null;
        }
    }
    
    private List<TraceElementDTO> getTraceLogsForEntitySafe(String entityType, Long entityId) {
        try {
            if (entityId == null) {
                log.warn("EntityId null pour getTraceLogsForEntitySafe type: {}", entityType);
                return Collections.emptyList();
            }
            
            return tracabiliteRepository.findLogsByEntityOrderByDateDesc(entityType, entityId).stream()
                .map(this::mapTraceLogToDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("❌ Erreur dans getTraceLogsForEntitySafe pour {}/{}: {}", entityType, entityId, e.getMessage());
            return Collections.emptyList();
        }
    }
    
    // ========== MAPPERS ENTITÉ -> DTO ==========
    
    private TraceElementDTO mapProduitToDTO(ProduitSanguin produit) {
        TraceElementDTO dto = new TraceElementDTO();
        
        try {
            dto.setId(produit.getId());
            dto.setType("produit");
            dto.setLibelle("Produit Sanguin - " + produit.getCodeProduit());
            dto.setReference(produit.getCodeProduit());
            dto.setDescription(String.format("%s - %s%s", 
                produit.getTypeProduit(),
                produit.getGroupeSanguin(),
                produit.getRhesus()));
            dto.setDate(produit.getDatePrelevement() != null ? 
                produit.getDatePrelevement().atStartOfDay() : LocalDateTime.now());
            dto.setUtilisateur("Système");
            dto.setStatut(produit.getEtat());
            dto.setLien("/produits/" + produit.getId());
            
            Map<String, Object> entity = new HashMap<>();
            entity.put("id", produit.getId());
            entity.put("codeProduit", produit.getCodeProduit());
            entity.put("typeProduit", produit.getTypeProduit());
            entity.put("groupeSanguin", produit.getGroupeSanguin());
            entity.put("rhesus", produit.getRhesus());
            entity.put("datePeremption", produit.getDatePeremption());
            entity.put("volume", produit.getVolumeMl());
            entity.put("datePrelevement", produit.getDatePrelevement());
            dto.setEntity(entity);
            
        } catch (Exception e) {
            log.error("Erreur dans mapProduitToDTO pour produit {}: {}", produit.getId(), e.getMessage());
        }
        
        return dto;
    }
    
    private TraceElementDTO mapDemandeToDTO(Demande demande) {
        TraceElementDTO dto = new TraceElementDTO();
        
        try {
            String utilisateur = (demande.getPersonnel() != null) 
                ? demande.getPersonnel().getNom() + " " + demande.getPersonnel().getPrenom()
                : "Inconnu";
            
            dto.setId(demande.getId());
            dto.setType("demande");
            dto.setLibelle("Demande #" + demande.getId());
            dto.setReference(demande.getPatientNumDossier());
            dto.setDescription(String.format("%s demandé pour %s %s (Groupe: %s)",
                demande.getTypeProduitDemande(),
                demande.getPatientPrenom(),
                demande.getPatientNom(),
                demande.getGroupeSanguinPatient()));
            dto.setDate(demande.getDateHeureDemande() != null ? 
                demande.getDateHeureDemande() : LocalDateTime.now());
            dto.setUtilisateur(utilisateur);
            dto.setStatut(demande.getStatut());
            dto.setLien("/demandes/" + demande.getId());
            
            Map<String, Object> entity = new HashMap<>();
            entity.put("id", demande.getId());
            entity.put("patientNom", demande.getPatientNom());
            entity.put("patientPrenom", demande.getPatientPrenom());
            entity.put("patientNumDossier", demande.getPatientNumDossier());
            entity.put("patientDateNaissance", demande.getPatientDateNaissance());
            entity.put("groupeSanguinPatient", demande.getGroupeSanguinPatient());
            entity.put("typeProduit", demande.getTypeProduitDemande());
            entity.put("quantite", demande.getQuantiteDemande());
            entity.put("urgence", demande.getUrgence());
            entity.put("serviceDemandeur", demande.getServiceDemandeur());
            dto.setEntity(entity);
            
        } catch (Exception e) {
            log.error("Erreur dans mapDemandeToDTO pour demande {}: {}", demande.getId(), e.getMessage());
        }
        
        return dto;
    }
    
    private TraceElementDTO mapDelivranceToDTO(Delivrance delivrance) {
        TraceElementDTO dto = new TraceElementDTO();
        
        try {
            String utilisateur = (delivrance.getPersonnel() != null)
                ? delivrance.getPersonnel().getNom() + " " + delivrance.getPersonnel().getPrenom()
                : "Inconnu";
            
            dto.setId(delivrance.getId());
            dto.setType("delivrance");
            dto.setLibelle("Délivrance vers " + delivrance.getDestination());
            dto.setReference("DEL-" + delivrance.getId());
            dto.setDescription(String.format("%d produits délivrés",
                delivrance.getProduitsSanguins() != null ? delivrance.getProduitsSanguins().size() : 0));
            dto.setDate(delivrance.getDateHeureDelivrance() != null ? 
                delivrance.getDateHeureDelivrance() : LocalDateTime.now());
            dto.setUtilisateur(utilisateur);
            dto.setStatut("COMPLETE");
            dto.setLien("/delivrances/" + delivrance.getId());
            
            Map<String, Object> entity = new HashMap<>();
            entity.put("id", delivrance.getId());
            entity.put("destination", delivrance.getDestination());
            entity.put("modeTransport", delivrance.getModeTransport());
            entity.put("observations", delivrance.getObservations());
            entity.put("nombreProduits", delivrance.getProduitsSanguins() != null ? delivrance.getProduitsSanguins().size() : 0);
            dto.setEntity(entity);
            
        } catch (Exception e) {
            log.error("Erreur dans mapDelivranceToDTO pour delivrance {}: {}", delivrance.getId(), e.getMessage());
        }
        
        return dto;
    }
    
    private TraceElementDTO mapTransfusionToDTO(Transfusion transfusion) {
        TraceElementDTO dto = new TraceElementDTO();
        
        try {
            String medecin = (transfusion.getMedecin() != null)
                ? transfusion.getMedecin().getNom() + " " + transfusion.getMedecin().getPrenom()
                : "Inconnu";
            
            dto.setId(transfusion.getId());
            dto.setType("transfusion");
            dto.setLibelle("Transfusion - " + transfusion.getPatientNom() + " " + transfusion.getPatientPrenom());
            dto.setReference(transfusion.getPatientNumDossier());
            dto.setDescription(String.format("Transfusion effectuée par %s", medecin));
            dto.setDate(transfusion.getDateTransfusion() != null ? 
                transfusion.getDateTransfusion().atStartOfDay() : LocalDateTime.now());
            dto.setUtilisateur(medecin);
            dto.setStatut(transfusion.getTolerance());
            dto.setLien("/transfusions/" + transfusion.getId());
            
            Map<String, Object> entity = new HashMap<>();
            entity.put("id", transfusion.getId());
            entity.put("patientNom", transfusion.getPatientNom());
            entity.put("patientPrenom", transfusion.getPatientPrenom());
            entity.put("patientNumDossier", transfusion.getPatientNumDossier());
            entity.put("groupeSanguinPatient", transfusion.getGroupeSanguinPatient());
            entity.put("tolerance", transfusion.getTolerance());
            entity.put("effetsIndesirables", transfusion.getEffetsIndesirables());
            dto.setEntity(entity);
            
        } catch (Exception e) {
            log.error("Erreur dans mapTransfusionToDTO pour transfusion {}: {}", transfusion.getId(), e.getMessage());
        }
        
        return dto;
    }
    
    private TraceElementDTO mapIncidentToDTO(IncidentTransfusionnel incident) {
        TraceElementDTO dto = new TraceElementDTO();
        
        try {
            String statut = (incident.getDateValidation() != null)
                ? "VALIDE"
                : "NON_VALIDE";
            
            LocalDateTime incidentDateTime = null;
            if (incident.getDateIncident() != null && incident.getHeureIncident() != null) {
                incidentDateTime = incident.getDateIncident().atTime(incident.getHeureIncident());
            } else {
                incidentDateTime = LocalDateTime.now();
            }
            
            dto.setId(incident.getId());
            dto.setType("incident");
            dto.setLibelle("Incident - " + incident.getPatientNom() + " " + incident.getPatientPrenom());
            dto.setReference("INC-" + incident.getId());
            dto.setDescription(String.format("Incident déclaré par %s", incident.getNomDeclarant()));
            dto.setDate(incidentDateTime);
            dto.setUtilisateur(incident.getNomDeclarant());
            dto.setStatut(statut);
            dto.setLien("/incidents/" + incident.getId());
            
            Map<String, Object> entity = new HashMap<>();
            entity.put("id", incident.getId());
            entity.put("patientNom", incident.getPatientNom());
            entity.put("patientPrenom", incident.getPatientPrenom());
            entity.put("typeProduitTransfuse", incident.getTypeProduitTransfuse());
            entity.put("numeroLotProduit", incident.getNumeroLotProduit());
            entity.put("lieuIncident", incident.getLieuIncident());
            entity.put("descriptionIncident", incident.getDescriptionIncident());
            entity.put("valide", statut.equals("VALIDE"));
            dto.setEntity(entity);
            
        } catch (Exception e) {
            log.error("Erreur dans mapIncidentToDTO pour incident {}: {}", incident.getId(), e.getMessage());
        }
        
        return dto;
    }
    
    private TraceElementDTO mapSurveillanceToDTO(Surveillance surveillance) {
        TraceElementDTO dto = new TraceElementDTO();
        
        try {
            LocalDate dateReference = (surveillance.getTransfusion() != null && surveillance.getTransfusion().getDateTransfusion() != null)
                ? surveillance.getTransfusion().getDateTransfusion()
                : LocalDate.now();

            LocalDateTime dateTime = surveillance.getHeure() != null
                ? dateReference.atTime(surveillance.getHeure())
                : dateReference.atStartOfDay();
            
            dto.setId(surveillance.getId());
            dto.setType("surveillance");
            dto.setLibelle("Surveillance transfusion #" + 
                (surveillance.getTransfusion() != null ? surveillance.getTransfusion().getId() : "?"));
            dto.setReference("SURV-" + surveillance.getId());
            dto.setDescription(String.format("Température: %.1f°C, Pouls: %d",
                surveillance.getTemperature() != null ? surveillance.getTemperature() : 0.0,
                surveillance.getPouls() != null ? surveillance.getPouls() : 0));
            dto.setDate(dateTime);
            dto.setUtilisateur("Infirmier");
            dto.setStatut("COMPLETE");
            dto.setLien("/surveillances/" + surveillance.getId());
            
            Map<String, Object> entity = new HashMap<>();
            entity.put("id", surveillance.getId());
            entity.put("temperature", surveillance.getTemperature());
            entity.put("pouls", surveillance.getPouls());
            entity.put("tension", surveillance.getTension());
            entity.put("signesCliniques", surveillance.getSignesCliniques());
            entity.put("observations", surveillance.getObservations());
            dto.setEntity(entity);
            
        } catch (Exception e) {
            log.error("Erreur dans mapSurveillanceToDTO pour surveillance {}: {}", surveillance.getId(), e.getMessage());
        }
        
        return dto;
    }
    
    private TraceElementDTO mapTraceLogToDTO(TraceLog traceLog) {
        TraceElementDTO dto = new TraceElementDTO();
        
        try {
            String userName = (traceLog.getUser() != null) 
                ? traceLog.getUser().getNom() + " " + traceLog.getUser().getPrenom()
                : "Système";
            
            dto.setId(traceLog.getId());
            dto.setType("log");
            dto.setLibelle("Log - " + traceLog.getAction() + " " + traceLog.getEntityType());
            dto.setReference("LOG-" + traceLog.getId());
            dto.setDescription(String.format("%s sur %s #%d",
                traceLog.getAction(),
                traceLog.getEntityType(),
                traceLog.getEntityId()));
            dto.setDate(traceLog.getTimestamp() != null ? traceLog.getTimestamp() : LocalDateTime.now());
            dto.setUtilisateur(userName);
            dto.setStatut("LOGUE");
            dto.setLien("/tracabilite/logs/" + traceLog.getId());
            
            Map<String, Object> entity = new HashMap<>();
            entity.put("id", traceLog.getId());
            entity.put("action", traceLog.getAction());
            entity.put("entityType", traceLog.getEntityType());
            entity.put("entityId", traceLog.getEntityId());
            entity.put("details", traceLog.getDetails());
            entity.put("ipAddress", traceLog.getIpAddress());
            dto.setEntity(entity);
            
        } catch (Exception e) {
            log.error("Erreur dans mapTraceLogToDTO pour traceLog {}: {}", traceLog.getId(), e.getMessage());
        }
        
        return dto;
    }
    
    // ========== NOUVELLES MÉTHODES POUR LE CONTROLLER ==========
    
    @Transactional(readOnly = true)
    public List<TraceElementDTO> getLogsByEntity(String entityType, Long entityId) {
        log.info("📋 Récupération des logs pour {}/{}", entityType, entityId);
        
        try {
            return tracabiliteRepository.findByEntityTypeAndEntityId(entityType, entityId).stream()
                .map(this::mapTraceLogToDTO)
                .sorted(Comparator.comparing(TraceElementDTO::getDate).reversed())
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("❌ Erreur dans getLogsByEntity pour {}/{}: {}", entityType, entityId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    @Transactional
    public void logAction(String action, String entityType, Long entityId, 
                         String details, String ipAddress, String userAgent) {
        log.info("📝 Log action: {} - {} {} - Details: {}, IP: {}, User-Agent: {}", 
            action, entityType, entityId, details, ipAddress, userAgent);
        
        try {
            String realIpAddress = getClientIpAddress(ipAddress);
            
            TraceLog traceLog = new TraceLog();
            traceLog.setAction(action);
            traceLog.setEntityType(entityType);
            traceLog.setEntityId(entityId);
            traceLog.setDetails(details);
            traceLog.setIpAddress(realIpAddress);
            traceLog.setUserAgent(userAgent);
            traceLog.setTimestamp(LocalDateTime.now());
            
            tracabiliteRepository.save(traceLog);
            
            log.info("✅ Action loguée avec succès");
            
        } catch (Exception e) {
            log.error("❌ Erreur lors du log de l'action: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Transactional(readOnly = true)
    public List<TraceElementDTO> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("📅 Récupération des logs de {} à {}", startDate, endDate);
        
        try {
            return tracabiliteRepository.findByTimestampBetween(startDate, endDate).stream()
                .map(this::mapTraceLogToDTO)
                .sorted(Comparator.comparing(TraceElementDTO::getDate).reversed())
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("❌ Erreur dans getLogsByDateRange: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    @Transactional(readOnly = true)
    public List<TraceElementDTO> getRecentActivity(int limit) {
        log.info("🕐 Récupération des {} dernières activités", limit);
        
        try {
            Pageable pageable = PageRequest.of(0, limit);
            List<TraceLog> logs = tracabiliteRepository.findAllOrderByTimestampDesc(pageable);
            
            log.info("📊 {} logs récupérés", logs.size());
            
            return logs.stream()
                .map(this::mapTraceLogToDTO)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("❌ Erreur dans getRecentActivity: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
        
    @Transactional(readOnly = true)
    public List<TraceElementDTO> getUserActivity(Long userId, LocalDate startDate, LocalDate endDate) {
        log.info("👤 Récupération des activités de l'utilisateur {}", userId);
        
        try {
            LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusDays(30);
            LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();
            
            return tracabiliteRepository.findLogsByUserAndDateRange(userId, startDateTime, endDateTime).stream()
                .map(this::mapTraceLogToDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("❌ Erreur dans getUserActivity pour userId {}: {}", userId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    private String getClientIpAddress(String xForwardedFor) {
        if (xForwardedFor == null || xForwardedFor.isEmpty()) {
            return "127.0.0.1";
        }
        
        try {
            String[] ips = xForwardedFor.split(",");
            return ips[0].trim();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }
    
    // ========== MÉTHODES POUR LES PATIENTS ==========
    
    @Transactional(readOnly = true)
    public List<Map<String, Object>> searchPatients(String keyword) {
        log.info("🔍 Recherche patients avec keyword: {}", keyword);
        
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return Collections.emptyList();
            }
            
            List<Demande> demandes = demandeRepository.findAll().stream()
                .filter(d -> matchesPatientCriteria(d, keyword))
                .collect(Collectors.toList());
            
            Map<String, List<Demande>> demandesParPatient = demandes.stream()
                .collect(Collectors.groupingBy(Demande::getPatientNumDossier));
            
            List<Map<String, Object>> patients = new ArrayList<>();
            
            demandesParPatient.forEach((numDossier, demandesDuPatient) -> {
                if (!demandesDuPatient.isEmpty()) {
                    Demande premiereDemande = demandesDuPatient.get(0);
                    Map<String, Object> patientInfo = new HashMap<>();
                    
                    patientInfo.put("numDossier", premiereDemande.getPatientNumDossier());
                    patientInfo.put("nom", premiereDemande.getPatientNom());
                    patientInfo.put("prenom", premiereDemande.getPatientPrenom());
                    patientInfo.put("dateNaissance", premiereDemande.getPatientDateNaissance());
                    patientInfo.put("groupeSanguin", premiereDemande.getGroupeSanguinPatient());
                    patientInfo.put("nombreDemandes", demandesDuPatient.size());
                    patientInfo.put("derniereDemande", demandesDuPatient.stream()
                        .map(Demande::getDateHeureDemande)
                        .max(LocalDateTime::compareTo)
                        .orElse(null));
                    
                    patients.add(patientInfo);
                }
            });
            
            patients.sort((a, b) -> {
                String nomA = (String) a.get("nom");
                String nomB = (String) b.get("nom");
                return nomA.compareToIgnoreCase(nomB);
            });
            
            return patients;
            
        } catch (Exception e) {
            log.error("❌ Erreur dans searchPatients: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    private boolean matchesPatientCriteria(Demande demande, String keyword) {
        try {
            String keywordLower = keyword.toLowerCase();
            return (demande.getPatientNom() != null && demande.getPatientNom().toLowerCase().contains(keywordLower)) ||
                   (demande.getPatientPrenom() != null && demande.getPatientPrenom().toLowerCase().contains(keywordLower)) ||
                   (demande.getPatientNumDossier() != null && demande.getPatientNumDossier().toLowerCase().contains(keywordLower)) ||
                   (demande.getGroupeSanguinPatient() != null && demande.getGroupeSanguinPatient().toLowerCase().contains(keywordLower));
        } catch (Exception e) {
            log.error("Erreur dans matchesPatientCriteria: {}", e.getMessage());
            return false;
        }
    }
    
    @Transactional(readOnly = true)
    public List<TraceElementDTO> getPatientHistory(String numDossier) {
        log.info("📜 Historique pour patient: {}", numDossier);
        
        try {
            List<Demande> demandes = demandeRepository.findAll().stream()
                .filter(d -> numDossier.equals(d.getPatientNumDossier()))
                .sorted((a, b) -> b.getDateHeureDemande().compareTo(a.getDateHeureDemande()))
                .collect(Collectors.toList());
            
            if (demandes.isEmpty()) {
                return Collections.emptyList();
            }
            
            Demande premiereDemande = demandes.get(0);
            List<TraceElementDTO> historique = new ArrayList<>();
            
            // Information patient
            TraceElementDTO infoPatient = new TraceElementDTO();
            infoPatient.setType("patient");
            infoPatient.setLibelle("Patient: " + premiereDemande.getPatientNom() + " " + premiereDemande.getPatientPrenom());
            infoPatient.setDescription("Numéro dossier: " + premiereDemande.getPatientNumDossier());
            
            Map<String, Object> patientEntity = new HashMap<>();
            patientEntity.put("nom", premiereDemande.getPatientNom());
            patientEntity.put("prenom", premiereDemande.getPatientPrenom());
            patientEntity.put("dateNaissance", premiereDemande.getPatientDateNaissance());
            patientEntity.put("groupeSanguin", premiereDemande.getGroupeSanguinPatient());
            patientEntity.put("nombreDemandes", demandes.size());
            infoPatient.setEntity(patientEntity);
            
            historique.add(infoPatient);
            
            // Historique des demandes
            for (Demande demande : demandes) {
                historique.add(mapDemandeToDTO(demande));
            }
            
            // Trier par date
            historique.sort(Comparator.comparing(TraceElementDTO::getDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
            
            return historique;
            
        } catch (Exception e) {
            log.error("❌ Erreur dans getPatientHistory pour patient {}: {}", numDossier, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}