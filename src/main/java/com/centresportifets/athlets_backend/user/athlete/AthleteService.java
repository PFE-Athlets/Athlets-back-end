package com.centresportifets.athlets_backend.user.athlete;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.centresportifets.athlets_backend.auth.AuthService;
import com.centresportifets.athlets_backend.team.AthleteTeam;
import com.centresportifets.athlets_backend.team.Team;
import com.centresportifets.athlets_backend.team.TeamRepository;
import com.centresportifets.athlets_backend.user.UserType;
import com.centresportifets.athlets_backend.user.athlete.dto.AthleteCreateRequest;
import com.centresportifets.athlets_backend.user.coach.CoachRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AthleteService {
    private final AthleteRepository athleteRepository;
    private final TeamRepository teamRepository;
    private final AthleteTeamRepository athleteTeamRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final CoachRepository coachRepository;

    @PreAuthorize("@authService.hasPermission(authentication, 'ADMIN') or @authService.hasPermission(authentication, 'COACH')")
    public void createAthlete(AthleteCreateRequest request, Authentication auth) {
        String teamName = request.getAthleteTeamName();
        if (authService.getAuthenticatedUserType(auth) == UserType.COACH) {
            String coachTeamName = coachRepository.findByUsername(auth.getName()).get().getTeam().getName();
            if (!coachTeamName.equals(request.getAthleteTeamName())) {
                throw new IllegalArgumentException("Coaches can only create athletes for their own team.");
            }
        }
        
        Team team = teamRepository.findByName(teamName)
            .orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamName));

        Athlete athlete = AthleteMapper.toAthlete(request, passwordEncoder.encode("ChangeMe123!"));
        athlete = athleteRepository.save(athlete);

        AthleteTeam athleteTeam = new AthleteTeam();
        athleteTeam.setAthlete(athlete);
        athleteTeam.setTeam(team);
        // TODO set discipline and position fields if provided
        athleteTeamRepository.save(athleteTeam);
    }
}
