package com.centresportifets.athlets_backend.physicalTest.dto;

import java.util.List;

import lombok.Data;

@Data
public class PhysicalTestCreateRequest {
    private String testName;
    private String unit;
    private String protocol;
    private List<String> sportNames;
    private String proof;
}
