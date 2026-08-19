package com.fourm.token.dto;

import lombok.Data;

@Data
public class TokenRegisterRequest {
    private String name;
    private Integer age;
    private String gender;
    private String phone;
    private String bp;
    private Double weight;
    private Long doctorId;
    private String reasonForVisit;
    private Boolean isEmergency;
    private String email;
}