package com.hematrace.hematrace.service;

import com.hematrace.hematrace.entite.ChefService;
import java.util.List;
import java.util.Optional;

public interface ChefServiceService {
    ChefService creerChefService(ChefService chefService);
    List<ChefService> getAllChefsService();
    Optional<ChefService> getChefServiceById(Long id);
    ChefService getChefServiceByMatricule(String matricule);
    ChefService getChefServiceByEmail(String email);
    ChefService updateChefService(Long id, ChefService chefServiceDetails);
    void updateServiceDirige(Long id, String nouveauService);
    void updateDepartement(Long id, String nouveauDepartement);
    void deleteChefService(Long id);
    List<ChefService> getChefsServiceByService(String service);
    List<ChefService> getChefsServiceByDepartement(String departement);
}