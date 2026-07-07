package com.centresportifets.athlets_backend.user.athlete.dto;

import com.centresportifets.athlets_backend.auth.dto.AuthUser;
import com.centresportifets.athlets_backend.user.athlete.Athlete;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AthleteData {
    
    private AuthUser authUser;
    private LocalDate birthDate;
    private String gender;
    private Integer heightMeters;
    private BigDecimal weightKg;
    private String dominantArm;
    private String dominantLeg;
    private String injuryHistory;

    public AthleteData(Athlete athlete) {
        this.authUser = new AuthUser(athlete); 
        this.birthDate = athlete.getBirthDate();
        this.gender = athlete.getGender();
        this.heightMeters = athlete.getHeightMeters();
        this.weightKg = athlete.getWeightKg();
        this.dominantArm = athlete.getDominantArm();
        this.dominantLeg = athlete.getDominantLeg();
        this.injuryHistory = athlete.getInjuryHistory();
    }
}