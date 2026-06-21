package com.centresportifets.athlets_backend.physicalTest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.centresportifets.athlets_backend.physicalTest.dto.PhysicalTestCreateRequest;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
	name = "Physical Test controller",
	description = "Handles all actions related to the physical tests")
@RestController
@RequestMapping("/api/physicalTest")
public class PhysicalTestController {
    private final PhysicalTestService physicalTestService;

    public PhysicalTestController(PhysicalTestService physicalTestService) {
        this.physicalTestService = physicalTestService;
    }

    @PostMapping("/create")
	public ResponseEntity<Void> createPhysicalTest(@RequestBody PhysicalTestCreateRequest request) {
        physicalTestService.createPhysicalTest(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<PhysicalTest>> getAllPhysicalTests() {
        List<PhysicalTest> tests = physicalTestService.getPhysicalTests();
        return ResponseEntity.ok(tests);
    }
}