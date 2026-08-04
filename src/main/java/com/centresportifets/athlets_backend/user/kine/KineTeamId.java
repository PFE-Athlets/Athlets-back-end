package com.centresportifets.athlets_backend.user.kine;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class KineTeamId implements Serializable {
    private Long kineId;
    private Long teamId;
}