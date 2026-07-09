package com.centresportifets.athlets_backend.team;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface AthleteTeamDisciplineRepository extends JpaRepository<AthleteTeamDiscipline, Long> {

    @Transactional
    void deleteByDiscipline_IdNotInAndAthlete_IdAndTeam_IdNotIn(
        List<Long> disciplineIds, 
        Long athleteId, 
        List<Long> teamIds
    );
}