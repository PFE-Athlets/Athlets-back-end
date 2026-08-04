package com.centresportifets.athlets_backend.result;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ResultValueRepository extends JpaRepository<ResultValue, Long> { 
    void deleteByResultId(Long resultId);
}