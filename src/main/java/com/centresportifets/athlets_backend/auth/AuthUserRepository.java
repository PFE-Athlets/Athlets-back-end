package com.centresportifets.athlets_backend.auth;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface AuthUserRepository extends JpaRepository<AuthUser, Long> {

    /**
     * Finds a user by their unique name string.
     * Essential for verifying credentials during the login flow.
     * 
     * @param name the username to look up
     * @return an Optional containing the AuthUser if found, or empty if not
     */
    Optional<AuthUser> findByName(String name);

    /**
     * Quickly checks if a username is already taken.
     * Perfect for validation logic during user registration.
     * 
     * @param name the username to check
     * @return true if the name exists in the database, false otherwise
     */
    boolean existsByName(String name);
}