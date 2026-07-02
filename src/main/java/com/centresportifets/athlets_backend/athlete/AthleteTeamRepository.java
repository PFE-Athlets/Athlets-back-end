package com.centresportifets.athlets_backend.athlete;

import org.springframework.data.jpa.repository.JpaRepository;

import com.centresportifets.athlets_backend.sport.AthleteTeam;
import com.centresportifets.athlets_backend.sport.AthleteTeamId;

public interface AthleteTeamRepository extends JpaRepository<AthleteTeam, AthleteTeamId> {
	
}
