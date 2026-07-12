package com.centresportifets.athlets_backend.team.dto;

import java.util.List;

import lombok.Data;

@Data
public class TeamModificationRequest {
    Long teamId;
    String newTeamName;
    Long newCoachId;
    List<Long> newSubcoachesIds;
}
