package com.centresportifets.athlets_backend.physicalTest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Data
@Entity
@Table(
    name = "Type_Resultat",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_type_resultat_test_nom", columnNames = {"id_test", "nom"})
    }
)
public class ResultType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_type_resultat")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_test", nullable = false)
    private PhysicalTest test;

    @ManyToOne
    @JoinColumn(name = "id_unite_mesure", nullable = false)
    private UnitMeasure unitMeasure;

    @Column(name = "nom", nullable = false, length = 200)
    private String name;

    @Column(name = "type_donnee", nullable = false, length = 20)
    private String dataType = "DECIMAL";
}