package com.centresportifets.athlets_backend.result.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class TestResultSubmission {

    private Long id;
    private LocalDate testDate;
    private String proof;
    private String comment;
    private List<ResultValueSubmissionDTO> resultValues;
}