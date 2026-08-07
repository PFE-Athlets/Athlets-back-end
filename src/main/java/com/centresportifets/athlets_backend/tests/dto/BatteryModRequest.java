package com.centresportifets.athlets_backend.tests.dto;

import java.util.List;

public record BatteryModRequest (
    long id,
    String newName,
    List<Long> physicalTestIdsToAdd,
    boolean newStatus
) {
}
