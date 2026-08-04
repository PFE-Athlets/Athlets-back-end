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
    name = "Valeur_Resultat",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_valeur_resultat",
            columnNames = {"id_resultat", "id_type_resultat"}
        )
    }
)
public class ResultValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_valeur_resultat")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_resultat", nullable = false)
    private Result result;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_type_resultat", nullable = false)
    private ResultType resultType;

    @Column(name = "valeur", nullable = false, precision = 10, scale = 2)
    private BigDecimal value;
}