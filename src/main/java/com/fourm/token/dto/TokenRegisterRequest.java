package com.fourm.token.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class TokenRegisterRequest {
    @NotBlank(message = "Patient name is required")
    private String name;
    private Integer age;
    private String gender;
    @NotBlank(message = "Phone number is required")
    private String phone;
    private String bp;
    private Double weight;
    private Long doctorId;
    private String reasonForVisit;
    private Boolean isEmergency;
    private String email;
}