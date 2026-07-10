package com.centresportifets.athlets_backend.user.athlete.dto;

import com.centresportifets.athlets_backend.auth.dto.AuthUser;
import com.centresportifets.athlets_backend.user.athlete.Athlete;
import com.centresportifets.athlets_backend.sport.discipline.Discipline;
import com.centresportifets.athlets_backend.sport.position.Position;
import com.centresportifets.athlets_backend.team.Team;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
    private List<Position> positions;
    private List<Discipline> disciplines;
    private List<Team> teams;

    public AthleteData(Athlete athlete) {
        this.authUser = new AuthUser(athlete); 
        this.birthDate = athlete.getBirthDate();
        this.gender = athlete.getGender();
        this.heightMeters = athlete.getHeightMeters();
        this.weightKg = athlete.getWeightKg();
        this.dominantArm = athlete.getDominantArm();
        this.dominantLeg = athlete.getDominantLeg();
        this.injuryHistory = athlete.getInjuryHistory();
        this.positions = athlete.getAthleteTeamPositions().stream().map((pos) -> pos.getPosition()).toList();
        this.disciplines = athlete.getAthleteTeamDisciplines().stream().map((dis) -> dis.getDiscipline()).toList();
        this.teams = athlete.getAthleteTeams().stream().map((team) -> team.getTeam()).toList();
    }
}