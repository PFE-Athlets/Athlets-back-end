package com.centresportifets.athlets_backend.result;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResultRepository extends JpaRepository<Result, Long> { 
    Page<Result> findByAthleteUsername(String username, Pageable pageable);

    Page<Result> findAll(Pageable pageable);

    Page<Result> findByAthleteIdIn(List<Long> athleteIds, Pageable pageable);

}