package com.centresportifets.athlets_backend.auth;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface AuthRepository extends JpaRepository<UserAccount, Long> {

	Optional<UserAccount> findByUsername(String username);

	boolean existsByUsername(String username);
}
