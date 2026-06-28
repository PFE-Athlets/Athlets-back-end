package com.centresportifets.athlets_backend.auth.userTypes;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CoachRepository extends JpaRepository<Coach, Long> {
    Optional<Coach> findByUsername(String username);
}