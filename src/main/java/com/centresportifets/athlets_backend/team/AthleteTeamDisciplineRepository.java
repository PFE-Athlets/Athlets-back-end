package com.centresportifets.athlets_backend.team;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface AthleteTeamDisciplineRepository extends JpaRepository<AthleteTeamDiscipline, Long> {
    @Transactional
    void deleteByAthlete_IdAndDiscipline_IdNotInAndTeam_IdNotIn(Long athleteId, List<Long> disciplineIds, List<Long> teamIds);

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO Athlete_Team_Discipline (athlete_id, discipline_id, team_id) VALUES (:athleteId, :disciplineId, :teamId) ON CONFLICT (athlete_id, discipline_id, team_id) DO NOTHING", nativeQuery = true)
    void createIfNotExists(@Param("athleteId") Long athleteId, @Param("disciplineId") Long disciplineId, @Param("teamId") Long teamId);
}