package com.centresportifets.athlets_backend.team.dto;

import java.util.List;

import lombok.Data;

@Data
public class TeamCreationRequest {
    String teamName;
    Long sportId;
    Long headCoachId;
    List<Long> subcoachIds;
}
