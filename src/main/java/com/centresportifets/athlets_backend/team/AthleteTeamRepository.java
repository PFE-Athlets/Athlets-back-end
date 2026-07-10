package com.centresportifets.athlets_backend.team;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AthleteTeamRepository extends JpaRepository<AthleteTeam, AthleteTeamId> {
    int countByTeamId(Long teamId);
}