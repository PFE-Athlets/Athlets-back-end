package com.centresportifets.athlets_backend.sport;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SportRepository extends JpaRepository<Sport, Long> {
    List<Sport> findAllByName(Set<String> name);
}