package com.centresportifets.athlets_backend.result.dto;

import java.util.List;
import lombok.Data;

@Data
public class TestResultSubmission {
    private Long id;
    private String proof;
    private String comment;
    private List<ResultValueSubmissionDTO> resultValues;
}