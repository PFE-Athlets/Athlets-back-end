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
@Table(name = "test")
@Entity
public class PhysicalTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @Column(name = "unit_of_mesure", nullable = false, length = 10)
    private String unit;

    @Lob
    @Column(name = "protocol")
    private String protocol;

    @Column(name = "proof_needed")
    private String proof;

    @ManyToMany
    @JoinTable(
        name = "Test_Sport",
        joinColumns = @JoinColumn(name = "test_id"),
        inverseJoinColumns = @JoinColumn(name = "sport_id")
    )
    private Set<Sport> sports = new HashSet<>();
}