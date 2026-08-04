package com.centresportifets.athlets_backend.sport.discipline;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DisciplineRepository extends JpaRepository<Discipline, Long> {
    Optional<Discipline> findById(Long disciplineId);
    List<Discipline> findBySport_Id(Long sportId);
}
