package com.centresportifets.athlets_backend.athlete;

import org.springframework.data.jpa.repository.JpaRepository;

import com.centresportifets.athlets_backend.sport.AthleteTeam;

public interface AthleteTeamRepository extends JpaRepository<AthleteTeam, Long> {
	
}
