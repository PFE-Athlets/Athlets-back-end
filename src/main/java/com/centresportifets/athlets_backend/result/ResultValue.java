package com.centresportifets.athlets_backend.result;

import java.math.BigDecimal;

import com.centresportifets.athlets_backend.tests.ResultType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "Result_Value",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_value_result",
            columnNames = {"id_result", "id_result_type"}
        )
    }
)
public class ResultValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_result_value")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_result", nullable = false)
    private Result result;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_result_type", nullable = false)
    private ResultType resultType;

    @Column(name = "value", nullable = false, precision = 10, scale = 2)
    private BigDecimal value;
}