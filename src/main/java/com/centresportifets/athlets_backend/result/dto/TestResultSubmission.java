package com.centresportifets.athlets_backend.result.dto;

import lombok.Data;

@Data
public class TestResultSubmission {
    private Long id;
    private String resultValue;
    private String videoProof;
    private String imageProof;
    private String comment;
}
