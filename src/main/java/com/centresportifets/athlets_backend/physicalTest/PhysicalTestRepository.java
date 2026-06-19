package com.centresportifets.athlets_backend.physicalTest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhysicalTestRepository extends JpaRepository<PhysicalTest, Long> { }