package com.centresportifets.athlets_backend.team;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;

import com.centresportifets.athlets_backend.auth.AuthService;
import com.centresportifets.athlets_backend.team.dto.SubcoachDisplay;
import com.centresportifets.athlets_backend.team.dto.TeamDisplay;
import com.centresportifets.athlets_backend.team.dto.TeamModificationRequest;
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

    @PreAuthorize("@authService.hasPermission(authentication, 'ADMIN') or @authService.hasPermission(authentication, 'COACH')")
    public void modifyTeam(Long teamId, TeamModificationRequest request, Authentication auth) {
        if (authService.getAuthenticatedUserType(auth) == UserType.COACH) {
            Coach coach = coachRepository.findByUsername(auth.getName()).get();

            if (!coach.getTeam().getId().equals(teamId)) {
                throw new SecurityException("You are not authorized to modify this team.");
            }
        }

        Team team = teamRepository.findById(teamId).orElseThrow(() -> new IllegalArgumentException("Team not found"));
        team.setName(request.getNewTeamName());
        teamRepository.save(team);

        updateTeamCoaches(team, request.getNewCoachId(), request.getNewSubcoachesIds());
    }

    private void updateTeamCoaches(Team team, Long newCoachId, List<Long> newSubcoachesIds) {
        if (newSubcoachesIds.contains(newCoachId)) {
            throw new IllegalArgumentException("The head coach cannot be listed as a subcoach.");
        }

        Coach previousHeadCoach = coachRepository.findByTeam_IdAndIsHeadCoachTrue(team.getId());
        if (previousHeadCoach != null && !previousHeadCoach.getId().equals(newCoachId)) {
            previousHeadCoach.setTeam(null);
            previousHeadCoach.setSport(null);
            previousHeadCoach.setAccountStatus("Inactive");
            coachRepository.save(previousHeadCoach);
        }

        Coach newHeadCoach = coachRepository.findById(newCoachId).orElseThrow(() -> new IllegalArgumentException("New head coach not found"));
        newHeadCoach.setTeam(team);
        newHeadCoach.setSport(team.getSport());
        newHeadCoach.setHeadCoach(true);
        newHeadCoach.setAccountStatus("Active");
        coachRepository.save(newHeadCoach);

        List<Coach> previousSubcoaches = coachRepository.findByTeam_IdAndIsHeadCoachFalse(team.getId());
        for (Coach previousSubcoach : previousSubcoaches) {
            if (!newSubcoachesIds.contains(previousSubcoach.getId())) {
                previousSubcoach.setTeam(null);
                previousSubcoach.setSport(null);
                previousSubcoach.setAccountStatus("Inactive");
                coachRepository.save(previousSubcoach);
            }
        }

        for (Long newSubcoachId : newSubcoachesIds) {
            Coach newSubcoach = coachRepository.findById(newSubcoachId).orElseThrow(() -> new IllegalArgumentException("New subcoach not found"));
            newSubcoach.setTeam(team);
            newSubcoach.setSport(team.getSport());
            newSubcoach.setHeadCoach(false);
            newSubcoach.setAccountStatus("Active");
            coachRepository.save(newSubcoach);
        }
    }
}
