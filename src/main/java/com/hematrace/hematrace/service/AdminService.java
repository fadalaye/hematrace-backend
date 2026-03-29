package com.hematrace.hematrace.service;

import com.hematrace.hematrace.entite.Admin;
import java.util.List;
import java.util.Optional;

public interface AdminService {
    Admin creerAdmin(Admin admin);
    List<Admin> getAllAdmins();
    Optional<Admin> getAdminById(Long id);
    Admin getAdminByMatricule(String matricule);
    Admin getAdminByEmail(String email);
    Admin updateAdmin(Long id, Admin adminDetails);
    void updateRoleAdmin(Long id, String nouveauRole);
    void updateDroitsAccessAdmin(Long id, String nouveauxDroits);
    void deleteAdmin(Long id);
    List<Admin> getAdminsByRole(String role);
}