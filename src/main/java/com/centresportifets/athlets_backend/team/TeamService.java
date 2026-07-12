package com.centresportifets.athlets_backend.team;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;

import com.centresportifets.athlets_backend.auth.AuthService;
import com.centresportifets.athlets_backend.team.dto.SubcoachDisplay;
import com.centresportifets.athlets_backend.team.dto.TeamDisplay;
import com.centresportifets.athlets_backend.user.UserType;
import com.centresportifets.athlets_backend.user.coach.Coach;
import com.centresportifets.athlets_backend.user.coach.CoachRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class TeamService {
    private final AuthService authService;
    private final TeamRepository teamRepository;
    private final AthleteTeamRepository athleteTeamRepository;
    private final CoachRepository coachRepository;

    @PreAuthorize("@authService.hasPermission(authentication, 'ADMIN') or @authService.hasPermission(authentication, 'COACH')")
    public List<Team> getTeams(Authentication auth) {
        if (authService.getAuthenticatedUserType(auth) == UserType.ADMIN) {
            return teamRepository.findAll();
        } else {
            return List.of(coachRepository.findByUsername(auth.getName()).get().getTeam());
        }
    }

    @PreAuthorize("@authService.hasPermission(authentication, 'ADMIN') or @authService.hasPermission(authentication, 'COACH')")
    public List<TeamDisplay> getTeamDisplays(Authentication auth) {
        List<TeamDisplay> teamDisplays = getTeams(auth).stream().map(team -> {
            TeamDisplay teamDisplay = new TeamDisplay();
            teamDisplay.setTeam(team);
            teamDisplay.setNumberOfAthletes(athleteTeamRepository.countByTeamId(team.getId()));
            
            Coach headCoach = coachRepository.findByTeam_IdAndIsHeadCoachTrue(team.getId());
            teamDisplay.setHeadCoachName(headCoach.getFirstName() + " " + headCoach.getLastName());
            teamDisplay.setHeadCoachId(headCoach.getId());
            return teamDisplay;
        }).toList();

        return teamDisplays;
    }

    @PreAuthorize("@authService.hasPermission(authentication, 'ADMIN') or @authService.hasPermission(authentication, 'COACH')")
    public List<SubcoachDisplay> getSubcoaches(Long teamId, Authentication auth) {
        if (authService.getAuthenticatedUserType(auth) == UserType.COACH) {
            Coach coach = coachRepository.findByUsername(auth.getName()).get();

            if (!coach.getTeam().getId().equals(teamId)) {
                throw new SecurityException("You are not authorized to access this team's subcoaches.");
            }
        }

        List<SubcoachDisplay> subcoaches = new ArrayList<>();
        coachRepository.findByTeam_IdAndIsHeadCoachFalse(teamId).forEach(coach -> {
            SubcoachDisplay subcoachDisplay = new SubcoachDisplay();
            subcoachDisplay.setSubcoachName(coach.getFirstName() + " " + coach.getLastName());
            subcoachDisplay.setCoachId(coach.getId());
            subcoaches.add(subcoachDisplay);
        });

        return subcoaches;
    }
}
