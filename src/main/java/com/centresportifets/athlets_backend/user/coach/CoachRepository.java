package com.centresportifets.athlets_backend.user.coach;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CoachRepository extends JpaRepository<Coach, Long> {
    Optional<Coach> findByUsername(String username);
    Coach findByTeam_IdAndIsHeadCoachTrue(Long teamId);
    List<Coach> findByTeam_IdAndIsHeadCoachFalse(Long teamId);
}