package com.centresportifets.athlets_backend.sport;

import com.centresportifets.athlets_backend.auth.userTypes.Athlete;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "Athlete_Team")
public class AthleteTeam {

    @EmbeddedId
    private AthleteTeamId id = new AthleteTeamId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("athleteId")
    @JoinColumn(name = "athlete_id")
    private Athlete athlete;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("teamId")
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(name = "position_id")
    private Integer positionId; // Or link to a Position entity if you map it later

    @Column(name = "discipline_id")
    private Integer disciplineId; // Or link to a Discipline entity if you map it later
}