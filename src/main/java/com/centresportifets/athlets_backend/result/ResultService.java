package com.centresportifets.athlets_backend.result;

import com.centresportifets.athlets_backend.auth.AuthService;

import java.util.Collections;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import com.centresportifets.athlets_backend.tests.PhysicalTest;
import com.centresportifets.athlets_backend.tests.PhysicalTestRepository;
import com.centresportifets.athlets_backend.result.dto.TestAssignmentRequest;
import com.centresportifets.athlets_backend.result.dto.TestData;
import com.centresportifets.athlets_backend.result.dto.TestResultSubmission;
import com.centresportifets.athlets_backend.user.UserType;
import com.centresportifets.athlets_backend.user.athlete.Athlete;
import com.centresportifets.athlets_backend.user.athlete.AthleteRepository;
import com.centresportifets.athlets_backend.user.coach.Coach;
import com.centresportifets.athlets_backend.user.coach.CoachRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ResultService {
    private final AuthService authService;
    private final AthleteRepository athleteRepository;
    private final CoachRepository coachRepository;
    private final PhysicalTestRepository physicalTestRepository;
    private final ResultRepository resultRepository;

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

    public void submitAthleteResult(TestResultSubmission resultSubmission, Authentication auth) {
        Result result = resultRepository.findById(resultSubmission.getId())
                .orElseThrow(() -> new IllegalArgumentException("Physical test result not found"));

        if (!authService.isAthleteOwner(auth, result.getAthlete())) {
            throw new AccessDeniedException("You are not authorized to submit this result.");
        }

        // validateProofRequirements(result, resultSubmission);

        if (isMissing(resultSubmission.getResultValue())) {
            throw new IllegalArgumentException("The result value is missing for this test");
        }

        result.setResultValue(resultSubmission.getResultValue());
        result.setCommentText(resultSubmission.getComment());
        result.setStatus(ResultStatus.PENDING.getStatus());
        resultRepository.save(result);
    }

    public void cancelSubmissionAthleteResult(Long testResultId, Authentication auth) {
        Result result = resultRepository.findById(testResultId)
                .orElseThrow(() -> new IllegalArgumentException("Physical test result not found"));

        if (!authService.isAthleteOwner(auth, result.getAthlete())) {
            throw new AccessDeniedException("You are not authorized to cancel this submission.");
        }

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

    /*private void validateProofRequirements(Result result, TestResultSubmission submission) {
        PhysicalTestProof requiredProof = PhysicalTestProof.valueOf(result.getTest().getProof());
        boolean videoMissing = isMissing(submission.getVideoProof());
        boolean photoMissing = isMissing(submission.getImageProof());

        switch (requiredProof) {
            case VIDEO -> {
                if (videoMissing) throw new IllegalArgumentException("A video proof is required for this test.");
                result.setVideoProof(submission.getVideoProof());
            }
            case PHOTO -> {
                if (photoMissing) throw new IllegalArgumentException("A photo proof is required for this test.");
                result.setPhotoProof(submission.getImageProof());
            }
            case BOTH -> {
                if (videoMissing || photoMissing) throw new IllegalArgumentException("Both photo and video proofs are required.");
                result.setVideoProof(submission.getVideoProof());
                result.setPhotoProof(submission.getImageProof());
            }
            default -> {}
        }
    }*/

    private boolean isMissing(String proofString) {
        return proofString == null || proofString.isBlank();
    }
}