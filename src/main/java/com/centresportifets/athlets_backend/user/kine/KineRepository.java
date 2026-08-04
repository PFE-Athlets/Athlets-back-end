package com.centresportifets.athlets_backend.user.kine;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface KineRepository extends JpaRepository<Kine, Long> {
    Optional<Kine> findByUsername(String username);
}