package com.fourm.token.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String specialization;
    private Long organizationId;
    private Double consultationFee;
    private Boolean isAvailable = true;
    private Integer maxTokensPerDay;
}