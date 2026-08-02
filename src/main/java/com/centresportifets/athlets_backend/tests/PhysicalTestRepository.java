package com.centresportifets.athlets_backend.tests;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PhysicalTestRepository extends JpaRepository<PhysicalTest, Long> {

    @Query("SELECT DISTINCT t FROM Battery b JOIN b.tests t WHERE b.team.id = :teamId")
    List<PhysicalTest> findAllByBatteriesTeamId(@Param("teamId") Long teamId);

    @Query("SELECT DISTINCT t FROM Battery b JOIN b.tests t WHERE b.team.id IN :teamIds")
    List<PhysicalTest> findAllByBatteriesTeamIdIn(@Param("teamIds") List<Long> teamIds);
}