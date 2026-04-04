package com.hematrace.hematrace.service.impl;

import com.hematrace.hematrace.dto.dashboard.DashboardAlertDto;
import com.hematrace.hematrace.dto.dashboard.DashboardBloodGroupDto;
import com.hematrace.hematrace.dto.dashboard.DashboardOverviewDto;
import com.hematrace.hematrace.dto.dashboard.DashboardProductTypeDto;
import com.hematrace.hematrace.dto.dashboard.DashboardRecentActivitiesDto;
import com.hematrace.hematrace.dto.dashboard.DashboardRecentActivityDto;
import com.hematrace.hematrace.dto.dashboard.DashboardStatsDto;
import com.hematrace.hematrace.dto.dashboard.DashboardTrendPointDto;
import com.hematrace.hematrace.dto.dashboard.DashboardTrendsDto;
import com.hematrace.hematrace.entite.Delivrance;
import com.hematrace.hematrace.entite.Demande;
import com.hematrace.hematrace.entite.ProduitSanguin;
import com.hematrace.hematrace.entite.Transfusion;
import com.hematrace.hematrace.repository.DelivranceRepository;
import com.hematrace.hematrace.repository.DemandeRepository;
import com.hematrace.hematrace.repository.IncidentTransfusionnelRepository;
import com.hematrace.hematrace.repository.ProduitSanguinRepository;
import com.hematrace.hematrace.repository.TransfusionRepository;
import com.hematrace.hematrace.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final String STATUT_EN_ATTENTE = "EN ATTENTE";
    private static final String STATUT_VALIDEE = "VALIDÉE";
    private static final String STATUT_REJETEE = "REJETÉE";
    private static final String STATUT_DELIVREE = "DÉLIVRÉE";

    private static final String ETAT_DISPONIBLE = "DISPONIBLE";
    private static final String ETAT_UTILISE = "UTILISÉ";
    private static final String ETAT_EXPIRE = "EXPIRÉ";

    private final ProduitSanguinRepository produitSanguinRepository;
    private final DemandeRepository demandeRepository;
    private final DelivranceRepository delivranceRepository;
    private final TransfusionRepository transfusionRepository;
    private final IncidentTransfusionnelRepository incidentTransfusionnelRepository;

    @Override
    public DashboardOverviewDto getOverview() {
        LocalDate today = LocalDate.now();
        LocalDate in7Days = today.plusDays(7);

        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();
        LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();

        long totalProduits = produitSanguinRepository.count();
        long produitsDisponibles = produitSanguinRepository.countByEtatIgnoreCase(ETAT_DISPONIBLE);
        long produitsUtilises = produitSanguinRepository.countByEtatIgnoreCase(ETAT_UTILISE);
        long produitsDelivres = produitSanguinRepository.countProduitsDelivres();
        long produitsExpires = produitSanguinRepository.countExpired(today);
        long produitsProchesPeremption = produitSanguinRepository.countExpiringSoon(today, in7Days);

        long totalDemandes = demandeRepository.count();
        long demandesEnAttente = demandeRepository.countByStatut(STATUT_EN_ATTENTE);
        long demandesValidees = demandeRepository.countByStatut(STATUT_VALIDEE);
        long demandesRejetees = demandeRepository.countByStatut(STATUT_REJETEE);
        long demandesDelivrees = demandeRepository.countByStatut(STATUT_DELIVREE);
        long demandesUrgentes = demandeRepository.countByUrgenceTrue();

        long totalDelivrances = delivranceRepository.count();
        long delivrancesAujourdhui = delivranceRepository.countByDateHeureDelivranceBetween(startOfDay, startOfTomorrow);
        long delivrancesCeMois = delivranceRepository.countByDateHeureDelivranceBetween(startOfMonth, startOfTomorrow);

        long totalTransfusions = transfusionRepository.count();
        long transfusionsAvecEffets = transfusionRepository.countByEffetsIndesirablesTrue();
        long transfusionsSansEffets = Math.max(0, totalTransfusions - transfusionsAvecEffets);

        long totalIncidents = incidentTransfusionnelRepository.count();
        long incidentsValides = incidentTransfusionnelRepository.countByValides();
        long incidentsNonValides = incidentTransfusionnelRepository.countByNonValides();

DashboardStatsDto stats = DashboardStatsDto.builder()
        .totalProduits(totalProduits)
        .produitsDisponibles(produitsDisponibles)
        .produitsDelivres(produitsDelivres)
        .produitsUtilises(produitsUtilises)
        .produitsExpires(produitsExpires)
        .produitsProchesPeremption(produitsProchesPeremption)
        .totalDemandes(totalDemandes)
        .demandesEnAttente(demandesEnAttente)
        .demandesValidees(demandesValidees)
        .demandesRejetees(demandesRejetees)
        .demandesDelivrees(demandesDelivrees)
        .demandesUrgentes(demandesUrgentes)
        .totalDelivrances(totalDelivrances)
        .delivrancesAujourdhui(delivrancesAujourdhui)
        .delivrancesCeMois(delivrancesCeMois)
        .totalTransfusions(totalTransfusions)
        .transfusionsAvecEffets(transfusionsAvecEffets)
        .transfusionsSansEffets(transfusionsSansEffets)
        .totalIncidents(totalIncidents)
        .incidentsValides(incidentsValides)
        .incidentsNonValides(incidentsNonValides)
        .build();

        return DashboardOverviewDto.builder()
                .stats(stats)
                .alertes(buildAlertes(produitsExpires, produitsProchesPeremption, demandesUrgentes))
                .stockParGroupe(buildStockParGroupe())
                .stockParType(buildStockParType())
                .build();
    }

    @Override
    public DashboardRecentActivitiesDto getRecentActivities(int limit) {
        if (limit <= 0) {
            limit = 10;
        }

        return DashboardRecentActivitiesDto.builder()
                .dernieresDemandes(buildRecentDemandes(limit))
                .dernieresDelivrances(buildRecentDelivrances(limit))
                .dernieresTransfusions(buildRecentTransfusions(limit))
                .build();
    }

    @Override
    public DashboardTrendsDto getTrends(int days) {
        if (days <= 0) {
            days = 7;
        }

        LocalDate today = LocalDate.now();
        List<DashboardTrendPointDto> points = new ArrayList<>();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime startDateTime = date.atStartOfDay();
            LocalDateTime endDateTime = date.plusDays(1).atStartOfDay();

            long demandes = demandeRepository.countByDateHeureDemandeBetween(startDateTime, endDateTime);
            long delivrances = delivranceRepository.countByDateHeureDelivranceBetween(startDateTime, endDateTime);
            long transfusions = transfusionRepository.countByDateTransfusionBetween(date, date);
            long incidents = incidentTransfusionnelRepository.countByDateIncidentBetween(date, date);

            points.add(DashboardTrendPointDto.builder()
                    .label(date.toString())
                    .demandes(demandes)
                    .delivrances(delivrances)
                    .transfusions(transfusions)
                    .incidents(incidents)
                    .build());
        }

        return DashboardTrendsDto.builder()
                .points(points)
                .build();
    }

    private List<DashboardAlertDto> buildAlertes(long produitsExpires,
                                                 long produitsProchesPeremption,
                                                 long demandesUrgentes) {
        List<DashboardAlertDto> alertes = new ArrayList<>();

        if (produitsExpires > 0) {
            alertes.add(DashboardAlertDto.builder()
                    .niveau("DANGER")
                    .code("PRODUITS_EXPIRES")
                    .titre("Produits expirés")
                    .message(produitsExpires + " produit(s) sanguin(s) sont expiré(s).")
                    .valeur(produitsExpires)
                    .build());
        }

        if (produitsProchesPeremption > 0) {
            alertes.add(DashboardAlertDto.builder()
                    .niveau("WARNING")
                    .code("PRODUITS_PROCHES_PEREMPTION")
                    .titre("Péremption proche")
                    .message(produitsProchesPeremption + " produit(s) expirent dans moins de 7 jours.")
                    .valeur(produitsProchesPeremption)
                    .build());
        }

        if (demandesUrgentes > 0) {
            alertes.add(DashboardAlertDto.builder()
                    .niveau("WARNING")
                    .code("DEMANDES_URGENTES")
                    .titre("Demandes urgentes")
                    .message(demandesUrgentes + " demande(s) urgente(s) sont en cours.")
                    .valeur(demandesUrgentes)
                    .build());
        }

        return alertes;
    }

    private List<DashboardBloodGroupDto> buildStockParGroupe() {
        List<ProduitSanguin> tousLesProduits = produitSanguinRepository.findAll();
        List<ProduitSanguin> produitsDisponibles = produitSanguinRepository.findDisponibles();

        Map<String, Long> totalMap = new LinkedHashMap<>();
        Map<String, Long> disponibleMap = new LinkedHashMap<>();

        for (ProduitSanguin produit : tousLesProduits) {
            String groupe = buildGroupeLabel(produit.getGroupeSanguin(), produit.getRhesus());
            totalMap.put(groupe, totalMap.getOrDefault(groupe, 0L) + 1);
        }

        for (ProduitSanguin produit : produitsDisponibles) {
            String groupe = buildGroupeLabel(produit.getGroupeSanguin(), produit.getRhesus());
            disponibleMap.put(groupe, disponibleMap.getOrDefault(groupe, 0L) + 1);
        }

        List<DashboardBloodGroupDto> result = new ArrayList<>();
        for (Map.Entry<String, Long> entry : totalMap.entrySet()) {
            String groupe = entry.getKey();
            long total = entry.getValue();
            long disponibles = disponibleMap.getOrDefault(groupe, 0L);

            result.add(DashboardBloodGroupDto.builder()
                    .groupe(groupe)
                    .total(total)
                    .disponibles(disponibles)
                    .build());
        }

        result.sort(Comparator.comparing(DashboardBloodGroupDto::getGroupe, Comparator.nullsLast(String::compareTo)));

        return result;
    }

    private List<DashboardProductTypeDto> buildStockParType() {
        List<ProduitSanguin> tousLesProduits = produitSanguinRepository.findAll();
        List<ProduitSanguin> produitsDisponibles = produitSanguinRepository.findDisponibles();

        Map<String, Long> totalMap = new LinkedHashMap<>();
        Map<String, Long> disponibleMap = new LinkedHashMap<>();

        for (ProduitSanguin produit : tousLesProduits) {
            String type = safeString(produit.getTypeProduit());
            if (type.isBlank()) {
                type = "INCONNU";
            }
            totalMap.put(type, totalMap.getOrDefault(type, 0L) + 1);
        }

        for (ProduitSanguin produit : produitsDisponibles) {
            String type = safeString(produit.getTypeProduit());
            if (type.isBlank()) {
                type = "INCONNU";
            }
            disponibleMap.put(type, disponibleMap.getOrDefault(type, 0L) + 1);
        }

        List<DashboardProductTypeDto> result = new ArrayList<>();
        for (Map.Entry<String, Long> entry : totalMap.entrySet()) {
            String type = entry.getKey();
            long total = entry.getValue();
            long disponibles = disponibleMap.getOrDefault(type, 0L);

            result.add(DashboardProductTypeDto.builder()
                    .type(type)
                    .total(total)
                    .disponibles(disponibles)
                    .build());
        }

        result.sort(Comparator.comparing(DashboardProductTypeDto::getType, Comparator.nullsLast(String::compareTo)));

        return result;
    }

    private List<DashboardRecentActivityDto> buildRecentDemandes(int limit) {
        List<Demande> demandes = demandeRepository.findAllByOrderByDateHeureDemandeDesc();

        List<DashboardRecentActivityDto> result = new ArrayList<>();
        int max = Math.min(limit, demandes.size());

        for (int i = 0; i < max; i++) {
            Demande demande = demandes.get(i);

            String patient = buildPatientNom(demande.getPatientPrenom(), demande.getPatientNom());
            String service = safeString(demande.getServiceDemandeur());

            result.add(DashboardRecentActivityDto.builder()
                    .id(demande.getId())
                    .type("DEMANDE")
                    .titre("Nouvelle demande")
                    .description("Demande pour " + patient + (service.isBlank() ? "" : " - Service: " + service))
                    .statut(safeString(demande.getStatut()))
                    .dateHeure(demande.getDateHeureDemande())
                    .build());
        }

        return result;
    }

    private List<DashboardRecentActivityDto> buildRecentDelivrances(int limit) {
        List<Delivrance> delivrances = delivranceRepository.findTop10ByOrderByDateHeureDelivranceDesc();

        List<DashboardRecentActivityDto> result = new ArrayList<>();
        int max = Math.min(limit, delivrances.size());

        for (int i = 0; i < max; i++) {
            Delivrance delivrance = delivrances.get(i);

            result.add(DashboardRecentActivityDto.builder()
                    .id(delivrance.getId())
                    .type("DÉLIVRANCE")
                    .titre("Nouvelle délivrance")
                    .description("Destination: " + safeString(delivrance.getDestination()))
                    .statut("DÉLIVRÉE")
                    .dateHeure(delivrance.getDateHeureDelivrance())
                    .build());
        }

        return result;
    }

    private List<DashboardRecentActivityDto> buildRecentTransfusions(int limit) {
        List<Transfusion> transfusions = transfusionRepository.findTop10ByOrderByDateTransfusionDesc();

        List<DashboardRecentActivityDto> result = new ArrayList<>();
        int max = Math.min(limit, transfusions.size());

        for (int i = 0; i < max; i++) {
            Transfusion transfusion = transfusions.get(i);

            String statut = Boolean.TRUE.equals(transfusion.getEffetsIndesirables())
                    ? "Avec effets indésirables"
                    : "Sans effets indésirables";

            result.add(DashboardRecentActivityDto.builder()
                    .id(transfusion.getId())
                    .type("TRANSFUSION")
                    .titre("Nouvelle transfusion")
                    .description("Transfusion enregistrée")
                    .statut(statut)
                    .dateHeure(toLocalDateTime(transfusion.getDateTransfusion()))
                    .build());
        }

        return result;
    }

    private String buildGroupeLabel(String groupeSanguin, String rhesus) {
        String groupe = safeString(groupeSanguin);
        String rh = safeString(rhesus);

        if (groupe.isBlank() && rh.isBlank()) {
            return "INCONNU";
        }

        return (groupe + rh).trim();
    }

    private String buildPatientNom(String prenom, String nom) {
        String fullName = (safeString(prenom) + " " + safeString(nom)).trim();
        return fullName.isBlank() ? "Patient inconnu" : fullName;
    }

    private String safeString(String value) {
        return value == null ? "" : value.trim();
    }

    private LocalDateTime toLocalDateTime(LocalDate date) {
        return date == null ? null : date.atStartOfDay();
    }

    
}