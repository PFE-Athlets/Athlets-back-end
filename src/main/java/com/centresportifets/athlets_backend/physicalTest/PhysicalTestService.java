package com.centresportifets.athlets_backend.physicalTest;

import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.centresportifets.athlets_backend.physicalTest.dto.PhysicalTestCreateRequest;
import com.centresportifets.athlets_backend.physicalTest.equipment.Equipment;
import com.centresportifets.athlets_backend.physicalTest.equipment.EquipmentRepository;
import com.centresportifets.athlets_backend.physicalTest.equipment.TestEquipment;
import com.centresportifets.athlets_backend.physicalTest.equipment.TestEquipmentId;
import com.centresportifets.athlets_backend.physicalTest.equipment.TestEquipmentRepository;
import com.centresportifets.athlets_backend.sport.Sport;
import com.centresportifets.athlets_backend.sport.SportRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class PhysicalTestService {
    private final PhysicalTestRepository physicalTestRepository;
    private final PhysicalQualityRepository physicalQualityRepository;
    private final EquipmentRepository equipmentRepository;
    private final TestEquipmentRepository testEquipmentRepository;
    private final UnitMeasureRepository unitMeasureRepository;
    private final ResultTypeRepository resultTypeRepository;

    public List<PhysicalTest> getPhysicalTests(){
        return physicalTestRepository.findAll();
    }

    @Transactional
    public void createPhysicalTest(PhysicalTestCreateRequest request) {
        PhysicalTest newTest = new PhysicalTest();

        // 1. Basic Fields
        newTest.setName(request.testName());
        newTest.setProtocol(request.protocol());
        newTest.setInformations(request.informationsSup());
        newTest.setSupervised(request.supervised());
        newTest.setProofRequired(request.proofRequired());
        
        PhysicalQuality quality = physicalQualityRepository.findById(request.physicalQualityId())
                .orElseThrow(() -> new IllegalArgumentException("Physical quality not found: " + request.physicalQualityId()));
        newTest.setPhysicalQuality(quality);

        PhysicalTest savedTest = physicalTestRepository.save(newTest);

        if (request.equipments() != null && !request.equipments().isEmpty()) {
            List<TestEquipment> testEquipments = request.equipments().stream()
                    .map(equipmentDTO -> {
                        Equipment equipment = equipmentRepository.findById(equipmentDTO.id())
                                .orElseThrow(() -> new IllegalArgumentException("Equipment not found: " + equipmentDTO.id()));

                        TestEquipment testEquipment = new TestEquipment();
                        testEquipment.setId(new TestEquipmentId(savedTest.getId(), equipment.getId()));
                        testEquipment.setTest(savedTest);
                        testEquipment.setEquipment(equipment);
                        testEquipment.setQuantityRequired(equipmentDTO.quantity());

                        return testEquipment;
                    })
                    .toList();

            testEquipmentRepository.saveAll(testEquipments);
        }

        if (request.resultTypes() != null && !request.resultTypes().isEmpty()) {
            List<ResultType> resultTypes = request.resultTypes().stream()
                    .map(resultTypeDTO -> {
                        UnitMeasure unit = unitMeasureRepository.findById((long) resultTypeDTO.unitId())
                                .orElseThrow(() -> new IllegalArgumentException("Unit of measure not found: " + resultTypeDTO.unitId()));

                        ResultType resultType = new ResultType();
                        resultType.setTest(savedTest);
                        resultType.setName(resultTypeDTO.name());
                        resultType.setUnitMeasure(unit);
                        return resultType;
                    })
                    .toList();

            resultTypeRepository.saveAll(resultTypes);
        }
    }
}
