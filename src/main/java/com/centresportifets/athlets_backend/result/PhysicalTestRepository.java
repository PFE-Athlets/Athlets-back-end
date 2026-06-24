package com.centresportifets.athlets_backend.result;

import org.springframework.data.jpa.repository.JpaRepository;

import com.centresportifets.athlets_backend.physicalTest.PhysicalTest;

public interface PhysicalTestRepository extends JpaRepository<PhysicalTest, Long> { }