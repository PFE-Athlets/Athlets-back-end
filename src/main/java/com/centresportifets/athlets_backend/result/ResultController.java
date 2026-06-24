package com.centresportifets.athlets_backend.result;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.centresportifets.athlets_backend.result.dto.TestAssignmentRequest;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(
	name = "Result controller",
	description = "Handles all actions related to attributing/filling test results")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/result")
public class ResultController {
    private final ResultService resultService;

    @PostMapping("/assign")
    public ResponseEntity<Void> assignTestToAthletes(@RequestBody TestAssignmentRequest request, Authentication auth) {
        resultService.assignTest(request, auth);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/submit/{testId}/status")
    public ResponseEntity<Void> submitResult(@PathVariable Long testId, Authentication auth) {
        resultService.submitAthleteResult(testId, auth);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/approve/{testId}/{status}")
    public ResponseEntity<Void> approveResult(@PathVariable Long testId, @PathVariable boolean status, Authentication auth) {
        resultService.approveAthleteResult(testId, status, auth);
        return ResponseEntity.ok().build();
    }
}
