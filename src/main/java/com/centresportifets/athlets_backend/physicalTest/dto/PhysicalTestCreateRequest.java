package com.centresportifets.athlets_backend.physicalTest.dto;

import java.util.Set;

import lombok.Data;

@Data
public class PhysicalTestCreateRequest {
    private String testName;
    private String unit;
    private String protocole;
    private Set<String> sportNames;
    private String proof;
}
