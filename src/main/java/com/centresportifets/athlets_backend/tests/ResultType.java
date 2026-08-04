package com.centresportifets.athlets_backend.tests;

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
    name = "Result_Type",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_result_type_test_name", columnNames = {"id_test", "name"})
    }
)
public class ResultType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_result_type")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_test", nullable = false)
    private PhysicalTest test;

    @ManyToOne
    @JoinColumn(name = "id_unit_measure", nullable = false)
    private UnitMeasure unitMeasure;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "data_type", nullable = false, length = 20)
    private String dataType = "DECIMAL";
}