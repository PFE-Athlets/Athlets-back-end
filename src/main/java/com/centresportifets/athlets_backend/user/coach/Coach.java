package com.centresportifets.athlets_backend.user.coach;

import com.centresportifets.athlets_backend.sport.Sport;
import com.centresportifets.athlets_backend.team.Team;
import com.centresportifets.athlets_backend.user.UserAccount;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "coach")
@PrimaryKeyJoinColumn(name = "user_id")
public class Coach extends UserAccount {

    @Column(length = 50)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id", nullable = true)
    private Sport sport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = true)
    private Team team;

    @Column(name = "is_head_coach", nullable = true)
    private boolean isHeadCoach;

    public Coach() {
        this.setAccessLevel(2);
    }
}