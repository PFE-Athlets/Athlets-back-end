package com.centresportifets.athlets_backend.user.athlete.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class AthleteUpdateRequest {
    private String phone;

    private BigDecimal weightKg;

    private String injuryHistory;

    private List<Long> teamIds;

    private List<Long> positionIds;

    private List<Long> disciplineIds;
}
