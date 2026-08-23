package com.fourm.token.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;
}