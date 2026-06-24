package com.centresportifets.athlets_backend.auth.userTypes;

import com.centresportifets.athlets_backend.auth.UserAccount;
import com.centresportifets.athlets_backend.sport.Sport;
import com.centresportifets.athlets_backend.sport.Team;
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
    @JoinColumn(name = "sport_id", nullable = false)
    private Sport sport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    public Coach() {
        this.setAccessLevel(2);
    }
}