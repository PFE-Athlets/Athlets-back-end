package com.centresportifets.athlets_backend.team.dto;

import com.centresportifets.athlets_backend.sport.dto.DisciplinesAndPositions;

import lombok.Data;

@Data
public class AthletePreviewDisplay {
    Long athleteId;
    String athleteName;
    DisciplinesAndPositions disciplinesAndPositions;
}
