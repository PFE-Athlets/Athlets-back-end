package com.centresportifets.athlets_backend.user.athlete;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.centresportifets.athlets_backend.user.UserAccount;

public interface AthleteRepository extends JpaRepository<Athlete, Long> {
	Optional<Athlete> findByUsername(String username);


	List<Athlete> findAllByUsernameIn(List<String> usernames);

	List<Athlete> findByAthleteTeamsTeamId(Long teamId);
}
