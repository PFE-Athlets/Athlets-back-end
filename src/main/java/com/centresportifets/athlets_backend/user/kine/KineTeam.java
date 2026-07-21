package com.centresportifets.athlets_backend.user.kine;

import com.centresportifets.athlets_backend.team.Team;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@Entity
@Table(name = "Kine_Team")
public class KineTeam {

    @EmbeddedId
    private KineTeamId id = new KineTeamId();

    @ToString.Exclude
    @ManyToOne
    @MapsId("kineId")
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private Kine kine;

    @ToString.Exclude
    @ManyToOne
    @MapsId("teamId")
    @JoinColumn(name = "team_id")
    @JsonIgnore
    private Team team;
}