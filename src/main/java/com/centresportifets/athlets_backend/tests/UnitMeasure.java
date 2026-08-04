package com.centresportifets.athlets_backend.tests;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "Unite_Mesure")
public class UnitMeasure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_unite")
    private Long id;

    @Column(name = "nom", nullable = false, unique = true)
    private String name;

    @Column(name = "symbole", nullable = false, unique = true)
    private String symbol;
}