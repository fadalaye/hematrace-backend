package com.hematrace.hematrace.service.impl;

import com.hematrace.hematrace.entite.Surveillance;
import com.hematrace.hematrace.entite.Transfusion;
import com.hematrace.hematrace.repository.SurveillanceRepository;
import com.hematrace.hematrace.repository.TransfusionRepository;
import com.hematrace.hematrace.service.SurveillanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class SurveillanceServiceImpl implements SurveillanceService {

    @Autowired
    private SurveillanceRepository surveillanceRepository;
    
    @Autowired
    private TransfusionRepository transfusionRepository;

    @Override
    public Surveillance creerSurveillance(Surveillance surveillance) {
        System.out.println("🚀 Début création surveillance:");
        System.out.println("- Surveillance reçue: " + surveillance);
        
        // Validation des relations obligatoires
        if (surveillance.getTransfusion() == null || surveillance.getTransfusion().getId() == null) {
            throw new RuntimeException("Une transfusion doit être associée à la surveillance");
        }
        
        // Vérifier que la transfusion existe
        Transfusion transfusion = transfusionRepository.findById(surveillance.getTransfusion().getId())
                .orElseThrow(() -> new RuntimeException("Transfusion non trouvée avec l'id: " + surveillance.getTransfusion().getId()));
        
        System.out.println("- Transfusion trouvée: ID=" + transfusion.getId());
        
        // Définir l'heure si non spécifiée
        if (surveillance.getHeure() == null) {
            surveillance.setHeure(LocalTime.now());
        }
        
        // CORRECTION: Gérer les valeurs null avant trim()
        if (surveillance.getTension() != null) {
            surveillance.setTension(surveillance.getTension().trim());
            // Validation du format si tension non vide
            if (!surveillance.getTension().isEmpty()) {
                if (!isValidTensionFormat(surveillance.getTension())) {
                    throw new RuntimeException("Le format de la tension est invalide. Format attendu: 'systolique/diastolique' (ex: 120/80)");
                }
            }
        } else {
            surveillance.setTension(""); // Ou null selon votre préférence
        }
        
        // CORRECTION: Gérer les champs optionnels
        if (surveillance.getSignesCliniques() != null) {
            surveillance.setSignesCliniques(capitalizeFirstLetter(surveillance.getSignesCliniques().trim()));
        } else {
            surveillance.setSignesCliniques("");
        }
        
        if (surveillance.getObservations() != null) {
            surveillance.setObservations(capitalizeFirstLetter(surveillance.getObservations().trim()));
        } else {
            surveillance.setObservations("");
        }
        
        // Validation des données numériques
        if (surveillance.getTemperature() != null) {
            if (surveillance.getTemperature() < 30.0 || surveillance.getTemperature() > 45.0) {
                throw new RuntimeException("La température doit être entre 30.0 et 45.0 °C");
            }
        }
        
        if (surveillance.getPouls() != null) {
            if (surveillance.getPouls() < 30 || surveillance.getPouls() > 200) {
                throw new RuntimeException("Le pouls doit être entre 30 et 200 battements par minute");
            }
        }
        
        System.out.println("- Surveillance validée, sauvegarde en cours...");
        
        Surveillance savedSurveillance = surveillanceRepository.save(surveillance);
        
        System.out.println("✅ Surveillance créée avec succès - ID: " + savedSurveillance.getId());
        
        return savedSurveillance;
    }

    @Override
    public List<Surveillance> getAllSurveillances() {
        return surveillanceRepository.findAll();
    }

    @Override
    public Optional<Surveillance> getSurveillanceById(Long id) {
        return surveillanceRepository.findById(id);
    }

    @Override
    public Surveillance updateSurveillance(Long id, Surveillance surveillanceDetails) {
        Surveillance surveillance = surveillanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Surveillance non trouvée avec l'id: " + id));
        
        // Mettre à jour les champs
        surveillance.setHeure(surveillanceDetails.getHeure());
        
        // CORRECTION: Gérer la tension
        if (surveillanceDetails.getTension() != null) {
            surveillance.setTension(surveillanceDetails.getTension().trim());
            // Validation du format si tension non vide
            if (!surveillance.getTension().isEmpty()) {
                if (!isValidTensionFormat(surveillance.getTension())) {
                    throw new RuntimeException("Le format de la tension est invalide. Format attendu: 'systolique/diastolique' (ex: 120/80)");
                }
            }
        }
        
        surveillance.setTemperature(surveillanceDetails.getTemperature());
        surveillance.setPouls(surveillanceDetails.getPouls());
        
        // CORRECTION: Gérer les champs optionnels
        if (surveillanceDetails.getSignesCliniques() != null) {
            surveillance.setSignesCliniques(capitalizeFirstLetter(surveillanceDetails.getSignesCliniques().trim()));
        }
        
        if (surveillanceDetails.getObservations() != null) {
            surveillance.setObservations(capitalizeFirstLetter(surveillanceDetails.getObservations().trim()));
        }
        
        // Validation des données
        if (surveillance.getTemperature() != null) {
            if (surveillance.getTemperature() < 30.0 || surveillance.getTemperature() > 45.0) {
                throw new RuntimeException("La température doit être entre 30.0 et 45.0 °C");
            }
        }
        
        if (surveillance.getPouls() != null) {
            if (surveillance.getPouls() < 30 || surveillance.getPouls() > 200) {
                throw new RuntimeException("Le pouls doit être entre 30 et 200 battements par minute");
            }
        }
        
        // Mettre à jour la relation si nécessaire
        if (surveillanceDetails.getTransfusion() != null && surveillanceDetails.getTransfusion().getId() != null) {
            Transfusion transfusion = transfusionRepository.findById(surveillanceDetails.getTransfusion().getId())
                    .orElseThrow(() -> new RuntimeException("Transfusion non trouvée"));
            surveillance.setTransfusion(transfusion);
        }
        
        return surveillanceRepository.save(surveillance);
    }

    @Override
    public void deleteSurveillance(Long id) {
        Surveillance surveillance = surveillanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Surveillance non trouvée avec l'id: " + id));
        
        surveillanceRepository.delete(surveillance);
    }

    @Override
    public List<Surveillance> getSurveillancesByTransfusion(Long transfusionId) {
        // CORRECTION: Utiliser la méthode du repository si elle existe
        // Ou améliorer la méthode actuelle
        return surveillanceRepository.findAll().stream()
                .filter(surveillance -> 
                    surveillance.getTransfusion() != null && 
                    surveillance.getTransfusion().getId() != null &&
                    surveillance.getTransfusion().getId().equals(transfusionId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Surveillance> getSurveillancesByHeure(LocalTime heure) {
        return surveillanceRepository.findAll().stream()
                .filter(surveillance -> surveillance.getHeure() != null && surveillance.getHeure().equals(heure))
                .collect(Collectors.toList());
    }

    @Override
    public List<Surveillance> getSurveillancesByHeureRange(LocalTime startHeure, LocalTime endHeure) {
        return surveillanceRepository.findAll().stream()
                .filter(surveillance -> 
                    surveillance.getHeure() != null && 
                    !surveillance.getHeure().isBefore(startHeure) && 
                    !surveillance.getHeure().isAfter(endHeure))
                .collect(Collectors.toList());
    }

    @Override
    public List<Surveillance> getSurveillancesByTemperatureRange(Double minTemperature, Double maxTemperature) {
        return surveillanceRepository.findAll().stream()
                .filter(surveillance -> 
                    surveillance.getTemperature() != null && 
                    surveillance.getTemperature() >= minTemperature && 
                    surveillance.getTemperature() <= maxTemperature)
                .collect(Collectors.toList());
    }

    @Override
    public List<Surveillance> getSurveillancesByPoulsRange(Integer minPouls, Integer maxPouls) {
        return surveillanceRepository.findAll().stream()
                .filter(surveillance -> 
                    surveillance.getPouls() != null && 
                    surveillance.getPouls() >= minPouls && 
                    surveillance.getPouls() <= maxPouls)
                .collect(Collectors.toList());
    }

    @Override
    public List<Surveillance> getSurveillancesBySignesCliniquesContaining(String keyword) {
        return surveillanceRepository.findAll().stream()
                .filter(surveillance -> 
                    surveillance.getSignesCliniques() != null && 
                    surveillance.getSignesCliniques().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public long countSurveillancesByTransfusion(Long transfusionId) {
        return surveillanceRepository.findAll().stream()
                .filter(surveillance -> 
                    surveillance.getTransfusion() != null && 
                    surveillance.getTransfusion().getId() != null &&
                    surveillance.getTransfusion().getId().equals(transfusionId))
                .count();
    }
    
    // Méthode utilitaire pour capitaliser la première lettre
    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
    
    // Méthode utilitaire pour valider le format de la tension - CORRIGÉE
    private boolean isValidTensionFormat(String tension) {
        if (tension == null || tension.isEmpty() || tension.trim().isEmpty()) {
            return true; // Accepte les valeurs vides
        }
        
        // Format attendu: "120/80"
        String[] parts = tension.split("/");
        if (parts.length != 2) {
            return false;
        }
        
        try {
            int systolique = Integer.parseInt(parts[0].trim());
            int diastolique = Integer.parseInt(parts[1].trim());
            
            // Valeurs raisonnables pour la tension artérielle
            return systolique >= 60 && systolique <= 250 && 
                   diastolique >= 40 && diastolique <= 150;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    // NOUVELLE MÉTHODE: Pour créer une surveillance à partir de données simples
    public Surveillance creerSurveillanceSimple(Long transfusionId, String heure, String tension, 
                                                Double temperature, Integer pouls, 
                                                String signesCliniques, String observations) {
        System.out.println("🚀 Création surveillance simple:");
        System.out.println("- Transfusion ID: " + transfusionId);
        System.out.println("- Heure: " + heure);
        System.out.println("- Tension: " + tension);
        
        Surveillance surveillance = new Surveillance();
        
        // Associer la transfusion
        Transfusion transfusion = transfusionRepository.findById(transfusionId)
                .orElseThrow(() -> new RuntimeException("Transfusion non trouvée avec l'id: " + transfusionId));
        surveillance.setTransfusion(transfusion);
        
        // Définir l'heure
        if (heure != null && !heure.isEmpty()) {
            try {
                surveillance.setHeure(LocalTime.parse(heure));
            } catch (Exception e) {
                throw new RuntimeException("Format d'heure invalide: " + heure);
            }
        } else {
            surveillance.setHeure(LocalTime.now());
        }
        
        // Définir les autres champs
        surveillance.setTension(tension != null ? tension.trim() : "");
        surveillance.setTemperature(temperature);
        surveillance.setPouls(pouls);
        surveillance.setSignesCliniques(signesCliniques != null ? signesCliniques.trim() : "");
        surveillance.setObservations(observations != null ? observations.trim() : "");
        
        return creerSurveillance(surveillance);
    }
}