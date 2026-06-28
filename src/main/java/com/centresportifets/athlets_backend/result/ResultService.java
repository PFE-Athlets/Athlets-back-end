package com.centresportifets.athlets_backend.result;

import com.centresportifets.athlets_backend.auth.AuthService;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import com.centresportifets.athlets_backend.auth.userTypes.Athlete;
import com.centresportifets.athlets_backend.auth.userTypes.AthleteRepository;
import com.centresportifets.athlets_backend.auth.userTypes.Coach;
import com.centresportifets.athlets_backend.auth.userTypes.CoachRepository;
import com.centresportifets.athlets_backend.auth.userTypes.UserType;
import com.centresportifets.athlets_backend.physicalTest.PhysicalTest;
import com.centresportifets.athlets_backend.physicalTest.PhysicalTestProof;
import com.centresportifets.athlets_backend.physicalTest.PhysicalTestRepository;
import com.centresportifets.athlets_backend.result.dto.TestAssignmentRequest;
import com.centresportifets.athlets_backend.result.dto.TestResultSubmission;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ResultService {
    private final AuthService authService;

    private final AthleteRepository athleteRepository;

    private final CoachRepository coachRepository;

    private final PhysicalTestRepository physicalTestRepository;

    private final ResultRepository resultRepository;

    @PreAuthorize("@authService.hasPermission(authentication, 'ADMIN') or @authService.hasPermission(authentication, 'COACH')")
    public void assignTest(TestAssignmentRequest request){
        List<Athlete> athletes = athleteRepository.findAllByUsernameIn(request.getUsernames());

        PhysicalTest physicalTest = physicalTestRepository.findById(request.getPhysicalTestId()).orElseThrow(() -> new IllegalArgumentException("Physical test not found"));

        athletes.forEach(athlete -> {
            Result result = new Result();
            result.setAthlete(athlete);
            result.setTest(physicalTest);
            //result.setDateToComplete(request.getDateToComplete());
        });
    }

    public void submitAthleteResult(TestResultSubmission resultSubmission, Authentication auth) {
        Result result = resultRepository.findById(resultSubmission.getId()).orElseThrow(() -> new IllegalArgumentException("Physical test result not found"));

        if(!authService.checkIfUserIsAuthenticatedUser(result.getAthlete(), auth)){
            throw new AccessDeniedException("You are not authorized to submit this result.");
        }

        PhysicalTestProof requiredProof = PhysicalTestProof.valueOf(result.getTest().getProof());

        switch(requiredProof){
            case VIDEO:
                if (isMissing(resultSubmission.getVideoProof())) {
                    throw new IllegalArgumentException("A video proof is required for this test.");
                }
                result.setVideoProof(resultSubmission.getVideoProof());
                break;

            case PHOTO:
                if (isMissing(resultSubmission.getImageProof())) {
                    throw new IllegalArgumentException("A photo proof is required for this test.");
                }
                result.setPhotoProof(resultSubmission.getImageProof());
                break;

            case BOTH:
                if (isMissing(resultSubmission.getVideoProof()) || isMissing(resultSubmission.getImageProof())) {
                    throw new IllegalArgumentException("Both photo and video proofs are required for this test.");
                }
                result.setVideoProof(resultSubmission.getVideoProof());
                result.setPhotoProof(resultSubmission.getImageProof());
                break;

            case NONE:
            default:
                break;
        }

        if (isMissing(resultSubmission.getResultValue())) {
            throw new IllegalArgumentException("The result value is missing for this test");
        }

        result.setResultValue(resultSubmission.getResultValue());
        result.setCommentText(resultSubmission.getComment());

        result.setStatus(ResultStatus.PENDING.getStatus());
    }

    public void cancelSubmitionAthleteResult(Long testResultId, Authentication auth) {
        Result result = resultRepository.findById(testResultId).orElseThrow(() -> new IllegalArgumentException("Physical test result not found"));

        if(authService.checkIfUserIsAuthenticatedUser(result.getAthlete(), auth)){
            throw new AccessDeniedException("You are not authorized to submit this result.");
        }

        result.setStatus(ResultStatus.ASSIGNED.getStatus());
    }

    @PreAuthorize("@authService.hasPermission(authentication, 'ADMIN') or @authService.hasPermission(authentication, 'COACH')")
    public void approveAthleteResult(Long testResultId, boolean approved) {
        Result result = resultRepository.findById(testResultId).orElseThrow(() -> new IllegalArgumentException("Physical test result not found"));
        
        String status = approved ? ResultStatus.APPROVED.getStatus() : ResultStatus.REJECTED.getStatus();
        
        result.setStatus(status);
    }

    public Page<Result> getTestResults(Authentication auth, Pageable pageable){
        UserType currentType = authService.getAuthenticatedUserType(auth);

        switch (currentType){
            case ADMIN: return resultRepository.findAll(pageable);
            case COACH: 
                Coach coach = coachRepository.findByUsername(auth.getName())
                        .orElseThrow(() -> new IllegalArgumentException("Coach profile not found"));

                List<Athlete> teamAthletes = athleteRepository.findByAthleteTeamsTeamId(coach.getTeam().getId());

                if (teamAthletes.isEmpty()) {
                    return Page.empty(pageable);
                }

                List<Long> athleteIds = teamAthletes.stream()
                        .map(athlete -> athlete.getId())
                        .collect(Collectors.toList());

                return resultRepository.findByAthleteIdIn(athleteIds, pageable);
            case ATHLETE: 
            default: return resultRepository.findByAthleteUsername(auth.getName(), pageable);
        }
    }

    private boolean isMissing(String proofString) {
        return proofString == null || proofString.isBlank();
    }
}
