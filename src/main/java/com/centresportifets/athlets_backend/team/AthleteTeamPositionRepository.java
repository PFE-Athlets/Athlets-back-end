package com.centresportifets.athlets_backend.team;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface AthleteTeamPositionRepository extends JpaRepository<AthleteTeamPosition, Long> {
    @Transactional
    void deleteByAthlete_Id(Long athleteId);
}