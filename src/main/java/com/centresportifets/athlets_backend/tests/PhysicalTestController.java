package com.centresportifets.athlets_backend.tests;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.centresportifets.athlets_backend.tests.dto.BatteryCreateRequest;
import com.centresportifets.athlets_backend.tests.dto.BatteryDTO;
import com.centresportifets.athlets_backend.tests.dto.BatteryModRequest;
import com.centresportifets.athlets_backend.tests.dto.PhysicalTestCreateRequest;
import com.centresportifets.athlets_backend.tests.dto.PhysicalTestResponseDTO;
import com.centresportifets.athlets_backend.tests.equipment.Equipment;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * REST Controller for managing physical tests, test batterys, and associated reference data.
 */
@Tag(
    name = "Physical Test controller",
    description = "Handles all actions related to the physical tests")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/physicalTest")
public class PhysicalTestController {
    private final PhysicalTestService physicalTestService;

    /**
     * Handles the creation of a physical test.
     *
     * @param request physical test data needed for creation
     * @return a {@link ResponseEntity} returning {@code 201 CREATED} upon successful test creation
     */
    @PostMapping("/create")
    public ResponseEntity<Void> createPhysicalTest(@RequestBody PhysicalTestCreateRequest request) {
        physicalTestService.createPhysicalTest(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Creates a new test battery grouping multiple physical tests for a team.
     *
     * @param request battery creation data including battery name, team ID, status, and test IDs
     */
    @PostMapping("/battery/create")
    public void createBattery(@RequestBody BatteryCreateRequest request) {
        physicalTestService.createBattery(request);
    }

    /**
     * Retrieves all available test batterys.
     *
     * @return a list of {@link BatteryDTO} representing the registered test batterys
     */
    @GetMapping("/battery")
    public List<BatteryDTO> getBatterys() {
        return physicalTestService.getBatterys();
    }

    /**
     * Modifies an existing test battery's information or associated physical tests.
     *
     * @param request modification request containing the battery ID, updated name, status, and physical test IDs to add
     */
    @PostMapping("/battery")
    public void postMethodName(@RequestBody BatteryModRequest request) {
        physicalTestService.modifyBattery(request);
    }

    /**
     * Fetches a list of physical tests filtered based on the role and permissions of the authenticated user.
     *
     * @param auth the current user's authentication context
     * @return a list of {@link PhysicalTestResponseDTO} accessible to the caller
     */
    @GetMapping
    public List<PhysicalTestResponseDTO> getAllPhysicalTests(Authentication auth) {
        return physicalTestService.getPhysicalTests(auth);
    }

    /**
     * Retrieves all available measurement units for test result evaluation.
     *
     * @return a list of {@link UnitMeasure} reference entities
     */
    @GetMapping("/units")
    public List<UnitMeasure> getTestUnits() {
        return physicalTestService.getUnits();
    }

    /**
     * Retrieves all registered physical test equipment.
     *
     * @return a list of {@link Equipment} entities
     */
    @GetMapping("/equipments")
    public List<Equipment> getTestEquipments() {
        return physicalTestService.getEquipments();
    }

    /**
     * Retrieves all registered physical quality categories evaluated during tests.
     *
     * @return a list of {@link PhysicalQuality} entities
     */
    @GetMapping("/qualities")
    public List<PhysicalQuality> getTestQualities() {
        return physicalTestService.getPhysicalQualities();
    }
}