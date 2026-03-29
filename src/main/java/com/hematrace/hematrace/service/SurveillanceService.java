package com.hematrace.hematrace.service;

import com.hematrace.hematrace.entite.Surveillance;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface SurveillanceService {
    Surveillance creerSurveillance(Surveillance surveillance);
    List<Surveillance> getAllSurveillances();
    Optional<Surveillance> getSurveillanceById(Long id);
    Surveillance updateSurveillance(Long id, Surveillance surveillanceDetails);
    void deleteSurveillance(Long id);
    List<Surveillance> getSurveillancesByTransfusion(Long transfusionId);
    List<Surveillance> getSurveillancesByHeure(LocalTime heure);
    List<Surveillance> getSurveillancesByHeureRange(LocalTime startHeure, LocalTime endHeure);
    List<Surveillance> getSurveillancesByTemperatureRange(Double minTemperature, Double maxTemperature);
    List<Surveillance> getSurveillancesByPoulsRange(Integer minPouls, Integer maxPouls);
    List<Surveillance> getSurveillancesBySignesCliniquesContaining(String keyword);
    long countSurveillancesByTransfusion(Long transfusionId);
}