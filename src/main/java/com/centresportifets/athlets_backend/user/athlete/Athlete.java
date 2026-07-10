package com.centresportifets.athlets_backend.user.athlete;

import com.centresportifets.athlets_backend.team.AthleteTeam;
import com.centresportifets.athlets_backend.user.UserAccount;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "athlete")
@PrimaryKeyJoinColumn(name = "user_id")
public class Athlete extends UserAccount {

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false, length = 10)
    private String gender;

    @Column(name = "height_meters")
    private Integer heightMeters;

    @Column(name = "weight_kg", precision = 4, scale = 1)
    private BigDecimal weightKg;

    @Column(name = "dominant_arm")
    private String dominantArm;

    @Column(name = "dominant_leg")
    private String dominantLeg;

    @Column(name = "injury_history")
    private String injuryHistory;

    @OneToMany(mappedBy = "athlete", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AthleteTeam> athleteTeams = new ArrayList<>();

    public Athlete() {
        this.setAccessLevel(3);
    }
}