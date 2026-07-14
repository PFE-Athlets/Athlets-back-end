package com.centresportifets.athlets_backend.sport.discipline;

import com.centresportifets.athlets_backend.sport.Sport;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "discipline")
public class Discipline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @ManyToOne
    @JoinColumn(name = "sport_id", nullable = false, foreignKey = @ForeignKey(name = "fk_discipline_sport"))
    private Sport sport;
}