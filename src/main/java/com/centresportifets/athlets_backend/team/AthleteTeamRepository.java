package com.centresportifets.athlets_backend.team;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface AthleteTeamRepository extends JpaRepository<AthleteTeam, AthleteTeamId> {
    int countByTeamId(Long teamId);

    @Transactional
    void deleteByAthlete_IdAndTeamIdNotIn(Long athleteId, List<Long> teamIds);

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO Athlete_Team (athlete_id, team_id) VALUES (:athleteId, :teamId) ON CONFLICT (athlete_id, team_id) DO NOTHING", nativeQuery = true)
    void createIfNotExists(@Param("athleteId") Long athleteId, @Param("teamId") Long teamId);
}