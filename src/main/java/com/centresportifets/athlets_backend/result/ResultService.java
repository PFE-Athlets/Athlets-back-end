package com.centresportifets.athlets_backend.result;

import com.centresportifets.athlets_backend.auth.AuthService;
import com.centresportifets.athlets_backend.result.dto.ResultValueSubmissionDTO;
import com.centresportifets.athlets_backend.result.dto.TestAssignmentRequest;
import com.centresportifets.athlets_backend.result.dto.TestData;
import com.centresportifets.athlets_backend.result.dto.TestResultSubmission;
import com.centresportifets.athlets_backend.tests.PhysicalTest;
import com.centresportifets.athlets_backend.tests.PhysicalTestRepository;
import com.centresportifets.athlets_backend.tests.ResultType;
import com.centresportifets.athlets_backend.tests.ResultTypeRepository;
import com.centresportifets.athlets_backend.user.UserType;
import com.centresportifets.athlets_backend.user.athlete.Athlete;
import com.centresportifets.athlets_backend.user.athlete.AthleteRepository;
import com.centresportifets.athlets_backend.user.coach.Coach;
import com.centresportifets.athlets_backend.user.coach.CoachRepository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ResultService {

    private final AuthService authService;
    private final AthleteRepository athleteRepository;
    private final CoachRepository coachRepository;
    private final PhysicalTestRepository physicalTestRepository;
    private final ResultRepository resultRepository;
    private final ResultTypeRepository resultTypeRepository;
    private final ResultValueRepository resultValueRepository;

    private static final Logger log = LoggerFactory.getLogger(ResultService.class);

    @PreAuthorize("@authService.canManageAthletes(authentication, #request.usernames)")
    public void assignTest(TestAssignmentRequest request) {
        List<Athlete> athletes = athleteRepository.findAllByUsernameIn(request.getUsernames());
        PhysicalTest physicalTest = physicalTestRepository.findById(request.getPhysicalTestId())
                .orElseThrow(() -> new IllegalArgumentException("Physical test not found"));

        athletes.forEach(athlete -> {
            Result result = new Result();
            result.setAthlete(athlete);
            result.setTest(physicalTest);
            result.setStatus(ResultStatus.ASSIGNED.getStatus());
            resultRepository.save(result);
        });
    }

    @Transactional
    public void submitAthleteResult(TestResultSubmission resultSubmission, Authentication auth) {
        Result result = resultRepository.findById(resultSubmission.getId())
                .orElseThrow(() -> new IllegalArgumentException("Physical test result not found"));

        if (!authService.isAthleteOwner(auth, result.getAthlete())) {
            throw new AccessDeniedException("You are not authorized to submit this result.");
        }

        if (result.getTest().isProofRequired() && isMissing(resultSubmission.getProof())) {
            throw new IllegalArgumentException("Proof is required for this test");
        }

        if (resultSubmission.getResultValues() == null || resultSubmission.getResultValues().isEmpty()) {
            throw new IllegalArgumentException("At least one result value is required for this test");
        }

        resultValueRepository.deleteByResultId(result.getId());

        for (ResultValueSubmissionDTO valueDTO : resultSubmission.getResultValues()) {
            if (valueDTO.getValue() == null) {
                throw new IllegalArgumentException("Result value cannot be null for result type ID: " + valueDTO.getResultTypeId());
            }

            ResultType resultType = resultTypeRepository.findById(valueDTO.getResultTypeId())
                    .orElseThrow(() -> new IllegalArgumentException("Result type not found with ID: " + valueDTO.getResultTypeId()));

            ResultValue resultValue = new ResultValue();
            resultValue.setResult(result);
            resultValue.setResultType(resultType);
            resultValue.setValue(valueDTO.getValue());

            resultValueRepository.save(resultValue);
        }

        result.setProof(resultSubmission.getProof());
        result.setCommentText(resultSubmission.getComment());
        result.setTestDate(LocalDate.now());
        result.setStatus(ResultStatus.PENDING.getStatus());

        resultRepository.save(result);
    }

    @Transactional
    public void cancelSubmissionAthleteResult(Long testResultId, Authentication auth) {
        Result result = resultRepository.findById(testResultId)
                .orElseThrow(() -> new IllegalArgumentException("Physical test result not found"));

        if (!authService.isAthleteOwner(auth, result.getAthlete())) {
            throw new AccessDeniedException("You are not authorized to cancel this submission.");
        }

        resultValueRepository.deleteByResultId(result.getId());
        result.setStatus(ResultStatus.ASSIGNED.getStatus());
        resultRepository.save(result);
    }

    @PreAuthorize("@authService.hasPermission(authentication, 'ADMIN') or @authService.hasPermission(authentication, 'COACH')")
    public void approveAthleteResult(Long testResultId, boolean approved) {
        Result result = resultRepository.findById(testResultId)
                .orElseThrow(() -> new IllegalArgumentException("Physical test result not found"));

        String status = approved ? ResultStatus.APPROVED.getStatus() : ResultStatus.REJECTED.getStatus();
        result.setStatus(status);
        resultRepository.save(result);
    }

    public List<TestData> getTestResults(Authentication auth) {
        UserType currentType = authService.getAuthenticatedUserType(auth);
        log.info("User {} with role {} is retrieving test results", auth.getName(), currentType);

        return switch (currentType) {
            case ADMIN -> resultRepository.findAll().stream().map(TestData::new).toList();
            case COACH -> getCoachTeamResults(auth.getName());
            default -> resultRepository.findByAthleteUsername(auth.getName()).stream().map(TestData::new).toList();
        };
    }

    private List<TestData> getCoachTeamResults(String coachUsername) {
        Coach coach = coachRepository.findByUsername(coachUsername)
                .orElseThrow(() -> new IllegalArgumentException("Coach profile not found"));

        List<Athlete> teamAthletes = athleteRepository.findByAthleteTeamsTeamId(coach.getTeam().getId());
        if (teamAthletes.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> athleteIds = teamAthletes.stream().map(Athlete::getId).toList();
        return resultRepository.findByAthleteIdIn(athleteIds).stream().map(TestData::new).toList();
    }

    private boolean isMissing(String str) {
        return str == null || str.isBlank();
    }
}