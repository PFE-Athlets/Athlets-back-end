package com.centresportifets.athlets_backend.user.kine;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.centresportifets.athlets_backend.user.UserAccount;

public interface KineTeamRepository extends JpaRepository<KineTeam, KineTeamId> {
    @Query("SELECT DISTINCT kt.kine FROM KineTeam kt")
    List<UserAccount> findDistinctKines();
}