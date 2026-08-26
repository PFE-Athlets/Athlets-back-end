package com.centresportifets.athlets_backend.result;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.centresportifets.athlets_backend.result.dto.ResultPageData;
import com.centresportifets.athlets_backend.result.dto.ResultRowData;
import com.centresportifets.athlets_backend.result.dto.TestAssignmentRequest;
import com.centresportifets.athlets_backend.result.dto.TestData;
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
    public ResponseEntity<Void> cancelSubmission(@PathVariable Long testResultId, Authentication auth) {
        resultService.cancelSubmissionAthleteResult(testResultId, auth);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/verify/{testResultId}/{approved}")
    public ResponseEntity<Void> approveResult(@PathVariable Long testResultId, @PathVariable boolean approved) {
        resultService.approveAthleteResult(testResultId, approved);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{resultId}")
    public ResponseEntity<ResultRowData> getResultById(
            @PathVariable Long resultId,
            Authentication auth) {

        return ResponseEntity.ok(
                resultService.getResultById(resultId, auth)
        );
    }

    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<ResultRowData>> getTeamResults(
            @PathVariable Long teamId,
            Authentication auth) {

        return ResponseEntity.ok(
                resultService.getTeamResults(
                        teamId,
                        auth
                )
        );
    }

    @GetMapping()
    public ResponseEntity<List<TestData>> getTestResults(Authentication auth) {
        return ResponseEntity.ok(resultService.getTestResults(auth));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportTestResults(Authentication auth) {
        byte[] workbook = resultService.exportTestResults(auth);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=resultats.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(workbook);
    @GetMapping("/page-data")
    public ResponseEntity<ResultPageData> getResultPageData(Authentication auth) {
        return ResponseEntity.ok(resultService.getResultPageData(auth));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportResults(
            Authentication auth,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long athleteId,
            @RequestParam(required = false) Long testId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) String statusCode,
            @RequestParam(required = false) Long batteryId) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=resultats.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resultService.exportResultsWorkbook(auth, startDate, endDate, athleteId, testId, teamId, statusCode, batteryId));
    }
}
