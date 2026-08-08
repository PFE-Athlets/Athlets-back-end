package com.centresportifets.athlets_backend.team;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface AthleteTeamPositionRepository extends JpaRepository<AthleteTeamPosition, Long> {
    @Transactional
    @Modifying
    @Query("DELETE FROM AthleteTeamPosition atp WHERE atp.athlete.id = :athleteId AND atp.id NOT IN :athleteTeamPositionIds")
    void deleteByAthlete_IdAndIdNotIn(@Param("athleteId") Long athleteId, @Param("athleteTeamPositionIds") List<Long> athleteTeamPositionIds);
    
    @Transactional
    @Modifying
    @Query(value = "INSERT INTO AthleteTeamPosition (athlete_id, position_id, team_id) VALUES (:athleteId, :positionId, :teamId) ON CONFLICT (athlete_id, position_id, team_id) DO NOTHING", nativeQuery = true)
    void createIfNotExists(@Param("athleteId") Long athleteId, @Param("positionId") Long positionId, @Param("teamId") Long teamId);
    
    AthleteTeamPosition findByAthlete_IdAndPosition_IdAndTeam_Id(Long athleteId, Long positionId, Long teamId);
    List<AthleteTeamPosition> findByAthlete_IdAndTeam_Id(Long athleteId, Long teamId);
}