package com.centresportifets.athlets_backend.auth;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface AuthUserRepository extends JpaRepository<AuthUser, Long> {

	Optional<AuthUser> findByName(String name);

	boolean existsByName(String name);
}
