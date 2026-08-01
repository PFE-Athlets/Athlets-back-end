package com.centresportifets.athlets_backend.tests.dto;

import java.util.List;

import com.centresportifets.athlets_backend.tests.battery.Battery;

public record BatteryDTO(
    int id,
    String teamName,
    String name,
    Boolean status,
    List<PhysicalTestResponseDTO> physicalTests
) {
    public static BatteryDTO fromEntity(Battery battery) {
        return new BatteryDTO(
            battery.getId().intValue(),
            battery.getTeam().getName(),
            battery.getName(),
            battery.isStatus(),
            battery.getTests().stream()
                .map(PhysicalTestResponseDTO::fromEntity)
                .toList()
        );
    }
}
