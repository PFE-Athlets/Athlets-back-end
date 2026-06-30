package com.centresportifets.athlets_backend.sport;

import com.centresportifets.athlets_backend.auth.userTypes.Athlete;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@Entity
@Table(name = "Athlete_Team")
public class AthleteTeam {

    @EmbeddedId
    private AthleteTeamId id = new AthleteTeamId();

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("athleteId")
    @JoinColumn(name = "athlete_id")
    @JsonIgnore
    private Athlete athlete;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("teamId")
    @JoinColumn(name = "team_id")
    @JsonIgnore
    private Team team;

    @Column(name = "position_id")
    private Integer positionId;

    @Column(name = "discipline_id")
    private Integer disciplineId;
}