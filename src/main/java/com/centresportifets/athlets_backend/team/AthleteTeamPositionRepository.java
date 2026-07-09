package com.centresportifets.athlets_backend.team;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AthleteTeamPositionRepository extends JpaRepository<AthleteTeamPosition, Long> {
    void deleteByPositionIdNotInAthleteIdAndTeamIdNotIn(List<Long> positionId, Long athleteId, List<Long> teamId);
}