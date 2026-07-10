package com.centresportifets.athlets_backend.result.dto;

import java.util.List;

import lombok.Data;

@Data
public class TestAssignmentRequest {
    private List<String> usernames;
    private String dateToComplete;
    private Long physicalTestId;
}
