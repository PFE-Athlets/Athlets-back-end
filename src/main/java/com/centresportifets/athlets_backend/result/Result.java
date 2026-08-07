package com.centresportifets.athlets_backend.result;

import com.centresportifets.athlets_backend.tests.PhysicalTest;
import com.centresportifets.athlets_backend.user.athlete.Athlete;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Table(name = "Result")
@Entity
public class Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_result")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_test", nullable = false)
    private PhysicalTest test;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_athlete", nullable = false)
    private Athlete athlete;

    @Column(name = "proof", length = 500)
    private String proof;

    @Column(name = "status", nullable = false, length = 20)
    private String status = ResultStatus.ASSIGNED.getStatus();

    @Column(name = "comment", columnDefinition = "TEXT")
    private String commentText;

    @Column(name = "date_result", nullable = false)
    private LocalDate testDate = LocalDate.now();

    @ToString.Exclude
    @OneToMany(mappedBy = "result", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResultValue> resultValues = new ArrayList<>();
}