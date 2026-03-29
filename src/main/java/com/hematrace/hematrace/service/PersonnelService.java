package com.hematrace.hematrace.service;

import com.hematrace.hematrace.entite.Personnel;
import java.util.List;
import java.util.Optional;

public interface PersonnelService {
    Personnel creerPersonnel(Personnel personnel);
    List<Personnel> getAllPersonnel();
    Optional<Personnel> getPersonnelById(Long id);
    Personnel getPersonnelByMatricule(String matricule);
    Personnel getPersonnelByEmail(String email);
    Personnel updatePersonnel(Long id, Personnel personnelDetails);
    void updateFonction(Long id, String nouvelleFonction);
    void deletePersonnel(Long id);
    List<Personnel> getPersonnelByFonction(String fonction);
    long countDemandesByPersonnel(Long personnelId);
}