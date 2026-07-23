package com.centresportifets.athlets_backend.physicalTest.dto;

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
