package com.fourm.token.dto;

import lombok.Data;

@Data
public class PatientUpdateRequest {
    private String bp;
    private Double weight;
    private String reasonForVisit;
    private String email;
    private Boolean isEmergency;
}