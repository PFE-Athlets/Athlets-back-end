package com.centresportifets.athlets_backend.sport;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "position", uniqueConstraints = {
    @UniqueConstraint(name = "uq_position_sport_name", columnNames = {"sport_id", "name"})
})
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id", nullable = false, foreignKey = @ForeignKey(name = "fk_position_sport"))
    private Sport sport;
}