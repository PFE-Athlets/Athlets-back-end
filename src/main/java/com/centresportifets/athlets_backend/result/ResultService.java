package com.centresportifets.athlets_backend.result;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import com.centresportifets.athlets_backend.auth.userTypes.Athlete;
import com.centresportifets.athlets_backend.auth.userTypes.AthleteRepository;
import com.centresportifets.athlets_backend.physicalTest.PhysicalTest;
import com.centresportifets.athlets_backend.result.dto.TestAssignmentRequest;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ResultService {
    private final AthleteRepository athleteRepository;

    private final PhysicalTestRepository physicalTestRepository;

    @PreAuthorize("@authService.hasPermission(authentication, 'ADMIN') or @authService.hasPermission(authentication, 'COACH')")
    public void assignTest(TestAssignmentRequest request, Authentication auth){
        List<Athlete> athletes = athleteRepository.findAllByUsernameIn(request.getUsernames());

        PhysicalTest physicalTest = physicalTestRepository.findById(request.getPhysicalTestId()).orElseThrow(() -> new IllegalArgumentException("Physical test not found"));

        athletes.forEach(athlete -> {
            Result result = new Result();
            result.setAthlete(athlete);
            result.setTest(physicalTest);
            //result.setDateToComplete(request.getDateToComplete());
        });
    }

    public void submitAthleteResult(Long testId, Authentication auth) {
        
    }
}
