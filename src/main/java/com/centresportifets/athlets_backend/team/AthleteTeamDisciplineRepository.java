package com.centresportifets.athlets_backend.team;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface AthleteTeamDisciplineRepository extends JpaRepository<AthleteTeamDiscipline, Long> {
    @Transactional
    @Modifying
    @Query("DELETE FROM AthleteTeamDiscipline atd WHERE atd.athlete.id = :athleteId AND atd.id NOT IN :athleteTeamDisciplineIds")
    void deleteByAthlete_IdAndIdNotIn(@Param("athleteId") Long athleteId, @Param("athleteTeamDisciplineIds") List<Long> athleteTeamDisciplineIds);

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO athlete_team_discipline (athlete_id, discipline_id, team_id) VALUES (:athleteId, :disciplineId, :teamId) ON CONFLICT (athlete_id, discipline_id, team_id) DO NOTHING", nativeQuery = true)
    void createIfNotExists(@Param("athleteId") Long athleteId, @Param("disciplineId") Long disciplineId, @Param("teamId") Long teamId);

    AthleteTeamDiscipline findByAthlete_IdAndDiscipline_IdAndTeam_Id(Long athleteId, Long disciplineId, Long teamId);
    List<AthleteTeamDiscipline> findByAthlete_IdAndTeam_Id(Long athleteId, Long teamId);
}