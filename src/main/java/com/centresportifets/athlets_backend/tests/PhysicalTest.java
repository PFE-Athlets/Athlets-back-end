package com.centresportifets.athlets_backend.tests;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.centresportifets.athlets_backend.tests.equipment.Equipment;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Table(name = "Tests")
@Entity
public class PhysicalTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_test")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_qualite_physique", nullable = false)
    private PhysicalQuality physicalQuality;

    @Column(name = "nom_test", nullable = false, length = 100, unique = true)
    private String name;

    @Column(name = "protocole", nullable = false)
    private String protocol;

    @Column(name = "supervise", nullable = false)
    private boolean supervised = false;

    @Column(name = "informations")
    private String informations;

    @Column(name = "preuve_requise", nullable = false)
    private boolean proofRequired = false;

    @ManyToMany
    @JoinTable(
        name = "Test_Equipement",
        joinColumns = @JoinColumn(name = "id_test"),
        inverseJoinColumns = @JoinColumn(name = "id_equipement")
    )
    private Set<Equipment> equipements = new HashSet<>();

    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ResultType> typesResultat = new ArrayList<>();
}