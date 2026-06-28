package com.centresportifets.athlets_backend.sport;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class AthleteTeamId implements Serializable {
    private Long athleteId;
    private Long teamId;
}