package com.hematrace.hematrace.dto;

import java.util.List;

import com.hematrace.hematrace.entite.Surveillance;

import lombok.Data;

@Data
public class CorrectionCliniqueTransfusionRequest {
    private String tolerance;
    private String etatPatientApres;
    private Boolean effetsIndesirables;
    private String typeEffet;
    private String graviteEffet;
    private String notes;
    private List<Surveillance> surveillances;

}