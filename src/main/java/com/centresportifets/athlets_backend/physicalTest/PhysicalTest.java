package com.centresportifets.athlets_backend.physicalTest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Table(name = "tests")
@Entity
public class PhysicalTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_test")
    private Long idTest;

    @Column(name = "nom_test", nullable = false, length = 20)
    private String nomTest;

    @Column(name = "unite_mesure", nullable = false, length = 10)
    private String uniteMesure;

    @Lob
    @Column(name = "protocole")
    private String protocole;
}