package com.centresportifets.athlets_backend.result;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.centresportifets.athlets_backend.result.dto.TestAssignmentRequest;
import com.centresportifets.athlets_backend.result.dto.TestResultSubmission;

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
    public ResponseEntity<Void> assignTestToAthletes(@RequestBody TestAssignmentRequest request) {
        resultService.assignTest(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/submit")
    public ResponseEntity<Void> submitResult(@RequestBody TestResultSubmission testResultSubmission, Authentication auth) {
        resultService.submitAthleteResult(testResultSubmission, auth);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/cancel/{testResultId}")
    public ResponseEntity<Void> cancelSubmition(@PathVariable Long testResultId, Authentication auth) {
        resultService.cancelSubmitionAthleteResult(testResultId, auth);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/verify/{testResultId}/{approved}")
    public ResponseEntity<Void> approveResult(@PathVariable Long testResultId, @PathVariable boolean approved) {
        resultService.approveAthleteResult(testResultId, approved);
        return ResponseEntity.ok().build();
    }

    // @GetMapping()
    // public ResponseEntity<Page<Result>> getTestResults(@PageableDefault(size = 20, sort = "testDate") Pageable pageable, Authentication auth) {
    //     return resultService.getTestResults(auth);
    // }
}
