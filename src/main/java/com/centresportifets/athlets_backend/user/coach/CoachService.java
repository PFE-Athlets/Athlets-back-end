package com.centresportifets.athlets_backend.user.coach;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.centresportifets.athlets_backend.user.coach.dto.CoachListDisplay;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CoachService {
    private final CoachRepository coachRepository;

    @PreAuthorize("@authService.hasPermission(authentication, 'ADMIN') or @authService.hasPermission(authentication, 'COACH')")
    public List<CoachListDisplay> getCoaches() {
        return coachRepository.findAll().stream().map(coach -> {
            CoachListDisplay display = new CoachListDisplay();
            display.setCoachId(coach.getId());
            display.setCoachName(coach.getFirstName() + " " + coach.getLastName());
            return display;
        }).toList();
    }
}
