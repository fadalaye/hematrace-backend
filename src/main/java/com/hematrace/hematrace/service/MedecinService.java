package com.hematrace.hematrace.service;

import com.hematrace.hematrace.entite.Medecin;
import java.util.List;
import java.util.Optional;

public interface MedecinService {
    Medecin creerMedecin(Medecin medecin);
    List<Medecin> getAllMedecins();
    Optional<Medecin> getMedecinById(Long id);
    Medecin getMedecinByMatricule(String matricule);
    Medecin getMedecinByEmail(String email);
    Medecin updateMedecin(Long id, Medecin medecinDetails);
    void updateSpecialite(Long id, String nouvelleSpecialite);
    void deleteMedecin(Long id);
    List<Medecin> getMedecinsBySpecialite(String specialite);
    long countDemandesByMedecin(Long medecinId);
    long countTransfusionsByMedecin(Long medecinId);
}