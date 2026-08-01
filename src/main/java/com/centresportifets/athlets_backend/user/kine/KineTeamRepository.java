package com.centresportifets.athlets_backend.user.kine;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface KineTeamRepository extends JpaRepository<KineTeam, KineTeamId> {
    List<KineTeam> findByTeamId(Long teamId);
    List<KineTeam> findByKineId(Long kineId);
    boolean existsByKineIdAndTeamId(Long kineId, Long teamId);

    @Transactional
    @Modifying
    void deleteByKineIdAndTeamId(Long kineId, Long teamId);
}