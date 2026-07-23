package com.centresportifets.athlets_backend.user.kine;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface KineTeamRepository extends JpaRepository<KineTeam, KineTeamId> {
    List<KineTeam> findByTeamId(Long teamId);
    List<KineTeam> findByKineId(Long kineId);
    boolean existsByKineIdAndTeamId(Long kineId, Long teamId);
    void deleteByKineIdAndTeamId(Long kineId, Long teamId);
}