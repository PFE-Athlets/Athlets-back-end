package com.centresportifets.athlets_backend.auth.userTypes;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AthleteRepository extends JpaRepository<Athlete, Long> {
	List<Athlete> findAllByUsernameIn(List<String> usernames);
}
