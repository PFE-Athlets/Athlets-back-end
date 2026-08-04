package com.centresportifets.athlets_backend.user.athlete.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TeamInfoData {
    @Schema(defaultValue = "2")
    Long teamId;

    @Schema(defaultValue = "1", nullable = true)
    Long disciplineId;

    @Schema(defaultValue = "", nullable = true)
    Long positionId;
}
