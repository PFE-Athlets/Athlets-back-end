package com.centresportifets.athlets_backend.team;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface AthleteTeamPositionRepository extends JpaRepository<AthleteTeamPosition, Long> {

    @Transactional
    void deleteByPosition_IdNotInAndAthlete_IdAndTeam_IdNotIn(
        List<Long> positionIds, 
        Long athleteId, 
        List<Long> teamIds
    );
}