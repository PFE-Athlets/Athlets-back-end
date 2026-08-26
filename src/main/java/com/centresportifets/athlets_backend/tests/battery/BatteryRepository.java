package com.centresportifets.athlets_backend.tests.battery;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BatteryRepository extends JpaRepository<Battery, Long> {
    List<Battery> findByTeam_IdAndTests_Id(Long teamId, Long testId);
}
