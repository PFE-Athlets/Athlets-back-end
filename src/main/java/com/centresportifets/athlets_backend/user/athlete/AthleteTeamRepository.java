package com.centresportifets.athlets_backend.user.athlete;

import org.springframework.data.jpa.repository.JpaRepository;

import com.centresportifets.athlets_backend.team.AthleteTeam;
import com.centresportifets.athlets_backend.team.AthleteTeamId;

public interface AthleteTeamRepository extends JpaRepository<AthleteTeam, AthleteTeamId> {
	
}
