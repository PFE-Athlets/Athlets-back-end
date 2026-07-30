package com.centresportifets.athlets_backend.team;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface AthleteTeamPositionRepository extends JpaRepository<AthleteTeamPosition, Long> {
    @Transactional
    void deleteByAthlete_IdAndPosition_IdNotInAndTeam_IdNotIn(Long athleteId, List<Long> positionIds, List<Long> teamIds);

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO Athlete_Team_Position (athlete_id, position_id, team_id) VALUES (:athleteId, :positionId, :teamId) ON CONFLICT (athlete_id, position_id, team_id) DO NOTHING", nativeQuery = true)
    void createIfNotExists(@Param("athleteId") Long athleteId, @Param("positionId") Long positionId, @Param("teamId") Long teamId);
}