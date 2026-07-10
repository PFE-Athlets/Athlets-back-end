package com.centresportifets.athlets_backend.team;

import com.centresportifets.athlets_backend.user.athlete.Athlete;
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
    @ManyToOne
    @MapsId("athleteId")
    @JoinColumn(name = "athlete_id")
    @JsonIgnore
    private Athlete athlete;

    @ToString.Exclude
    @ManyToOne
    @MapsId("teamId")
    @JoinColumn(name = "team_id")
    @JsonIgnore
    private Team team;
}