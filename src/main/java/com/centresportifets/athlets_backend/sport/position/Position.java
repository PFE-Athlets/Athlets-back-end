package com.centresportifets.athlets_backend.sport.position;

import com.centresportifets.athlets_backend.sport.Sport;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "position")
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @ManyToOne
    @JoinColumn(name = "sport_id", nullable = false, foreignKey = @ForeignKey(name = "fk_position_sport"))
    private Sport sport;
}