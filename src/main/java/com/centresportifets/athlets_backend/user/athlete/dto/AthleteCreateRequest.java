package com.centresportifets.athlets_backend.user.athlete.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class AthleteCreateRequest {
    @Schema(defaultValue = "John")
    private String firstName;
    @Schema(defaultValue = "Doe")
    private String lastName;
    @Schema(defaultValue = "john.doe@example.com")
    private String email;
    @Schema(defaultValue = "+21612345678")
    private String phone;
    @Schema(defaultValue = "jdoe")
    private String username;
    @Schema(defaultValue = "Active")
    private String accountStatus;

    @Schema(defaultValue = "1998-05-12")
    private LocalDate birthDate;
    @Schema(defaultValue = "Male")
    private String gender;
    @Schema(defaultValue = "180")
    private int heightMeters;
    @Schema(defaultValue = "70.5")
    private BigDecimal weightKg;
    @Schema(defaultValue = "Right")
    private String dominantArm;
    @Schema(defaultValue = "Right")
    private String dominantLeg;
    @Schema(defaultValue = "No major injuries")
    private String injuryHistory;

    private List<TeamInfoData> teamsInfo;
}
