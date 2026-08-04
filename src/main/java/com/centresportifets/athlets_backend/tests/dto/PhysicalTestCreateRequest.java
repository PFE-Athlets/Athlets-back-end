package com.centresportifets.athlets_backend.tests.dto;

import java.util.List;

public record PhysicalTestCreateRequest (
    String testName,
    int physicalQualityId,
    String protocol,
    String informationsSup,
    boolean supervised,
    boolean proofRequired,
    List<EquipmentDTO> equipments,
    List<ResultTypeDTO> resultTypes
) {
}
