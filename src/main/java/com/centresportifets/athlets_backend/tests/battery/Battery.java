package com.centresportifets.athlets_backend.tests.battery;

import java.util.ArrayList;
import java.util.List;

import com.centresportifets.athlets_backend.tests.PhysicalTest;
import com.centresportifets.athlets_backend.team.Team;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Data
@Entity
@Table(
    name = "Batterie",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_batterie_equipe_nom", columnNames = {"id_equipe", "nom_batterie"})
    }
)
public class Battery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_batterie")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_equipe", nullable = false)
    private Team team;

    @Column(name = "nom_batterie", nullable = false, length = 100)
    private String name;

    @Column(name = "statut", nullable = false)
    private boolean status = true;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "Batterie_Test",
        joinColumns = @JoinColumn(name = "id_batterie"),
        inverseJoinColumns = @JoinColumn(name = "id_test")
    )
    private List<PhysicalTest> tests = new ArrayList<>();
}