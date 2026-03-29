package com.hematrace.hematrace.service.impl;

import com.hematrace.hematrace.entite.IncidentTransfusionnel;
import com.hematrace.hematrace.repository.IncidentTransfusionnelRepository;
import com.hematrace.hematrace.service.StatistiquesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class StatistiquesServiceImpl implements StatistiquesService {

    @Autowired
    private IncidentTransfusionnelRepository incidentRepository;
    
    @Override
    public Map<String, Object> getStatistiquesIncidentsGlobale() {
        Map<String, Object> statistiques = new HashMap<>();
        
        List<IncidentTransfusionnel> incidents = incidentRepository.findAll();
        long total = incidents.size();
        long valides = incidents.stream()
            .filter(i -> i.getDateValidation() != null)
            .count();
        long nonValides = total - valides;
        
        // Statistiques par type de produit
        Map<String, Long> parTypeProduit = incidents.stream()
            .collect(Collectors.groupingBy(
                IncidentTransfusionnel::getTypeProduitTransfuse,
                Collectors.counting()
            ));
        
        // Statistiques par mois (année en cours)
        Map<String, Long> parMois = incidents.stream()
            .filter(i -> i.getDateIncident() != null)
            .filter(i -> i.getDateIncident().getYear() == LocalDate.now().getYear())
            .collect(Collectors.groupingBy(
                i -> i.getDateIncident().getMonth().toString(),
                Collectors.counting()
            ));
        
        statistiques.put("total", total);
        statistiques.put("valides", valides);
        statistiques.put("nonValides", nonValides);
        statistiques.put("parTypeProduit", parTypeProduit);
        statistiques.put("parMois", parMois);
        
        return statistiques;
    }
    
    @Override
    public Map<String, Long> getStatistiquesIncidentsParTypeProduit() {
        List<IncidentTransfusionnel> incidents = incidentRepository.findAll();
        
        return incidents.stream()
            .collect(Collectors.groupingBy(
                IncidentTransfusionnel::getTypeProduitTransfuse,
                Collectors.counting()
            ));
    }
    
    @Override
    public Map<String, Long> getStatistiquesIncidentsParMois(int annee) {
        List<IncidentTransfusionnel> incidents = incidentRepository.findAll();
        
        return incidents.stream()
            .filter(i -> i.getDateIncident() != null && i.getDateIncident().getYear() == annee)
            .collect(Collectors.groupingBy(
                i -> i.getDateIncident().getMonth().toString(),
                Collectors.counting()
            ));
    }
    
    @Override
    public Map<String, Long> getStatistiquesValidation() {
        Map<String, Long> stats = new HashMap<>();
        
        stats.put("valides", incidentRepository.countByValides());
        stats.put("nonValides", incidentRepository.countByNonValides());
        
        return stats;
    }
}