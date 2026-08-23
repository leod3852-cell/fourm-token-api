package com.fourm.token.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Integer age;
    private String gender;
    private String phone;
    private String bp;
    private Double weight;
    private String reasonForVisit;
    private Boolean isEmergency = false;
    private String email;
    private Long organizationId;
}