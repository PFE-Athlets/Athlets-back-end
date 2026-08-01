package com.centresportifets.athlets_backend.tests;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.centresportifets.athlets_backend.tests.battery.Battery;
import com.centresportifets.athlets_backend.tests.battery.BatteryRepository;
import com.centresportifets.athlets_backend.tests.dto.BatteryCreateRequest;
import com.centresportifets.athlets_backend.tests.dto.BatteryDTO;
import com.centresportifets.athlets_backend.tests.dto.PhysicalTestCreateRequest;
import com.centresportifets.athlets_backend.tests.dto.PhysicalTestResponseDTO;
import com.centresportifets.athlets_backend.tests.equipment.Equipment;
import com.centresportifets.athlets_backend.tests.equipment.EquipmentRepository;
import com.centresportifets.athlets_backend.tests.equipment.TestEquipment;
import com.centresportifets.athlets_backend.tests.equipment.TestEquipmentId;
import com.centresportifets.athlets_backend.tests.equipment.TestEquipmentRepository;
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
    private final BatteryRepository batteryRepository;

    public List<PhysicalTestResponseDTO> getPhysicalTests(){
        return physicalTestRepository.findAll().stream()
                .map(PhysicalTestResponseDTO::fromEntity)
                .toList();
    }

    @Transactional
    @PreAuthorize("@authService.hasPermission(authentication, 'ADMIN') || @authService.hasPermission(authentication, 'COACH')")
    public void createPhysicalTest(PhysicalTestCreateRequest request) {
        PhysicalTest newTest = new PhysicalTest();

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

    @PreAuthorize("@authService.hasPermission(authentication, 'ADMIN') || @authService.hasPermission(authentication, 'COACH')")
    public void createBattery(BatteryCreateRequest request) {
        Battery newBattery = new Battery();

        newBattery.setName(request.name());
        newBattery.setStatus(request.status());

        List<PhysicalTest> physicalTests = physicalTestRepository.findAllById(request.physicalTestIds());
        newBattery.setTests(new ArrayList<>(physicalTests));

        batteryRepository.save(newBattery);
    }

    @PreAuthorize("@authService.hasPermission(authentication, 'ADMIN') || @authService.hasPermission(authentication, 'COACH')")
    public List<BatteryDTO> getBatteries() {
        return batteryRepository.findAll().stream().map(BatteryDTO::fromEntity).toList();
    }

    public List<UnitMeasure> getUnits() {
        return unitMeasureRepository.findAll();
    }

    public List<Equipment> getEquipments() {
        return equipmentRepository.findAll();
    }

    public List<PhysicalQuality> getPhysicalQualities() {
        return physicalQualityRepository.findAll();
    }
}
