package com.centresportifets.athlets_backend.tests;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.centresportifets.athlets_backend.tests.dto.BatteryCreateRequest;
import com.centresportifets.athlets_backend.tests.dto.PhysicalTestCreateRequest;
import com.centresportifets.athlets_backend.tests.dto.PhysicalTestResponseDTO;
import com.centresportifets.athlets_backend.tests.equipment.Equipment;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(
	name = "Physical Test controller",
	description = "Handles all actions related to the physical tests")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/physicalTest")
public class PhysicalTestController {
    private final PhysicalTestService physicalTestService;

    /**
	 * Handles the creation of a physical test
	 *
	 * @param request        physical test data needed for creation
	 * @return a {@link ResponseEntity} returning {@code 201 CREATED} upon successful test creation
	 */
    @PostMapping("/create")
	public ResponseEntity<Void> createPhysicalTest(@RequestBody PhysicalTestCreateRequest request) {
        physicalTestService.createPhysicalTest(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/battery/create")
    public void createBattery(@RequestBody BatteryCreateRequest request) {
        physicalTestService.createBattery(request);
    }

    /**
	 * Fetches a list of all of the physical tests
	 *
	 * @return a list of every physical test in the database
	 */
    @GetMapping
    public List<PhysicalTestResponseDTO> getAllPhysicalTests() {
        return physicalTestService.getPhysicalTests();
    }

    @GetMapping("/units")
    public List<UnitMeasure> getTestUnits() {
        return physicalTestService.getUnits();
    }

    @GetMapping("/equipments")
    public List<Equipment> getTestEquipments() {
        return physicalTestService.getEquipments();
    }

    @GetMapping("/qualities")
    public List<PhysicalQuality> getTestQualities() {
        return physicalTestService.getPhysicalQualities();
    }
    
}