package com.centresportifets.athlets_backend.result;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ResultRepository extends JpaRepository<Result, Long> { 
    List<Result> findByAthleteUsername(String username);

    List<Result> findAll();

    List<Result> findByAthleteIdIn(List<Long> athleteIds);

}