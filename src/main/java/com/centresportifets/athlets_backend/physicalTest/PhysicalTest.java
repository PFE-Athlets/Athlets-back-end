package com.centresportifets.athlets_backend.physicalTest;

import java.util.HashSet;
import java.util.Set;

import com.centresportifets.athlets_backend.sport.Sport;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Table(name = "tests")
@Entity
public class PhysicalTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_test")
    private Long id;

    @Column(name = "nom_test", nullable = false, length = 20)
    private String name;

    @Column(name = "unite_mesure", nullable = false, length = 10)
    private String unit;

    @Lob
    @Column(name = "protocole")
    private String protocole;

    @Column(name = "proof")
    private String proof;

    @ManyToMany
    @JoinTable(
        name = "Test_Sport",
        joinColumns = @JoinColumn(name = "id_test"),
        inverseJoinColumns = @JoinColumn(name = "id_sport")
    )
    private Set<Sport> sports = new HashSet<>();
}