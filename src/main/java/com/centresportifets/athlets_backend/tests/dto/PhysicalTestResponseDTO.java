package com.centresportifets.athlets_backend.tests.dto;

import java.util.List;
import com.centresportifets.athlets_backend.tests.PhysicalTest;

public record PhysicalTestResponseDTO(
    Long id,
    String name,
    String protocol,
    boolean supervised,
    String informations,
    boolean proofRequired,
    PhysicalQualityDTO physicalQuality,
    List<EquipmentDTO> equipments,
    List<ResultTypeDTO> resultTypes
) {
    public record PhysicalQualityDTO(
        int id,
        String name
    ) {}
    public record EquipmentDTO(
        int id,
        String name
    ) {}
    public record ResultTypeDTO(
        int id,
        String name,
        String dataType,
        String unitName,
        String unitSymbol
    ) {}

    public static PhysicalTestResponseDTO fromEntity(PhysicalTest test) {
        PhysicalQualityDTO qualityDTO = test.getPhysicalQuality() != null 
            ? new PhysicalQualityDTO(test.getPhysicalQuality().getId(), test.getPhysicalQuality().getName())
            : null;

        List<EquipmentDTO> equipmentDTOs = test.getEquipments() != null
            ? test.getEquipments().stream()
                .map(e -> new EquipmentDTO(e.getId().intValue(), e.getName()))
                .toList()
            : List.of();

        List<ResultTypeDTO> resultTypeDTOs = test.getResultTypes() != null
            ? test.getResultTypes().stream()
                .map(rt -> new ResultTypeDTO(
                    rt.getId().intValue(),
                    rt.getName(),
                    rt.getDataType(),
                    rt.getUnitMeasure() != null ? rt.getUnitMeasure().getName() : null,
                    rt.getUnitMeasure() != null ? rt.getUnitMeasure().getSymbol() : null
                ))
                .toList()
            : List.of();

        return new PhysicalTestResponseDTO(
            test.getId(),
            test.getName(),
            test.getProtocol(),
            test.isSupervised(),
            test.getInformations(),
            test.isProofRequired(),
            qualityDTO,
            equipmentDTOs,
            resultTypeDTOs
        );
    }
}