package com.centresportifets.athlets_backend.sport.position;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionRepository extends JpaRepository<Position, Long> {
    Optional<Position> findById(Long positionId);
    List<Position> findBySport_Id(Long disciplineId);
}
