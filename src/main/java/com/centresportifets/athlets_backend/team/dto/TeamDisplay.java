package com.centresportifets.athlets_backend.team.dto;

import com.centresportifets.athlets_backend.team.Team;

import lombok.Data;

@Data
public class TeamDisplay {
    private Team team;
    private int numberOfAthletes;
    private String headCoachName;
    private Long headCoachId;
}