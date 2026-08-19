package com.fourm.token.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class PermissionConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String actionKey;   // CALL_NEXT, COMPLETE, HOLD

    private Boolean allowAdmin = true;
    private Boolean allowDoctor = false;
}