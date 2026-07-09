package com.centresportifets.athlets_backend.team;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AthleteTeamDisciplineRepository extends JpaRepository<AthleteTeamDiscipline, Long> {
    void deleteByDisciplineIdNotInAthleteIdAndTeamIdNotIn(List<Long> disciplineId, Long athleteId, List<Long> teamId);

}