package com.centresportifets.athlets_backend.tests.dto;

import java.util.List;

public record BatteryCreateRequest(
    String name,
    int teamId,
    boolean status,
    List<Long> physicalTestIds
) {
    
}
